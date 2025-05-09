package com.vietjoke.vn.Activities.FlightList

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import đúng
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card // Material 2 Card
import androidx.compose.material.Divider // Material 2 Divider
import androidx.compose.material.IconButton // Material 2 IconButton
import androidx.compose.material.Text // Material 2 Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog // Material 3 Dialog
import androidx.compose.material3.Icon // Material 3 Icon
import androidx.compose.material3.TextButton // Material 3 TextButton
import androidx.compose.runtime.* // Import wildcard
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.model.FlightBookingModel // Đảm bảo model đã cập nhật
import com.vietjoke.vn.retrofit.ResponseDTO.FareClassDTO
import com.vietjoke.vn.retrofit.ResponseDTO.FlightResponseDTO
import kotlinx.coroutines.launch // For scrolling
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FlightListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IS_ROUND_TRIP = "isRoundTrip"
        const val EXTRA_FLIGHTS = "flights"
        const val EXTRA_OUTBOUND_FLIGHTS = "outboundFlights"
        const val EXTRA_RETURN_FLIGHTS = "returnFlights"
        const val EXTRA_SESSION_TOKEN = "sessionToken"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INTENT HANDLING ---
        val isRoundTrip = intent.getBooleanExtra(EXTRA_IS_ROUND_TRIP, false)
        val sessionToken = intent.getStringExtra(EXTRA_SESSION_TOKEN)

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val flights: List<FlightResponseDTO> = if (!isRoundTrip) {
            intent.getSerializableExtra(EXTRA_FLIGHTS) as? List<FlightResponseDTO> ?: emptyList()
        } else { emptyList() }

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val outboundFlights: List<FlightResponseDTO> = if (isRoundTrip) {
            intent.getSerializableExtra(EXTRA_OUTBOUND_FLIGHTS) as? List<FlightResponseDTO> ?: emptyList()
        } else { emptyList() }

        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val returnFlights: List<FlightResponseDTO> = if (isRoundTrip) {
            intent.getSerializableExtra(EXTRA_RETURN_FLIGHTS) as? List<FlightResponseDTO> ?: emptyList()
        } else { emptyList() }
        // --- END INTENT HANDLING ---

        if (isRoundTrip && (outboundFlights.isEmpty() || returnFlights.isEmpty())) {
            Toast.makeText(this, "Lỗi dữ liệu chuyến bay khứ hồi.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            FlightListScreen(
                isRoundTrip = isRoundTrip,
                initialFlights = flights,
                outboundFlights = outboundFlights,
                returnFlights = returnFlights,
                sessionToken = sessionToken
            )
        }
    }
}

// Type alias for clarity
typealias FlightSelection = Pair<FlightResponseDTO, String> // Flight and FareCode

