package com.vietjoke.vn.Activities.Booking // Đặt package phù hợp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos // Icon chevron iOS style
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.* // Sử dụng Material 3
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.Preview.PreviewActivity
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.SeatSelectionResult
import com.vietjoke.vn.model.LuggageSelectionResult
import com.vietjoke.vn.retrofit.ResponseDTO.AddonBookingItem
import com.vietjoke.vn.retrofit.ResponseDTO.BookAddonsRequest
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

// Data class để chứa thông tin cho mỗi lựa chọn
data class AncillaryOptionData(
    val icon: ImageVector, // Hoặc @DrawableRes Int nếu dùng drawable
    val iconBackgroundColor: Color = Color(0xFFFFEBEE), // Màu nền icon mặc định (hồng nhạt)
    val title: String,
    val subtitle: String? = null,
    val price: String? = null,
    val details: @Composable (() -> Unit)? = null, // Composable cho chi tiết (vd: SGN-BMV)
    val onClick: () -> Unit
)

class AncillaryActivity : ComponentActivity() {
    private val seatSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Lấy tham chiếu đến state của Composable (được cập nhật qua callback onStateCreated)
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "SEAT_SELECTION_RESULT" // Key phải khớp với key đã đặt ở SeatSelectionActivity

            val seatResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, SeatSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? SeatSelectionResult
            }

            if (seatResult != null) {
                Log.i("AncillaryActivity", "Nhận kết quả chọn ghế: ${seatResult.selectedSeatsForAllLegs}")
                currentAncillaryState?.selectedSeatsResult = seatResult // Cập nhật state trong Composable
            } else {
                Log.w("AncillaryActivity", "Nhận RESULT_OK nhưng không có dữ liệu kết quả.")
                currentAncillaryState?.selectedSeatsResult = null // Xóa kết quả cũ nếu có
            }
        } else {
            Log.w("AncillaryActivity", "Chọn ghế bị hủy/thất bại. Code: ${result.resultCode}")
        }
    }

    private val luggageSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "LUGGAGE_SELECTION_RESULT"
            val luggageResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, LuggageSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? LuggageSelectionResult
            }
            if (luggageResult != null) {
                currentAncillaryState?.selectedLuggageResult = luggageResult
            } else {
                currentAncillaryState?.selectedLuggageResult = null
            }
        }
    }

    private val insuranceSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "INSURANCE_SELECTION_RESULT"
            val insuranceResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, com.vietjoke.vn.model.InsuranceSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? com.vietjoke.vn.model.InsuranceSelectionResult
            }
            if (insuranceResult != null) {
                currentAncillaryState?.selectedInsuranceResult = insuranceResult
            } else {
                currentAncillaryState?.selectedInsuranceResult = null
            }
        }
    }

    private val otherServiceSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "OTHER_SERVICE_SELECTION_RESULT"
            val otherResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, com.vietjoke.vn.model.OtherServiceSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? com.vietjoke.vn.model.OtherServiceSelectionResult
            }
            if (otherResult != null) {
                currentAncillaryState?.selectedOtherServiceResult = otherResult
            } else {
                currentAncillaryState?.selectedOtherServiceResult = null
            }
        }
    }

    private val mealSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "MEAL_SELECTION_RESULT"
            val mealResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, com.vietjoke.vn.model.MealSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? com.vietjoke.vn.model.MealSelectionResult
            }
            if (mealResult != null) {
                currentAncillaryState?.selectedMealsResult = mealResult
            } else {
                currentAncillaryState?.selectedMealsResult = null
            }
        }
    }

    private val drinkSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val currentAncillaryState = ancillaryScreenState
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val resultKey = "DRINK_SELECTION_RESULT"
            val drinkResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra(resultKey, com.vietjoke.vn.model.DrinkSelectionResult::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(resultKey) as? com.vietjoke.vn.model.DrinkSelectionResult
            }
            if (drinkResult != null) {
                currentAncillaryState?.selectedDrinksResult = drinkResult
            } else {
                currentAncillaryState?.selectedDrinksResult = null
            }
        }
    }

    // Biến tạm để giữ tham chiếu đến state của Composable
    private var ancillaryScreenState: AncillaryScreenState? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AncillaryScreen(
                seatLauncher = { intent ->
                    Log.d("AncillaryActivity", "Launching SeatSelectionActivity...")
                    seatSelectionLauncher.launch(intent)
                },
                luggageLauncher = { intent ->
                    luggageSelectionLauncher.launch(intent)
                },
                insuranceLauncher = { intent ->
                    insuranceSelectionLauncher.launch(intent)
                },
                otherServiceLauncher = { intent ->
                    otherServiceSelectionLauncher.launch(intent)
                },
                mealLauncher = { intent ->
                    mealSelectionLauncher.launch(intent)
                },
                drinkLauncher = { intent ->
                    drinkSelectionLauncher.launch(intent)
                },
                onStateCreated = { state ->
                    Log.d("AncillaryActivity", "AncillaryScreen state holder received.")
                    ancillaryScreenState = state
                }
            )
        }
    }
}

