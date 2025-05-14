package com.vietjoke.vn.Activities.Booking
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
// import androidx.compose.foundation.lazy.grid.items // Không cần items riêng nếu dùng forEach
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.PassengerModel
// Đảm bảo bạn đã import các data class này (đã định nghĩa ở các bước trước)
import com.vietjoke.vn.model.SeatSelectionResult
import com.vietjoke.vn.model.SelectedSeatInfoForLeg
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.ResponseDTO.*
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import java.io.Serializable // Cần cho data class truyền qua Intent
import kotlin.Comparator

// ==================================
// === Activity Class ================
// ==================================
class SeatSelectionActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        val seatSelectionData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("seatSelectionData", SeatSelectionData::class.java)
        } else {
            intent.getSerializableExtra("seatSelectionData") as? SeatSelectionData
        }
        val isRoundTrip = intent.getBooleanExtra("isRoundTrip", false)

        val adults = FlightBookingModel.passengersAdult.toList()
        val children = FlightBookingModel.passengersChild.toList()
        val allPassengers = adults + children // Chỉ lấy adults và children như code gốc của bạn

        var initialErrorMessage: String? = null

        if (seatSelectionData == null || seatSelectionData.flight.isEmpty()) {
            Log.e("SeatSelectionActivity", "onCreate: Seat data is null or flights list is empty.")
            initialErrorMessage = "Không thể tải dữ liệu sơ đồ ghế."
        } else if (isRoundTrip && seatSelectionData.flight.size < 2) {
            Log.w("SeatSelectionActivity", "onCreate: RoundTrip is true but only received ${seatSelectionData.flight.size} flight leg(s).")
            // Có thể không cần gán lỗi ở đây, UI sẽ tự xử lý việc chỉ có 1 chặng
        }

        setContent {
            // Giả sử bạn đã có MaterialTheme bao bọc ứng dụng
            MaterialTheme { // Thêm MaterialTheme nếu chưa có ở cấp ứng dụng
                SeatSelectionScreen_Sequential(
                    seatData = seatSelectionData,
                    isRoundTrip = isRoundTrip,
                    passengers = allPassengers,
                    initialErrorMessage = initialErrorMessage
                )
            }
        }
    }
}

// ==================================
// === Helper Functions & Enums =====
// ==================================

fun parseSeatInfo(seatNumber: String): Pair<String, Int?> {
    val parts = seatNumber.split('-')
    val type = parts.getOrNull(0)?.uppercase() ?: "UNKNOWN"
    val number = parts.getOrNull(1)?.toIntOrNull()
    if (number == null && !seatNumber.contains("-")) {
        val parsedNum = seatNumber.filter { it.isDigit() }.toIntOrNull()
        if (parsedNum != null) return Pair("STD", parsedNum)
    }
    return Pair(type, number ?: -1)
}

val seatComparator = Comparator<FlightSeatDTO> { s1, s2 ->
    val (type1, num1) = parseSeatInfo(s1.seatNumber)
    val (type2, num2) = parseSeatInfo(s2.seatNumber)
    val typeOrder = mapOf("PRM" to 0, "BUS" to 1, "STD" to 2, "UNKNOWN" to 3)
    val typeComparison = (typeOrder[type1] ?: 3).compareTo(typeOrder[type2] ?: 3)
    if (typeComparison != 0) return@Comparator typeComparison
    compareValues(num1, num2)
}

enum class SeatStatus { AVAILABLE, OCCUPIED, BLOCKED, SELECTED, UNAVAILABLE }
enum class SeatType { PREMIUM, BUSINESS, STANDARD, UNKNOWN }