// #region Helper Functions
@RequiresApi(Build.VERSION_CODES.O)
private fun extractTimeFromDateTime(dateTimeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "--:--" }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun extractDateFromDateTime(dateTimeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("MMM dd,yyyy"))
    } catch (e: Exception) { "Invalid Date" }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateDuration(departure: String, arrival: String): String {
    return try {
        val departureTime = LocalDateTime.parse(departure, DateTimeFormatter.ISO_DATE_TIME)
        val arrivalTime = LocalDateTime.parse(arrival, DateTimeFormatter.ISO_DATE_TIME)
        val duration = Duration.between(departureTime, arrivalTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        "${hours}h ${minutes}Min" // Giữ nguyên format gốc
    } catch (e: Exception) { "" } // Format gốc
}
// #endregion

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlightListScreen(
    isRoundTrip: Boolean,
    initialFlights: List<FlightResponseDTO>,
    outboundFlights: List<FlightResponseDTO>,
    returnFlights: List<FlightResponseDTO>,
    sessionToken: String?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showLoginDialog by remember { mutableStateOf(false) }
    var showingOutbound by remember { mutableStateOf(isRoundTrip) }

    val currentFlights by remember(showingOutbound, isRoundTrip) {
        derivedStateOf {
            when {
                isRoundTrip && showingOutbound -> outboundFlights
                isRoundTrip && !showingOutbound -> returnFlights
                else -> initialFlights
            }
        }
    }

    var selectedOutbound: FlightSelection? by remember { mutableStateOf(null) }
    var selectedReturn: FlightSelection? by remember { mutableStateOf(null) }

    val screenTitle = when {
        isRoundTrip && showingOutbound -> "Chọn chuyến bay đi"
        isRoundTrip && !showingOutbound -> "Chọn chuyến bay về"
        else -> "Chuyến bay khả dụng" // Hoặc "Available Flights" như gốc
    }

    // Login Dialog (Logic không đổi)
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            // Sử dụng Text của Material 2 theo yêu cầu giữ nguyên giao diện gốc
            title = { Text("Đăng nhập") },
            text = { Text("Bạn cần đăng nhập để tiếp tục đặt vé") },
            confirmButton = {
                TextButton( // TextButton của M3 vẫn ổn trong Dialog M3
                    onClick = {
                        showLoginDialog = false
                        val intent = Intent(context, LoginActivity::class.java).apply {
                            putExtra(FlightListActivity.EXTRA_SESSION_TOKEN, sessionToken)
                            putExtra(FlightListActivity.EXTRA_IS_ROUND_TRIP, isRoundTrip)
                            selectedOutbound?.let { (flight, fareCode) ->
                                putExtra(if (isRoundTrip) "outboundFlightNumber" else "flightNumber", flight.flightNumber)
                                putExtra(if (isRoundTrip) "outboundFareCode" else "fareCode", fareCode)
                            }
                            selectedReturn?.let { (flight, fareCode) ->
                                putExtra("returnFlightNumber", flight.flightNumber)
                                putExtra("returnFareCode", fareCode)
                            }
                        }
                        FlightBookingModel.isRoundTrip = isRoundTrip
                        FlightBookingModel.sessionToken = sessionToken
                        selectedOutbound?.let { (flight, fareCode) ->
                            FlightBookingModel.flightNumber = flight.flightNumber
                            FlightBookingModel.fareCode = fareCode
                        }
                        selectedReturn?.let { (flight, fareCode) ->
                            FlightBookingModel.returnFlightNumber = flight.flightNumber
                            FlightBookingModel.returnFareCode = fareCode
                        } ?: run {
                            FlightBookingModel.returnFlightNumber = null
                            FlightBookingModel.returnFareCode = null
                        }
                        context.startActivity(intent)
                    }
                ) { androidx.compose.material3.Text("Đăng nhập") } // Text trong TextButton của M3
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { androidx.compose.material3.Text("Hủy") } // Text trong TextButton của M3
            }
        )
    }

    // Main Screen Layout (Logic không đổi)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.grey01)) // Màu gốc
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar (Logic không đổi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF9C27B0), Color(0xFFEFB8C8)) // Màu gốc
                    ))
                // Không có padding top trong code gốc
            ) {
                Row( // Row gốc
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image( // Image gốc
                        painter = painterResource(R.drawable.back), // Đảm bảo drawable tồn tại
                        contentDescription = null, // Hoặc "Back"
                        modifier = Modifier
                            .padding(8.dp) // Padding gốc
                            .clickable { // Logic back đã cập nhật
                                if (isRoundTrip && !showingOutbound) {
                                    showingOutbound = true
                                    selectedOutbound = null
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                } else {
                                    (context as? AppCompatActivity)?.finish()
                                }
                            }
                    )
                    Text( // Text gốc (M2)
                        text = screenTitle, // Title động
                        fontSize = 22.sp, // Size gốc
                        fontWeight = FontWeight.Bold, // Weight gốc
                        modifier = Modifier.padding(top = 10.dp, bottom = 16.dp), // Padding gốc
                        color = Color.White // Màu gốc
                        // Không có weight(1f) hay Spacer trong code gốc
                    )
                }
            }

            // Flight List (Logic không đổi)
            if (currentFlights.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có chuyến bay nào.") // M2 Text
                }
            } else {
                LazyColumn( // LazyColumn gốc
                    state = listState, // Thêm state để scroll
                    modifier = Modifier.fillMaxSize()
                        .padding(16.dp), // Padding gốc
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing gốc
                    // Không có contentPadding trong code gốc
                ) {
                    items(currentFlights, key = { it.flightNumber + it.scheduledDeparture }) { flight ->
                        // Sử dụng FlightTicketCard với giao diện gốc
                        FlightTicketCard(
                            flight = flight,
                            onFareClassSelected = { selectedFlight, fareCode ->
                                val selection = Pair(selectedFlight, fareCode)
                                if (isRoundTrip) {
                                    if (showingOutbound) {
                                        selectedOutbound = selection
                                        showingOutbound = false
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    } else {
                                        selectedReturn = selection
                                        showLoginDialog = true
                                    }
                                } else {
                                    selectedOutbound = selection
                                    showLoginDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


// #####################################################################
// #  FlightTicketCard Composable - GIỮ NGUYÊN GIAO DIỆN BAN ĐẦU       #
// #####################################################################
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlightTicketCard(
    flight: FlightResponseDTO,
    onFareClassSelected: (FlightResponseDTO, String) -> Unit // Thêm tham số callback
) {
    var isExpanded by remember { mutableStateOf(false) } // State gốc đã sửa lỗi

    ConstraintLayout( // Layout gốc
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp) // Padding gốc
            .fillMaxWidth()
            .background(
                Color.White, // Background gốc
                shape = RoundedCornerShape(15.dp) // Shape gốc
            )
            .clip(RoundedCornerShape(15.dp)) // Thêm clip để hiệu ứng ripple đẹp hơn
    ) {
        // Refs gốc
        val (dateIcon, dateContainer, durationContainer, flightNumber, durationIcon, durationText, dashLine,
            departureTime, departureCode, departureName,
            airplane,
            arrivalTime, arrivalCode, arrivalName,
            fareButton, fareContent) = createRefs()

        // Date Box gốc
        Box(
            modifier = Modifier
                .background(
                    color = colorResource(R.color.pink).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .constrainAs(dateContainer) {
                    start.linkTo(parent.start, 8.dp)
                    top.linkTo(parent.top, 8.dp)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon( // Icon M3
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = colorResource(R.color.pink),
                    modifier = Modifier.size(14.dp)
                )
                Text( // Text M2
                    text = extractDateFromDateTime(flight.scheduledDeparture),
                    color = colorResource(R.color.pink),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Flight Number Text gốc - Đã sửa lỗi hiển thị ngày thay vì số hiệu CB
        Text( // Text M2
            text = flight.flightNumber, // SỬA LỖI: Hiển thị số hiệu chuyến bay
            color = colorResource(R.color.purple_700),
            fontSize = 18.sp, // Size gốc
            fontWeight = FontWeight.Bold, // Thêm Bold cho rõ
            modifier = Modifier.constrainAs(flightNumber){ // Ref gốc
                start.linkTo(parent.start) // Căn giữa theo gốc
                end.linkTo(parent.end)
                top.linkTo(dateContainer.bottom, 8.dp) // Dưới date box
            }
        )

        // Duration Box gốc
        Box(
            modifier = Modifier
                .background(
                    color = colorResource(R.color.pink).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .constrainAs(durationContainer) {
                    end.linkTo(parent.end, 8.dp)
                    top.linkTo(parent.top, 8.dp)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon( // Icon M3
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = colorResource(R.color.pink),
                    modifier = Modifier.size(14.dp)
                )
                Text( // Text M2
                    text = calculateDuration(flight.scheduledDeparture, flight.scheduledArrival),
                    color = colorResource(R.color.pink),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Dash line gốc
        Image(
            painter = painterResource(id = R.drawable.dash_line),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(dashLine) {
                    // top.linkTo(dateContainer.bottom, 16.dp) // Gốc liên kết với dateContainer ? -> Nên là flightNumber
                    top.linkTo(flightNumber.bottom, 16.dp) // Liên kết với flightNumber hợp lý hơn
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(color = colorResource(R.color.grey01))
        )

        // Departure details gốc
        Text( // Text M2
            text = extractTimeFromDateTime(flight.scheduledDeparture),
            fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black,
            modifier = Modifier.constrainAs(departureTime) {
                start.linkTo(parent.start, 10.dp)
                top.linkTo(dashLine.bottom, 8.dp)
            }
        )
        Text( // Text M2
            text = flight.route.originAirport.airportCode,
            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black,
            modifier = Modifier.constrainAs(departureCode) {
                start.linkTo(departureTime.start)
                top.linkTo(departureTime.bottom, 4.dp)
            }
        )
        Text( // Text M2
            text = flight.route.originAirport.airportName,
            color = Color.Gray, fontSize = 12.sp, maxLines = 2,
            modifier = Modifier.constrainAs(departureName) {
                start.linkTo(departureCode.start)
                top.linkTo(departureCode.bottom, 2.dp)
                end.linkTo(airplane.start, 8.dp) // Ngăn chồng lấp
                width = Dimension.preferredWrapContent // Cho phép wrap
            }
        )

        // Airplane gốc
        Image(
            painter = painterResource(id = R.drawable.line_airple_blue),
            contentDescription = null,
            modifier = Modifier
                .width(120.dp) // Size gốc
                .height(24.dp) // Size gốc
                .constrainAs(airplane) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(dashLine.bottom, 16.dp) // Vị trí gốc
                },
            contentScale = ContentScale.FillBounds
        )

        // Arrival details gốc
        Text( // Text M2
            text = extractTimeFromDateTime(flight.scheduledArrival),
            fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black,
            modifier = Modifier.constrainAs(arrivalTime) {
                end.linkTo(parent.end, 10.dp)
                top.linkTo(dashLine.bottom, 8.dp)
            }
        )
        Text( // Text M2
            text = flight.route.destinationAirport.airportCode,
            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black,
            modifier = Modifier.constrainAs(arrivalCode) {
                end.linkTo(arrivalTime.end)
                top.linkTo(arrivalTime.bottom, 4.dp)
            }
        )
        Text( // Text M2
            text = flight.route.destinationAirport.airportName,
            color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End, maxLines = 2,
            modifier = Modifier.constrainAs(arrivalName) {
                end.linkTo(arrivalCode.end)
                top.linkTo(arrivalCode.bottom, 2.dp)
                start.linkTo(airplane.end, 8.dp) // Ngăn chồng lấp
                width = Dimension.preferredWrapContent // Cho phép wrap
            }
        )

        // Barrier gốc
        val bottomBarrier = createBottomBarrier(departureName, arrivalName, airplane) // Thêm airplane vào barrier

        // Fare button gốc
        IconButton( // IconButton M2
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.constrainAs(fareButton) {
                // top.linkTo(departureName.bottom, 12.dp) // Gốc liên kết với departureName? -> Nên là barrier
                top.linkTo(bottomBarrier, 12.dp) // Liên kết với barrier hợp lý hơn
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                // bottom constraint handled by ConstraintLayout padding?
            }
        ) {
            Row( // Row gốc
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon( // Icon M3
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF1A237E) // Màu gốc
                )
            }
        }

        // Fare content gốc
        if (isExpanded) {
            Column( // Column gốc
                modifier = Modifier
                    .constrainAs(fareContent) {
                        top.linkTo(fareButton.bottom, 8.dp) // Dưới nút fare
                        start.linkTo(parent.start) // Trải rộng
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        bottom.linkTo(parent.bottom, 8.dp) // Thêm padding dưới cùng khi mở rộng
                        height = Dimension.wrapContent // Quan trọng cho Column
                    }
                    .padding(horizontal = 16.dp) // Padding được áp dụng cho Card bên trong theo code gốc
            ) {
                // Optional: Add a title or divider before the list
                Text("Select Fare Class:", modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium)
                Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

                flight.fareClasses.forEachIndexed { index, fareClass ->
                    // Divider between items
                    if (index > 0) {
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.6f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }

                    // Card for each fare class
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            // .padding(vertical = 5.dp) // Remove this if Divider handles spacing
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                // Optional: Disable click if not available?
                                if (fareClass.availableSeats > 0) {
                                    onFareClassSelected(flight, fareClass.fareClassCode)
                                }
                                // Else: Maybe show a Toast message "Hết chỗ"?
                            },
                        backgroundColor = Color.White,
                        elevation = 2.dp,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp), // Internal padding
                            horizontalArrangement = Arrangement.SpaceBetween, // Pushes price to the end
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Side: Class Name and Seat Info
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = fareClass.fareClassName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF333333)
                                )
                                Spacer(modifier = Modifier.height(2.dp))

                                // *** MODIFIED ROW FOR AVAILABILITY ***
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Determine text and color based on availability
                                    val isAvailable = fareClass.availableSeats > 0
                                    val availabilityText = if (isAvailable) "Còn chỗ" else "Hết chỗ"
                                    // Use Gray if available, a muted Red if not available
                                    val availabilityColor = if (isAvailable) Color.Gray else Color.Red.copy(alpha = 0.8f)

                                    Icon(
                                        imageVector = Icons.Default.AirlineSeatReclineNormal, // Seat Icon
                                        contentDescription = availabilityText, // Update content description too
                                        tint = availabilityColor, // Match icon tint to text color
                                        modifier = Modifier.size(14.dp) // Adjust icon size
                                    )
                                    Spacer(modifier = Modifier.width(4.dp)) // Space between icon and text
                                    Text(
                                        text = availabilityText,    // Use the dynamic text
                                        color = availabilityColor,  // Use the dynamic color
                                        fontSize = 12.sp
                                    )
                                }
                                // *** END OF MODIFIED ROW ***
                            }

                            // Right Side: Price
                            Text(
                                text = "${String.format("%,.0f", fareClass.basePrice)}đ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colorResource(R.color.purple_700)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp)) // Add padding at the bottom of the list

            }
        } // Kết thúc if(isExpanded)
    } // Kết thúc ConstraintLayout
} // Kết thúc Composable FlightTicketCard