class AncillaryScreenState(
    selectedSeats: SeatSelectionResult? = null,
    selectedLuggage: LuggageSelectionResult? = null,
    selectedInsurance: com.vietjoke.vn.model.InsuranceSelectionResult? = null,
    selectedOtherService: com.vietjoke.vn.model.OtherServiceSelectionResult? = null,
    selectedMeals: com.vietjoke.vn.model.MealSelectionResult? = null,
    selectedDrinks: com.vietjoke.vn.model.DrinkSelectionResult? = null
) {
    var selectedSeatsResult by mutableStateOf(selectedSeats)
    var selectedLuggageResult by mutableStateOf(selectedLuggage)
    var selectedInsuranceResult by mutableStateOf(selectedInsurance)
    var selectedOtherServiceResult by mutableStateOf(selectedOtherService)
    var selectedMealsResult by mutableStateOf(selectedMeals)
    var selectedDrinksResult by mutableStateOf(selectedDrinks)
}

@Composable
fun rememberAncillaryScreenState(
    initialSeats: SeatSelectionResult? = null,
    initialLuggage: LuggageSelectionResult? = null,
    initialInsurance: com.vietjoke.vn.model.InsuranceSelectionResult? = null,
    initialOtherService: com.vietjoke.vn.model.OtherServiceSelectionResult? = null,
    initialMeals: com.vietjoke.vn.model.MealSelectionResult? = null,
    initialDrinks: com.vietjoke.vn.model.DrinkSelectionResult? = null
): AncillaryScreenState {
    return rememberSaveable(saver = AncillaryScreenStateSaver) {
        AncillaryScreenState(initialSeats, initialLuggage, initialInsurance, initialOtherService, initialMeals, initialDrinks)
    }
}