// ==================================
// === Seat Item Composable =========
// ==================================
@Composable
fun SeatItem(
    modifier: Modifier = Modifier,
    seatInfo: FlightSeatDTO?,
    seatSize: Dp = 44.dp,
    fontSize: TextUnit = 12.sp,
    isSelected: Boolean,
    isActuallyAvailable: Boolean,
    isClickable: Boolean, // <<< THAM SỐ MỚI ĐỂ KIỂM SOÁT CLICKABLE >>>
    onClick: (FlightSeatDTO) -> Unit
) {
    val seatType = remember(seatInfo?.fareCode) {
        when (seatInfo?.fareCode?.uppercase()) {
            "PRM" -> SeatType.PREMIUM
            "BUS" -> SeatType.BUSINESS
            "STD" -> SeatType.STANDARD
            else -> SeatType.UNKNOWN
        }
    }

    val status = when {
        seatInfo == null -> SeatStatus.UNAVAILABLE
        isSelected -> SeatStatus.SELECTED
        isActuallyAvailable -> SeatStatus.AVAILABLE
        else -> SeatStatus.OCCUPIED
    }

    val themeColors = MaterialTheme.colorScheme

    val backgroundColor = when (status) {
        SeatStatus.SELECTED -> themeColors.primary
        SeatStatus.OCCUPIED -> themeColors.onSurface.copy(alpha = 0.38f)
        SeatStatus.BLOCKED -> themeColors.onSurface.copy(alpha = 0.2f)
        SeatStatus.UNAVAILABLE -> Color.Transparent
        SeatStatus.AVAILABLE -> when (seatType) {
            SeatType.PREMIUM -> themeColors.secondaryContainer.copy(alpha = 0.7f)
            SeatType.BUSINESS -> themeColors.tertiaryContainer.copy(alpha = 0.7f)
            SeatType.STANDARD, SeatType.UNKNOWN -> themeColors.surfaceVariant
        }
    }

    val contentColor = when (status) {
        SeatStatus.SELECTED -> themeColors.onPrimary
        SeatStatus.OCCUPIED, SeatStatus.BLOCKED -> themeColors.surface.copy(alpha = 0.7f)
        SeatStatus.UNAVAILABLE -> Color.Transparent
        SeatStatus.AVAILABLE -> when (seatType) {
            SeatType.PREMIUM -> themeColors.onSecondaryContainer
            SeatType.BUSINESS -> themeColors.onTertiaryContainer
            SeatType.STANDARD, SeatType.UNKNOWN -> themeColors.onSurfaceVariant
        }
    }

    val border = when (status) {
        SeatStatus.SELECTED -> BorderStroke(2.dp, themeColors.primary)
        SeatStatus.AVAILABLE -> BorderStroke(1.dp, contentColor.copy(alpha = 0.5f))
        else -> null
    }

    val seatLabel = remember(seatInfo?.seatNumber) {
        seatInfo?.seatNumber?.substringAfter('-', "")?.filter { it.isDigit() }
            ?: seatInfo?.seatNumber?.filter { it.isDigit() } ?: ""
    }

    // Thêm alpha để làm mờ ghế nếu không click được
    val contentAlpha = if (isClickable) 1f else 0.5f

    Surface(
        modifier = modifier
            .size(seatSize)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                // Sử dụng isClickable và kiểm tra trạng thái hợp lệ
                enabled = isClickable && (status == SeatStatus.AVAILABLE || status == SeatStatus.SELECTED),
                onClick = { if (seatInfo != null) onClick(seatInfo) }
            ),
        color = backgroundColor.copy(alpha = backgroundColor.alpha * contentAlpha), // Làm mờ màu nền
        shape = RoundedCornerShape(8.dp),
        border = border,
        shadowElevation = if (status == SeatStatus.AVAILABLE && isClickable) 2.dp else 0.dp
    ) {
        if (status != SeatStatus.UNAVAILABLE && seatLabel.isNotEmpty()) {
            Box(contentAlignment = Alignment.Center) {
                val iconToShow = when (seatType) {
                    SeatType.PREMIUM -> Icons.Filled.StarOutline
                    SeatType.BUSINESS -> Icons.Filled.BusinessCenter
                    else -> null
                }
                if (iconToShow != null) {
                    Icon(
                        imageVector = iconToShow,
                        contentDescription = seatType.name,
                        tint = contentColor.copy(alpha = 0.4f * contentAlpha), // Làm mờ icon
                        modifier = Modifier.size(seatSize * 0.4f)
                    )
                }
                Text(
                    text = seatLabel,
                    color = contentColor.copy(alpha = contentColor.alpha * contentAlpha), // Làm mờ chữ
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}
// ==================================
// === Chú thích Ghế (Legend) =======
// ==================================
@OptIn(ExperimentalLayoutApi::class) // Cần cho FlowRow
@Composable
fun SeatLegend() {
    val themeColors = MaterialTheme.colorScheme
    data class LegendItem(val label: String, val color: Color, val borderColor: Color = color, val textColor: Color = themeColors.onSurface)

    val legendItems = listOfNotNull(
        LegendItem("Còn trống", themeColors.surfaceVariant, themeColors.outline, themeColors.onSurfaceVariant),
        LegendItem("Đang chọn", themeColors.primary, themeColors.primary, themeColors.onPrimary),
        LegendItem("Đã bị chiếm", themeColors.onSurface.copy(alpha = 0.38f), textColor = themeColors.surface.copy(alpha = 0.7f)),
        if (SeatType.PREMIUM != SeatType.UNKNOWN) LegendItem("Premium", themeColors.secondaryContainer.copy(alpha=0.7f), textColor = themeColors.onSecondaryContainer) else null,
        if (SeatType.BUSINESS != SeatType.UNKNOWN) LegendItem("Business", themeColors.tertiaryContainer.copy(alpha=0.7f), textColor = themeColors.onTertiaryContainer) else null
    )

    Text("Chú thích:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp, top = 8.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        legendItems.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(item.color, RoundedCornerShape(4.dp))
                        .border(1.dp, item.borderColor, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(item.label, style = MaterialTheme.typography.labelMedium, color = themeColors.onSurfaceVariant)
            }
        }
    }
}


// ==================================
// === Main Screen Composable =======
// ==================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SeatSelectionScreen_Sequential(
    seatData: SeatSelectionData?,
    isRoundTrip: Boolean,
    passengers: List<PassengerModel>,
    initialErrorMessage: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val flightLegs = seatData?.flight ?: emptyList()

    var currentPassengerIndex by rememberSaveable { mutableStateOf(0) }
    var currentLegIndex by rememberSaveable { mutableStateOf(0) }
    var currentErrorMessage by remember { mutableStateOf<String?>(null) }
    var currentSessionToken by remember { mutableStateOf(FlightBookingModel.sessionToken) }

    // State lưu lựa chọn TẠM THỜI trên UI (để tương tác chọn/bỏ chọn)
    val seatAssignments = remember {
        mutableStateMapOf<String, MutableMap<Int, String>>()
        // Không khởi tạo từ FlightBookingModel nữa, sẽ đồng bộ trong LaunchedEffect
    }

    // State lưu lựa chọn ĐÃ XÁC NHẬN (source of truth từ lần load hoặc sau khi confirm)
    val confirmedSeatAssignments = remember {
        mutableStateMapOf<String, MutableMap<Int, String>>().apply {
            FlightBookingModel.seatAssignmentsPerFlight.forEach { (flightNum, assignments) ->
                this[flightNum] = assignments.toMutableMap()
            }
        }
    }

    // --- State quản lý chế độ Xem/Hủy hay Chọn mới ---
    var isInViewMode by remember { mutableStateOf(false) } // Sẽ được đặt trong LaunchedEffect

    // --- Xác định thông tin cho bước hiện tại ---
    val currentPassenger = passengers.getOrNull(currentPassengerIndex)
    val currentFlightInfo = flightLegs.getOrNull(currentLegIndex)
    val currentFlightNumber = currentFlightInfo?.flightNumber ?: ""
    val isOutboundLeg = currentLegIndex == 0
    val hasReturnLeg = isRoundTrip && flightLegs.size >= 2

    val sortedSeats = remember(currentFlightInfo) {
        currentFlightInfo?.flightSeats?.sortedWith(seatComparator) ?: emptyList()
    }

    val themeColors = MaterialTheme.colorScheme

    // --- Khởi tạo/Cập nhật chế độ và UI khi bước thay đổi ---
    LaunchedEffect(currentPassengerIndex, currentLegIndex, confirmedSeatAssignments) {
        Log.d(
            "SequentialSeat",
            "LaunchedEffect: Step changed to Pax $currentPassengerIndex, Leg $currentLegIndex."
        )
        val confirmedSeat =
            confirmedSeatAssignments[currentFlightNumber]?.get(currentPassengerIndex)
        isInViewMode = (confirmedSeat != null)
        Log.d("SequentialSeat", "LaunchedEffect: isInViewMode set to: $isInViewMode")

        // Đồng bộ seatAssignments (UI state) với trạng thái đúng
        val currentLegAssignments =
            seatAssignments.getOrPut(currentFlightNumber) { mutableStateMapOf() }
        val currentUISelection = currentLegAssignments.get(currentPassengerIndex)

        if (isInViewMode) {
            // Đang ở chế độ View, UI phải hiển thị ghế đã confirm
            if (currentUISelection != confirmedSeat) {
                currentLegAssignments[currentPassengerIndex] = confirmedSeat!!
                seatAssignments[currentFlightNumber] =
                    currentLegAssignments // Trigger recomposition
                Log.d(
                    "SequentialSeat",
                    "LaunchedEffect: UI state synced to show confirmed seat: $confirmedSeat"
                )
            }
        } else {
            // Không ở chế độ View, nếu UI đang hiển thị gì đó (ví dụ sót lại từ bước trước) thì xóa đi
            // Trừ khi nó là lựa chọn người dùng vừa thực hiện (không cần xử lý ở đây)
            if (currentUISelection != null && confirmedSeat == null) {
                // Trường hợp này ít xảy ra nếu logic đúng, nhưng để đảm bảo
                Log.d(
                    "SequentialSeat",
                    "LaunchedEffect: Clearing stale UI selection $currentUISelection"
                )
                currentLegAssignments.remove(currentPassengerIndex)
                seatAssignments[currentFlightNumber] = currentLegAssignments
            }
        }
        // Xóa lỗi cũ khi chuyển bước
        currentErrorMessage = null
    }

    // --- Hàm Helper để gọi API Release Seat ---
    suspend fun releaseSeatInternal(
        seatToRelease: String,
        passenger: PassengerModel?,
        flightNumber: String,
        sessionToken: String?,
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        if (passenger?.uuid == null || sessionToken == null) {
            Log.e("SequentialSeat", "Release failed: Invalid passenger UUID or session token.")
            onResult(false, "Thông tin hành khách hoặc phiên không hợp lệ.")
            return
        }

        // Dùng lại ReserveSeatRequest nếu cấu trúc body API release yêu cầu giống
        val request = ReserveSeatRequest(
            flightNumber = flightNumber,
            seatNumber = seatToRelease,
            passengerUUID = passenger.uuid,
            sessionToken = sessionToken
        )
        Log.i("SequentialSeat", "Gọi API hủy ghế: $request")
        try {
            val response = RetrofitInstance.seatApi.releaseSeat(
                authorization = UserModel.token ?: "",
                request = request
            )
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.status == 200 || response.code() == 204 || response.code() == 200) {
                    onResult(true, apiResponse?.message ?: "Hủy ghế thành công.")
                } else {
                    onResult(
                        false,
                        "Lỗi ${apiResponse?.status}: ${apiResponse?.message ?: "Không thể hủy ghế."}"
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                onResult(false, "Lỗi ${response.code()} khi hủy ghế.")
            }
        } catch (e: Exception) {
            onResult(false, "Lỗi mạng hoặc xử lý khi hủy ghế.")
        }
    }

    fun moveToNextStep() {
        // Chỉ chuyển index, LaunchedEffect sẽ xử lý cập nhật mode và UI
        currentErrorMessage = null
        var nextPaxIndex = currentPassengerIndex
        var nextLegIndex = currentLegIndex

        if (isOutboundLeg && hasReturnLeg) {
            nextLegIndex = 1
            Log.d("SequentialSeat", "Moving to Return Leg for Pax $currentPassengerIndex")
        } else {
            if (currentPassengerIndex < passengers.size - 1) {
                nextPaxIndex++
                nextLegIndex = 0
                Log.d("SequentialSeat", "Moving to Next Pax $currentPassengerIndex")
            } else {
                // === HOÀN TẤT ===
                Log.i("SequentialSeat", "=== ALL STEPS COMPLETED ===")
                // ... (Chuẩn bị và trả kết quả về Activity trước dựa trên confirmedSeatAssignments) ...
                val resultDataList = mutableListOf<SelectedSeatInfoForLeg>()
                flightLegs.forEach { flightInfoData ->
                    val flightNum = flightInfoData.flightNumber
                    val assignmentsForThisLeg: Map<Int, String> =
                        confirmedSeatAssignments[flightNum]?.toMap() ?: emptyMap()
                    if (assignmentsForThisLeg.isNotEmpty()) {
                        resultDataList.add(
                            SelectedSeatInfoForLeg(
                                flightNumber = flightNum,
                                originCode = flightInfoData.route?.originAirport?.airportCode,
                                destinationCode = flightInfoData.route?.destinationAirport?.airportCode,
                                seatsByPassengerIndex = assignmentsForThisLeg
                            )
                        )
                    }
                }
                val finalResult = SeatSelectionResult(selectedSeatsForAllLegs = resultDataList)
                val resultIntent = Intent().putExtra("SEAT_SELECTION_RESULT", finalResult)
                FlightBookingModel.sessionToken = currentSessionToken
                FlightBookingModel.seatAssignmentsPerFlight =
                    confirmedSeatAssignments.toMap() // Lưu trạng thái confirmed cuối cùng
                (context as? ComponentActivity)?.apply {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                return // Quan trọng: Thoát hàm sau khi finish
            }
        }
        // Cập nhật state indices để kích hoạt LaunchedEffect cho bước mới
        currentPassengerIndex = nextPaxIndex
        currentLegIndex = nextLegIndex
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Chỗ Ngồi") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.primary,
                    titleContentColor = themeColors.onPrimary,
                    navigationIconContentColor = themeColors.onPrimary
                )
            )
        },
        containerColor = themeColors.surfaceContainerLowest // Nền nhẹ nhàng
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // --- Card thông tin bước ---
                if (currentPassenger != null && currentFlightInfo != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // --- Thông tin Bước ---
                            val stepDescription = if (isOutboundLeg) "chuyến đi" else "chuyến về"
                            Text(
                                "Bước ${currentPassengerIndex * (if(hasReturnLeg) 2 else 1) + (currentLegIndex + 1)}/${passengers.size * (if(hasReturnLeg) 2 else 1)}: Chọn ghế $stepDescription",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.primary,
                                modifier = Modifier.padding(bottom = 12.dp) // Tăng khoảng cách dưới
                            )

                            // --- THÔNG TIN HÀNH KHÁCH ĐANG CHỌN (Làm nổi bật) ---
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp) // Tăng khoảng cách dưới
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle, // Icon người dùng rõ hơn
                                    contentDescription = "Hành khách",
                                    tint = MaterialTheme.colorScheme.primary, // Dùng màu primary
                                    modifier = Modifier
                                        .size(32.dp) // Icon lớn hơn
                                        .padding(end = 12.dp) // Khoảng cách lớn hơn
                                )
                                Column { // Nhóm label và tên
                                    Text(
                                        "Đang chọn cho hành khách:",
                                        style = MaterialTheme.typography.labelMedium, // Label nhỏ hơn
                                        color = themeColors.onSurfaceVariant
                                    )
                                    Text(
                                        // Tên hành khách to và rõ hơn
                                        text = "${currentPassenger.firstName} ${currentPassenger.lastName}",
                                        style = MaterialTheme.typography.titleMedium, // Dùng titleMedium
                                        fontWeight = FontWeight.SemiBold // Đậm vừa phải
                                    )
                                }
                            }
                            // --- KẾT THÚC THÔNG TIN HÀNH KHÁCH ---


                            // --- Thông tin Chuyến bay ---
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp) // Tăng khoảng cách dưới
                            ) {
                                Icon(
                                    Icons.Filled.FlightTakeoff,
                                    contentDescription = "Chuyến bay",
                                    tint = themeColors.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    "${currentFlightInfo.route?.originAirport?.airportCode ?: ""} \u2708 ${currentFlightInfo.route?.destinationAirport?.airportCode ?: ""} (${currentFlightNumber})",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            // --- Thông tin Ghế đã chọn ---
                            val assignedSeatThisStep = seatAssignments[currentFlightNumber]?.get(currentPassengerIndex)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.AirlineSeatReclineNormal,
                                    contentDescription = "Ghế đã chọn",
                                    tint = if (assignedSeatThisStep != null) themeColors.primary else themeColors.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Ghế đã chọn: ", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = assignedSeatThisStep ?: "Chưa chọn",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (assignedSeatThisStep != null) themeColors.primary else themeColors.onSurfaceVariant
                                )
                            }
                        } // End Column trong Card
                    } // End Card
                }
                // Hiển thị lỗi API (nếu có)
                if (currentErrorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.errorContainer
                    ) {
                        Text(
                            text = currentErrorMessage!!,
                            color = themeColors.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                // Phần chú thích ghế
                Column(modifier = Modifier.padding(horizontal = 16.dp)) { // Thêm padding cho legend
                    SeatLegend()
                }


                // --- Sơ đồ ghế (LazyVerticalGrid) ---
                if (sortedSeats.isNotEmpty() && currentFlightInfo != null) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            .background(themeColors.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, themeColors.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .graphicsLayer(alpha = if (isInViewMode) 0.6f else 1f)
                    ) {
                        // ... (Logic hiển thị header) ...
                        sortedSeats.forEachIndexed { index, seatDto ->
                            // Thêm khoảng trắng sau mỗi 6 ghế, bắt đầu từ ghế thứ 3
                            if (index > 0 && (index + 3) % 6 == 0) {
                                item(span = { GridItemSpan(1) }) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                            }
                            
                            item(key = "${currentFlightNumber}_${seatDto.id}") {
                                val assignedSeatForThisStep =
                                    seatAssignments[currentFlightNumber]?.get(currentPassengerIndex)
                                val isSelectedOnUI = assignedSeatForThisStep == seatDto.seatNumber

                                val paxIndexWhoConfirmedThisSeat =
                                    confirmedSeatAssignments[currentFlightNumber]?.entries?.find { it.value == seatDto.seatNumber }?.key
                                val isConfirmedByOther =
                                    paxIndexWhoConfirmedThisSeat != null && paxIndexWhoConfirmedThisSeat != currentPassengerIndex

                                val isAvailableFromAPI =
                                    seatDto.seatStatus.equals("AVAILABLE", ignoreCase = true)
                                // Khả dụng để chọn = API Available VÀ chưa ai confirm
                                val isAvailableToSelect =
                                    isAvailableFromAPI && paxIndexWhoConfirmedThisSeat == null

                                SeatItem(
                                    seatInfo = seatDto,
                                    isSelected = isSelectedOnUI,
                                    isActuallyAvailable = isAvailableToSelect,
                                    isClickable = !isInViewMode,
                                    onClick = { clickedSeat ->
                                        // Logic onClick đơn giản chỉ cập nhật seatAssignments (state tạm thời)
                                        if (isAvailableToSelect || isSelectedOnUI) { // Chỉ cho phép toggle nếu ghế đó hợp lệ để chọn hoặc đang được chọn
                                            currentErrorMessage = null
                                            val currentLegAssignments =
                                                seatAssignments.getOrPut(currentFlightNumber) { mutableStateMapOf() }
                                            if (isSelectedOnUI) {
                                                currentLegAssignments.remove(currentPassengerIndex)
                                            } else {
                                                currentLegAssignments[currentPassengerIndex] =
                                                    clickedSeat.seatNumber
                                            }
                                            seatAssignments[currentFlightNumber] =
                                                currentLegAssignments
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Ghế ${clickedSeat.seatNumber} không khả dụng.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) // Kết thúc SeatItem
                            } // Kết thúc item
                        } // Kết thúc forEach
                    } // Kết thúc LazyVerticalGrid
                }
                // --- Nút xác nhận ---
                if (currentPassenger != null && currentFlightInfo != null) {
                    val confirmedSeat =
                        confirmedSeatAssignments[currentFlightNumber]?.get(currentPassengerIndex)
                    val seatSelectedTemporarily =
                        seatAssignments[currentFlightNumber]?.get(currentPassengerIndex)

                    // Xác định loại nút và hành động
                    val buttonActionType = when {
                        isInViewMode -> "CANCEL" // Đang xem ghế đã confirm -> Hủy
                        seatSelectedTemporarily != null -> "CONFIRM_NEW" // Đã chọn ghế mới trên UI -> Xác nhận
                        else -> "IDLE" // Chưa chọn gì -> Nút bị disable
                    }

                    val buttonEnabled =
                        !isLoading && (buttonActionType == "CANCEL" || buttonActionType == "CONFIRM_NEW")

                    // --- NÚT CHÍNH ---
                    Button(
                        onClick = {
                            val passengerUUID = currentPassenger.uuid
                            if (currentSessionToken == null || passengerUUID == null) { /*...*/ return@Button
                            }

                            isLoading = true
                            currentErrorMessage = null
                            coroutineScope.launch {
                                try {
                                    // --- XỬ LÝ HỦY GHẾ ---
                                    if (buttonActionType == "CANCEL" && confirmedSeat != null) {
                                        releaseSeatInternal(
                                            confirmedSeat,
                                            currentPassenger,
                                            currentFlightNumber,
                                            currentSessionToken
                                        ) { success, message ->
                                            if (success) {
                                                // Xóa khỏi cả 2 state
                                                confirmedSeatAssignments[currentFlightNumber]?.remove(
                                                    currentPassengerIndex
                                                )
                                                seatAssignments[currentFlightNumber]?.remove(
                                                    currentPassengerIndex
                                                )
                                                isInViewMode = false // Chuyển sang chế độ chọn
                                            } else {
                                                currentErrorMessage =
                                                    message ?: "Không thể hủy ghế."
                                            }
                                            isLoading = false
                                        }
                                    }
                                    // --- XỬ LÝ XÁC NHẬN GHẾ MỚI ---
                                    else if (buttonActionType == "CONFIRM_NEW" && seatSelectedTemporarily != null) {
                                        val request = ReserveSeatRequest(
                                            flightNumber = currentFlightNumber,
                                            seatNumber = seatSelectedTemporarily,
                                            passengerUUID = passengerUUID,
                                            sessionToken = currentSessionToken!!
                                        )
                                        val response = RetrofitInstance.seatApi.reserveSeat(
                                            authorization = UserModel.token ?: "",
                                            request = request
                                        )

                                        if (response.isSuccessful && response.body() != null && response.body()!!.status == 200 && response.body()!!.data != null) {
                                            // Reserve thành công
                                            currentSessionToken =
                                                response.body()!!.data!!.sessionToken
                                            // Cập nhật state ĐÃ XÁC NHẬN
                                            val confirmedLegMap = confirmedSeatAssignments.getOrPut(
                                                currentFlightNumber
                                            ) { mutableStateMapOf() }
                                            confirmedLegMap[currentPassengerIndex] =
                                                seatSelectedTemporarily
                                            // Cập nhật state UI khớp
                                            val tempLegMap =
                                                seatAssignments.getOrPut(currentFlightNumber) { mutableStateMapOf() }
                                            tempLegMap[currentPassengerIndex] =
                                                seatSelectedTemporarily
                                            // Chuyển bước (LaunchedEffect sẽ tự đặt isInViewMode cho bước mới)
                                            moveToNextStep()
                                        } else {
                                            // Xử lý lỗi reserve (API hoặc HTTP)
                                            val errorBody = response.errorBody()?.string()
                                            val apiResponse =
                                                response.body() // Có thể null nếu lỗi HTTP
                                            Log.e(
                                                "SequentialSeat",
                                                "Xác nhận $seatSelectedTemporarily THẤT BẠI (HTTP ${response.code()} / API ${apiResponse?.status}): ${apiResponse?.message ?: errorBody}"
                                            )
                                            currentErrorMessage = when {
                                                response.code() == 409 -> "Rất tiếc, ghế $seatSelectedTemporarily vừa bị người khác chọn mất. Vui lòng chọn ghế khác."
                                                response.code() == 406 -> "Không thể chọn ghế $seatSelectedTemporarily. Ghế này không phù hợp."
                                                apiResponse?.message != null -> "Lỗi ${apiResponse.status}: ${apiResponse.message}"
                                                else -> "Lỗi ${response.code()} khi xác nhận ghế."
                                            }
                                            isLoading = false // Tắt loading ngay khi có lỗi
                                        }
                                    } else {
                                        isLoading =
                                            false // Trường hợp nút được click dù không nên (hiếm)
                                    }
                                } catch (e: Exception) {
                                    Log.e("SequentialSeat", "Exception trong onClick Button", e)
                                    currentErrorMessage = "Đã xảy ra lỗi không mong muốn."
                                    isLoading = false
                                }
                                // Nếu không phải gọi API Hủy (trường hợp reserve), isLoading sẽ tự tắt
                                // Nếu là Hủy, nó đã tắt trong callback
                            } // end coroutineScope
                            isLoading = false
                        }, // end onClick Button
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                            .height(52.dp),
                        enabled = buttonEnabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buttonActionType == "CANCEL") themeColors.error else themeColors.primary,
                            contentColor = if (buttonActionType == "CANCEL") themeColors.onError else themeColors.onPrimary,
                            disabledContainerColor = themeColors.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = themeColors.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        // ... (Nội dung nút: Loading hoặc Text dựa trên buttonActionType) ...
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            val buttonText = when (buttonActionType) {
                                "CANCEL" -> "Hủy ghế ${confirmedSeat ?: ""}"
                                "CONFIRM_NEW" -> if (isOutboundLeg) "Xác nhận ghế đi" else "Xác nhận ghế về"
                                else -> "Vui lòng chọn ghế"
                            }
                            Text(
                                buttonText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } // End Button
                } // End if (currentPassenger != null && currentFlightInfo != null)

            }
        }
    }
}

