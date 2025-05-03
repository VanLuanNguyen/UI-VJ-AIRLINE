package com.vietjoke.vn.Activities.FlightList

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.ResponseDTO.FlightResponseDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.model.FlightBookingModel

class FlightListActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get flight list and sessionToken from intent
        @Suppress("UNCHECKED_CAST")
        val flights = intent.getSerializableExtra("flights") as? List<FlightResponseDTO> ?: emptyList()
        val sessionToken = intent.getStringExtra("sessionToken")

        setContent {
            FlightListScreen(flights = flights, sessionToken = sessionToken)
        }
    }
}

// Add this function to extract time from datetime string
@RequiresApi(Build.VERSION_CODES.O)
private fun extractTimeFromDateTime(dateTimeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        dateTimeStr // Return original string if parsing fails
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun extractDateFromDateTime(dateTimeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    } catch (e: Exception) {
        dateTimeStr
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateDuration(departure: String, arrival: String): String {
    return try {
        val departureTime = LocalDateTime.parse(departure, DateTimeFormatter.ISO_DATE_TIME)
        val arrivalTime = LocalDateTime.parse(arrival, DateTimeFormatter.ISO_DATE_TIME)
        val duration = java.time.Duration.between(departureTime, arrivalTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        "${hours}h ${minutes}Min"
    } catch (e: Exception) {
        ""
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlightListScreen(flights: List<FlightResponseDTO>, sessionToken: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLoginDialog by remember { mutableStateOf(false) }
    var selectedFlightPair by remember { mutableStateOf<Pair<FlightResponseDTO, String>?>(null) }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Đăng nhập") },
            text = { Text("Bạn cần đăng nhập để tiếp tục đặt vé") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLoginDialog = false
                        selectedFlightPair?.let { (flight, fareCode) ->
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                putExtra("flightNumber", flight.flightNumber)
                                putExtra("fareCode", fareCode)
                                putExtra("sessionToken", sessionToken)
                            }
                            // Save to model
                            FlightBookingModel.flightNumber = flight.flightNumber
                            FlightBookingModel.fareCode = fareCode
                            FlightBookingModel.sessionToken = sessionToken
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.grey01))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFFEFB8C8)
                        )
                    ))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { (context as? AppCompatActivity)?.finish() }
                    )
                    Text(
                        text = "Available Flights",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
                        color = Color.White
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(flights) { flight ->
                    FlightTicketCard(
                        flight = flight,
                        onFareClassSelected = { selectedFlight, fareCode ->
                            selectedFlightPair = Pair(selectedFlight, fareCode)
                            showLoginDialog = true
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlightTicketCard(flight: FlightResponseDTO, onFareClassSelected: (FlightResponseDTO, String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(
                Color.White,
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        val (dateIcon, dateContainer, durationContainer, flightNumber, durationIcon, durationText, dashLine,
            departureTime, departureCode, departureName,
            airplane,
            arrivalTime, arrivalCode, arrivalName,
            fareButton, fareContent) = createRefs()

        // Date
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
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = colorResource(R.color.pink),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = extractDateFromDateTime(flight.scheduledDeparture),
                    color = colorResource(R.color.pink),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Text(
            text = extractDateFromDateTime(flight.flightNumber),
            color = colorResource(R.color.purple_700),
            fontSize = 18.sp,
            modifier = Modifier.constrainAs(flightNumber){
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(dateContainer.bottom, 8.dp)
            }
        )
        // Duration
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
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = colorResource(R.color.pink),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = calculateDuration(flight.scheduledDeparture, flight.scheduledArrival),
                    color = colorResource(R.color.pink),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Dash line
        Image(
            painter = painterResource(id = R.drawable.dash_line),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(dashLine) {
                    top.linkTo(dateContainer.bottom, 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(color = colorResource(R.color.grey01))
        )

        // Departure details
        Text(
            text = extractTimeFromDateTime(flight.scheduledDeparture),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(departureTime) {
                start.linkTo(parent.start, 10.dp)
                top.linkTo(dashLine.bottom, 8.dp)
            }
        )
        Text(
            text = flight.route.originAirport.airportCode,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(departureCode) {
                start.linkTo(departureTime.start)
                top.linkTo(departureTime.bottom, 4.dp)
            }
        )
        Text(
            text = flight.route.originAirport.airportName,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.constrainAs(departureName) {
                start.linkTo(departureCode.start)
                top.linkTo(departureCode.bottom, 2.dp)
            }
        )

        // Airplane
        Image(
            painter = painterResource(id = R.drawable.line_airple_blue),
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .height(24.dp)
                .constrainAs(airplane) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(dashLine.bottom, 16.dp)
                },
            contentScale = ContentScale.FillBounds
        )

        // Arrival details
        Text(
            text = extractTimeFromDateTime(flight.scheduledArrival),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(arrivalTime) {
                end.linkTo(parent.end, 10.dp)
                top.linkTo(dashLine.bottom, 8.dp)
            }
        )
        Text(
            text = flight.route.destinationAirport.airportCode,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.constrainAs(arrivalCode) {
                end.linkTo(arrivalTime.end)
                top.linkTo(arrivalTime.bottom, 4.dp)
            }
        )
        Text(
            text = flight.route.destinationAirport.airportName,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.constrainAs(arrivalName) {
                end.linkTo(arrivalCode.end)
                top.linkTo(arrivalCode.bottom, 2.dp)
            }
        )

        // Fare button
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.constrainAs(fareButton) {
                top.linkTo(departureName.bottom, 12.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF1A237E)
                )
            }
        }

        // Fare content
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .constrainAs(fareContent) {
                        top.linkTo(fareButton.bottom, 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 16.dp) // Add horizontal padding to the container column
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
        }
    }
}