val AncillaryScreenStateSaver: Saver<AncillaryScreenState, Any> = mapSaver(
    save = {
        mapOf(
            "selectedSeatsResult_v1" to it.selectedSeatsResult,
            "selectedLuggageResult_v1" to it.selectedLuggageResult,
            "selectedInsuranceResult_v1" to it.selectedInsuranceResult,
            "selectedOtherServiceResult_v1" to it.selectedOtherServiceResult,
            "selectedMealsResult_v1" to it.selectedMealsResult,
            "selectedDrinksResult_v1" to it.selectedDrinksResult
        )
    },
    restore = { savedMap ->
        AncillaryScreenState(
            savedMap["selectedSeatsResult_v1"] as? SeatSelectionResult,
            savedMap["selectedLuggageResult_v1"] as? LuggageSelectionResult,
            savedMap["selectedInsuranceResult_v1"] as? com.vietjoke.vn.model.InsuranceSelectionResult,
            savedMap["selectedOtherServiceResult_v1"] as? com.vietjoke.vn.model.OtherServiceSelectionResult,
            savedMap["selectedMealsResult_v1"] as? com.vietjoke.vn.model.MealSelectionResult,
            savedMap["selectedDrinksResult_v1"] as? com.vietjoke.vn.model.DrinkSelectionResult
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AncillaryScreen(
    seatLauncher: (Intent) -> Unit,
    luggageLauncher: (Intent) -> Unit,
    insuranceLauncher: (Intent) -> Unit,
    otherServiceLauncher: (Intent) -> Unit,
    mealLauncher: (Intent) -> Unit,
    drinkLauncher: (Intent) -> Unit,
    onStateCreated: (AncillaryScreenState) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val screenState = rememberAncillaryScreenState()
    LaunchedEffect(screenState) {
        onStateCreated(screenState)
    }
    val ancillaryOptions = remember(screenState.selectedSeatsResult, screenState.selectedLuggageResult, screenState.selectedInsuranceResult, screenState.selectedOtherServiceResult, screenState.selectedMealsResult, screenState.selectedDrinksResult) {
        Log.d("AncillaryScreen", "Rebuilding ancillary options. Selected seats: ${screenState.selectedSeatsResult != null}, Selected luggage: ${screenState.selectedLuggageResult != null}, Selected insurance: ${screenState.selectedInsuranceResult != null}, Selected other service: ${screenState.selectedOtherServiceResult != null}")
        buildList {
            // --- Mục Chọn chỗ ngồi ---
            val seatSelectionOption = AncillaryOptionData(
                icon = Icons.Filled.AirlineSeatReclineNormal,
                iconBackgroundColor = Color(0xFFFFEBEE), // Hồng nhạt
                title = "Chọn chỗ ngồi yêu thích",
                // Subtitle và Details thay đổi dựa trên kết quả chọn ghế
                subtitle = if (screenState.selectedSeatsResult == null)
                    "Hãy chọn chỗ ngồi yêu thích của bạn" // Mặc định khi chưa chọn
                else null, // Ẩn subtitle mặc định khi đã có kết quả
                details = { // Composable hiển thị chi tiết ghế đã chọn
                    // Chỉ hiển thị nếu có kết quả và danh sách chặng không rỗng
                    screenState.selectedSeatsResult?.selectedSeatsForAllLegs?.takeIf { it.isNotEmpty() }?.let { legs ->
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            legs.forEach { legInfo ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween, // Đẩy route và ghế ra 2 bên
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hiển thị Route (SGN -> BMV)
                                    Text(
                                        text = "${legInfo.originCode ?: "?"} \u2708 ${legInfo.destinationCode ?: "?"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                    // Hiển thị danh sách ghế (8-B, 8-C)
                                    Text(
                                        text = legInfo.seatsByPassengerIndex.values.joinToString(", "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9C27B0) // Màu tím cho ghế
                                    )
                                }
                                Spacer(Modifier.height(2.dp)) // Khoảng cách giữa các chặng nếu có nhiều
                            }
                        }
                    }
                },
                onClick = { // Xử lý khi nhấn vào mục chọn ghế
                    val currentSessionTokenOnClick = FlightBookingModel.sessionToken
                    if (currentSessionTokenOnClick == null) {
                        Toast.makeText(context, "Lỗi: Session token không hợp lệ.", Toast.LENGTH_SHORT).show()
                        return@AncillaryOptionData // Thoát lambda onClick
                    }

                    isLoading = true // Hiển thị loading
                    coroutineScope.launch {
                        try {
                            Log.d("AncillaryScreen", "Đang gọi API getSeats...")
                            // Gọi API lấy dữ liệu sơ đồ ghế
                            val response = RetrofitInstance.flightApi.getSeats(token = currentSessionTokenOnClick)
                            Log.d("AncillaryScreen", "API getSeats response: Code=${response.code()}, Success=${response.isSuccessful}")

                            if (response.isSuccessful && response.body() != null) {
                                val apiResponse = response.body()!!
                                if (apiResponse.status == 200 && apiResponse.data != null) {
                                    // Thành công, cập nhật token và chuẩn bị Intent
                                    FlightBookingModel.sessionToken = apiResponse.data.sessionToken
                                    Log.d("AncillaryScreen", "API getSeats thành công. Chuẩn bị mở SeatSelectionActivity.")

                                    val intent = Intent(context, SeatSelectionActivity::class.java).apply {
                                        // Truyền dữ liệu cần thiết cho SeatSelectionActivity
                                        putExtra("seatSelectionData", apiResponse.data) // SeatSelectionData phải Serializable
                                        putExtra("isRoundTrip", FlightBookingModel.isRoundTrip)
                                        // Có thể truyền thêm thông tin hành khách nếu cần,
                                        // nhưng hiện tại SeatSelectionActivity đọc từ FlightBookingModel
                                    }
                                    // !!! GỌI ACTIVITY QUA LAUNCHER !!!
                                    seatLauncher(intent)
                                } else {
                                    // API báo lỗi logic
                                    Log.w("AncillaryScreen", "API getSeats báo lỗi: Status=${apiResponse.status}, Msg=${apiResponse.message}")
                                    Toast.makeText(context, apiResponse.message ?: "Lỗi khi lấy dữ liệu ghế.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Lỗi HTTP
                                val errorBody = response.errorBody()?.string()
                                Log.e("AncillaryScreen", "Lỗi HTTP khi getSeats: Code=${response.code()}, Body: $errorBody")
                                Toast.makeText(context, "Lỗi ${response.code()} khi lấy sơ đồ ghế.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            // Lỗi mạng hoặc lỗi khác
                            Log.e("AncillaryScreen", "Exception khi getSeats", e)
                            Toast.makeText(context, "Lỗi mạng hoặc xử lý: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false // Ẩn loading
                        }
                    }
                } // Kết thúc onClick
            ) // Kết thúc AncillaryOptionData cho chọn ghế
            add(seatSelectionOption)
            add(AncillaryOptionData(
                icon = Icons.Filled.Luggage,
                iconBackgroundColor = Color(0xFFFFF9C4), // Vàng nhạt
                title = "Chọn hành lý/Dịch vụ nối chuyến",
                price = screenState.selectedLuggageResult?.selectedLuggageForAllLegs?.sumOf { leg ->
                    leg.luggageByPassengerIndex.values.flatten().sumOf { it.price }
                }?.let { if (it > 0) String.format("%,.0f VND", it) else null },
                details = {
                    val legs = screenState.selectedLuggageResult?.selectedLuggageForAllLegs
                    if (!legs.isNullOrEmpty()) {
                        Row(Modifier.fillMaxWidth()) {
                            legs.forEach { leg ->
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${leg.originCode} \u2708 ${leg.destinationCode}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    leg.luggageByPassengerIndex.values.flatten().forEach {
                                        Text("${it.name}", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                onClick = {
                    val intent = Intent(context, LuggageSelectionActivity::class.java)
                    luggageLauncher(intent)
                }
            ))
            add(AncillaryOptionData(
                icon = Icons.Filled.Shield, // Hoặc dùng ảnh như trong hình
                iconBackgroundColor = Color(0xFFFFEBEE), // Hồng nhạt
                title = "Bảo hiểm du lịch Vietjoke Travel Safe",
                price = screenState.selectedInsuranceResult?.selectedInsuranceForAllLegs?.sumOf { leg ->
                    leg.insuranceByPassengerIndex.values.flatten().sumOf { it.price }
                }?.let { if (it > 0) String.format("%,.0f VND", it) else null },
                details = {
                    val legs = screenState.selectedInsuranceResult?.selectedInsuranceForAllLegs
                    if (!legs.isNullOrEmpty()) {
                        Row(Modifier.fillMaxWidth()) {
                            legs.forEach { leg ->
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${leg.originCode} \u2708 ${leg.destinationCode}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    leg.insuranceByPassengerIndex.values.flatten().forEach {
                                        Text("${it.name}", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                onClick = {
                    val intent = Intent(context, InsuranceSelectionActivity::class.java)
                    insuranceLauncher(intent)
                }
            ))
            add(AncillaryOptionData(
                icon = Icons.Filled.MoreHoriz,
                iconBackgroundColor = Color(0xFFE3F2FD), // Xanh dương nhạt
                title = "Dịch vụ khác",
                price = screenState.selectedOtherServiceResult?.selectedServiceForAllLegs?.sumOf { leg ->
                    leg.serviceByPassengerIndex.values.flatten().sumOf { it.price }
                }?.let { if (it > 0) String.format("%,.0f VND", it) else null },
                details = {
                    val legs = screenState.selectedOtherServiceResult?.selectedServiceForAllLegs
                    if (!legs.isNullOrEmpty()) {
                        Row(Modifier.fillMaxWidth()) {
                            legs.forEach { leg ->
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${leg.originCode} \u2708 ${leg.destinationCode}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    leg.serviceByPassengerIndex.values.flatten().forEach {
                                        Text("${it.name}", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                onClick = {
                    val intent = Intent(context, OtherServiceSelectionActivity::class.java)
                    otherServiceLauncher(intent)
                }
            ))
            // Thêm các lựa chọn khác nếu cần

            // Update meal selection option
            add(AncillaryOptionData(
                icon = Icons.Filled.Restaurant,
                iconBackgroundColor = Color(0xFFE3F2FD),
                title = "Đặt suất ăn",
                price = screenState.selectedMealsResult?.selectedMealsForAllLegs?.sumOf { leg ->
                    leg.itemsByPassengerIndex.values.flatten().sumOf { it.addon.price * it.quantity }
                }?.let { if (it > 0) String.format("%,.0f VND", it) else null },
                details = {
                    val legs = screenState.selectedMealsResult?.selectedMealsForAllLegs
                    if (!legs.isNullOrEmpty()) {
                        Column(Modifier.fillMaxWidth()) {
                            legs.forEach { leg ->
                                Text(
                                    "${leg.originCode} \u2708 ${leg.destinationCode}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                leg.itemsByPassengerIndex.forEach { (passengerIndex, items) ->
                                    val passenger = (FlightBookingModel.passengersAdult + FlightBookingModel.passengersChild).getOrNull(passengerIndex)
                                    passenger?.let {
                                        Text(
                                            "${it.passengerType.take(1)} ${it.firstName.take(1)} ${it.lastName.take(1)}:",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        items.forEach { item ->
                                            Text("- ${item.addon.name} x${item.quantity}", fontSize = 14.sp)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                },
                onClick = {
                    val intent = Intent(context, MealSelectionActivity::class.java)
                    mealLauncher(intent)
                }
            ))

            // Update drink selection option
            add(AncillaryOptionData(
                icon = Icons.Filled.LocalCafe,
                iconBackgroundColor = Color(0xFFE3F2FD),
                title = "Đặt đồ uống",
                price = screenState.selectedDrinksResult?.selectedDrinksForAllLegs?.sumOf { leg ->
                    leg.itemsByPassengerIndex.values.flatten().sumOf { it.addon.price * it.quantity }
                }?.let { if (it > 0) String.format("%,.0f VND", it) else null },
                details = {
                    val legs = screenState.selectedDrinksResult?.selectedDrinksForAllLegs
                    if (!legs.isNullOrEmpty()) {
                        Column(Modifier.fillMaxWidth()) {
                            legs.forEach { leg ->
                                Text(
                                    "${leg.originCode} \u2708 ${leg.destinationCode}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                leg.itemsByPassengerIndex.forEach { (passengerIndex, items) ->
                                    val passenger = (FlightBookingModel.passengersAdult + FlightBookingModel.passengersChild).getOrNull(passengerIndex)
                                    passenger?.let {
                                        Text(
                                            "${it.passengerType.take(1)} ${it.firstName.take(1)} ${it.lastName.take(1)}:",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        items.forEach { item ->
                                            Text("- ${item.addon.name} x${item.quantity}", fontSize = 14.sp)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                },
                onClick = {
                    val intent = Intent(context, DrinkSelectionActivity::class.java)
                    drinkLauncher(intent)
                }
            ))
        }
    }
    // --- Kết thúc dữ liệu mẫu ---

    Scaffold( // Sử dụng Scaffold để có cấu trúc màn hình chuẩn (tùy chọn)
        topBar = {
            // TopAppBar đơn giản (tùy chọn)
            TopAppBar(
                title = { Text("Dịch vụ bổ sung") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Xử lý nút back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0), // Màu tím
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5) // Màu nền chung
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Áp dụng padding từ Scaffold
                .padding(horizontal = 16.dp) // Padding ngang cho nội dung
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Khoảng cách từ TopAppBar

            // Header Text
            Text(
                text = "Đừng quên mua hành lý, suất ăn, chọn chỗ ngồi và hơn thế nữa...",
                color = Color(0xFF4CAF50), // Màu xanh lá cây
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // List of Options
            ancillaryOptions.forEach { optionData ->
                AncillaryOptionItem(
                    icon = optionData.icon,
                    iconBackgroundColor = optionData.iconBackgroundColor,
                    title = optionData.title,
                    subtitle = optionData.subtitle,
                    price = optionData.price,
                    details = optionData.details,
                    onClick = optionData.onClick
                )
                Spacer(modifier = Modifier.height(12.dp)) // Khoảng cách giữa các item
            }

            Spacer(modifier = Modifier.height(16.dp)) // Khoảng cách cuối trang

            // Nút tiếp tục
            Button(
                onClick = {
                    // Kiểm tra và gọi API book addons
                    val sessionToken = FlightBookingModel.sessionToken
                    if (sessionToken == null) {
                        Toast.makeText(context, "Lỗi: Session token không hợp lệ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            // Tạo danh sách request cho từng hành khách
                            val passengers = FlightBookingModel.passengersAdult + FlightBookingModel.passengersChild
                            var hasError = false

                            // Xử lý từng hành khách cho cả chuyến đi và về
                            val legs = if (FlightBookingModel.isRoundTrip) listOf(0, 1) else listOf(0)
                            for (leg in legs) {
                                val flightNumber = if (leg == 0) FlightBookingModel.flightNumber else FlightBookingModel.returnFlightNumber
                                if (flightNumber == null) continue

                                for (passenger in passengers) {
                                    val addons = mutableListOf<AddonBookingItem>()

                                    // Thêm các addon đã chọn cho chuyến đi
                                    screenState.selectedSeatsResult?.let { result ->
                                        result.selectedSeatsForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.seatsByPassengerIndex[passengers.indexOf(passenger)]?.let { seat ->
                                                // Thêm ghế vào addons nếu cần
                                            }
                                        }
                                    }

                                    screenState.selectedLuggageResult?.let { result ->
                                        result.selectedLuggageForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.luggageByPassengerIndex[passengers.indexOf(passenger)]?.forEach { luggage ->
                                                addons.add(AddonBookingItem(luggage.id, 1))
                                            }
                                        }
                                    }

                                    screenState.selectedInsuranceResult?.let { result ->
                                        result.selectedInsuranceForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.insuranceByPassengerIndex[passengers.indexOf(passenger)]?.forEach { insurance ->
                                                addons.add(AddonBookingItem(insurance.id, 1))
                                            }
                                        }
                                    }

                                    screenState.selectedOtherServiceResult?.let { result ->
                                        result.selectedServiceForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.serviceByPassengerIndex[passengers.indexOf(passenger)]?.forEach { service ->
                                                addons.add(AddonBookingItem(service.id, 1))
                                            }
                                        }
                                    }

                                    // Thêm suất ăn cho chuyến tương ứng
                                    screenState.selectedMealsResult?.let { result ->
                                        result.selectedMealsForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.itemsByPassengerIndex[passengers.indexOf(passenger)]?.forEach { meal ->
                                                addons.add(AddonBookingItem(meal.addon.id, meal.quantity))
                                            }
                                        }
                                    }

                                    // Thêm đồ uống cho chuyến tương ứng
                                    screenState.selectedDrinksResult?.let { result ->
                                        result.selectedDrinksForAllLegs.getOrNull(leg)?.let { legInfo ->
                                            legInfo.itemsByPassengerIndex[passengers.indexOf(passenger)]?.forEach { drink ->
                                                addons.add(AddonBookingItem(drink.addon.id, drink.quantity))
                                            }
                                        }
                                    }

                                    // Nếu có addon được chọn, gọi API
                                    if (addons.isNotEmpty()) {
                                        val request = BookAddonsRequest(
                                            sessionToken = sessionToken,
                                            flightNumber = flightNumber,
                                            passengerUuid = passenger.uuid,
                                            addons = addons
                                        )

                                        val response = RetrofitInstance.addonApi.bookAddons(request)
                                        if (response.isSuccessful && response.body() != null) {
                                            val apiResponse = response.body()!!
                                            if (apiResponse.status == 200) {
                                                // Cập nhật session token mới
                                                FlightBookingModel.sessionToken = apiResponse.data.sessionToken
                                            } else {
                                                hasError = true
                                                Toast.makeText(context, apiResponse.message, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            hasError = true
                                            Toast.makeText(context, "Lỗi khi đặt dịch vụ", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            if (!hasError) {
                                val response = RetrofitInstance.bookingApi.completeBooking(sessionToken)
                                if (response.isSuccessful && response.body() != null) {
                                    val apiResponse = response.body()!!
                                    if (apiResponse.status == 200) {
                                        Toast.makeText(context, "Đặt dịch vụ thành công", Toast.LENGTH_SHORT).show()
                                        // Navigate to Preview screen
                                        val intent = Intent(context, PreviewActivity::class.java)
                                        context.startActivity(intent)
                                        (context as? Activity)?.finish()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AncillaryActivity", "Error booking addons", e)
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Tiếp tục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}

@Composable
fun AncillaryOptionItem(
    icon: ImageVector,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String?,
    price: String?,
    details: @Composable (() -> Unit)?, // Composable cho phần chi tiết phụ
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Bo góc nhẹ
            .clickable(onClick = onClick), // Click vào cả Card
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Nền trắng
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Độ nổi nhẹ
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Padding bên trong Card
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background + Icon
            Box(
                modifier = Modifier
                    .size(48.dp) // Kích thước ô chứa icon
                    .clip(CircleShape) // Bo tròn
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title, // Mô tả icon
                    tint = Color(0xFF9C27B0), // Màu icon chính (tím)
                    modifier = Modifier.size(28.dp) // Kích thước icon
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Khoảng cách giữa icon và text

            // Column for Text Content
            Column(
                modifier = Modifier.weight(1f), // Chiếm không gian còn lại
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1, // Chỉ hiện 1 dòng
                    overflow = TextOverflow.Ellipsis // Thêm dấu ... nếu quá dài
                )
                // Hiển thị subtitle nếu có
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Hiển thị details nếu có
                details?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    it() // Gọi Composable chi tiết
                }
            }

            Spacer(modifier = Modifier.width(8.dp)) // Khoảng cách trước giá (nếu có)

            // Price (nếu có)
            price?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Red, // Màu đỏ cho giá
                    modifier = Modifier.padding(start = 8.dp) // Padding thêm nếu có giá
                )
            }

            // Chevron Icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, // Icon mũi tên qua phải
                contentDescription = "Select option",
                tint = Color.Gray, // Màu xám nhạt
                modifier = Modifier.size(16.dp).padding(start = 8.dp) // Tăng padding bên trái icon
            )
        }
    }
}