// ==================================
// === Preview ======================
// ==================================
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420")
@Composable
fun SeatSelectionScreen_Sequential_Preview() {
    val adults = listOf(
        PassengerModel(passengerType = "ADULT", firstName = "An", lastName = "Nguyen", uuid = "uuid-an"),
        PassengerModel(passengerType = "ADULT", firstName = "Binh", lastName = "Le", uuid = "uuid-binh")
    )
    val route1 = RouteDTO(originAirport = AirportDTO("SGN", "SGN"), destinationAirport = AirportDTO("HAN", "HAN"))
    val route2 = RouteDTO(originAirport = AirportDTO("HAN", "HAN"), destinationAirport = AirportDTO("SGN", "SGN"))

    val seats1 = mutableListOf<FlightSeatDTO>()
    val seatTypes = listOf("PRM", "BUS", "STD")
    var seatIdCounter = 1
    for (type in seatTypes) {
        for (row in 1.. (if(type == "STD") 5 else 2) ) {
            for (colChar in 'A'..'F') {
                if (type == "PRM" && (colChar == 'C' || colChar == 'D')) continue // PRM không có C, D
                if (type == "BUS" && (colChar == 'C' || colChar == 'D')) continue // BUS không có C, D
                val status = when ((row + colChar.code) % 4) {
                    0 -> "AVAILABLE"
                    1 -> "OCCUPIED"
                    2 -> "AVAILABLE"
                    else -> "BLOCKED"
                }
                seats1.add(FlightSeatDTO(seatIdCounter++, "$type-$row$colChar", "FL100", type, status))
            }
        }
    }

    val seats2 = mutableListOf<FlightSeatDTO>()
    seatIdCounter = 100
    for (row in 1..4) {
        for (colChar in 'A'..'F') {
            seats2.add(FlightSeatDTO(seatIdCounter++, "STD-$row$colChar", "FL101", "STD", if ((row+colChar.code)%3 == 0) "OCCUPIED" else "AVAILABLE" ))
        }
    }

    val flightInfo1 = FlightSeatInfo("FL100", "Boeing 787", "2025-12-25T10:00:00", "2025-12-25T12:00:00", route1, seats1)
    val flightInfo2 = FlightSeatInfo("FL101", "Airbus A321", "2025-12-26T14:00:00", "2025-12-26T16:00:00", route2, seats2)

    val sampleSeatData = SeatSelectionData(sessionToken = "preview_token_123", flight = listOf(flightInfo1, flightInfo2))
    FlightBookingModel.sessionToken = "preview_token_123" // Mock token

    MaterialTheme { // Bao bọc Preview bằng MaterialTheme
        SeatSelectionScreen_Sequential(
            seatData = sampleSeatData,
            isRoundTrip = true,
            passengers = adults,
            initialErrorMessage = null // Hoặc "Đây là lỗi thử nghiệm ban đầu."
        )
    }
}