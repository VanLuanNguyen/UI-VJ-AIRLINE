package com.vietjoke.vn.Activities.SearchFlight

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.FlightList.FlightListActivity

import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.ResponseDTO.FlightResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SearchParamDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SearchFlightActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchFlightScreen()
        }
    }
}

fun parsePassengers(passengerString: String): Triple<Int, Int, Int> {
    val adultMatch = "(\\d+)\\s+người\\s+lớn".toRegex().find(passengerString)
    val childMatch = "(\\d+)\\s+trẻ\\s+em".toRegex().find(passengerString)
    val infantMatch = "(\\d+)\\s+em\\s+bé".toRegex().find(passengerString)
    val adults = adultMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val children = childMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val infants = infantMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    return Triple(adults, children, infants)
}

fun formatPassengers(adults: Int, children: Int, infants: Int): String {
    val parts = mutableListOf<String>()
    if (adults > 0) parts.add("$adults người lớn")
    if (children > 0) parts.add("$children trẻ em")
    if (infants > 0) parts.add("$infants em bé")
    return parts.joinToString(", ").ifEmpty { "Chọn hành khách" }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun SearchFlightScreen() {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var fromCode by remember { mutableStateOf("") }
    var toCode by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var returnDate by remember { mutableStateOf<LocalDate?>(null) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var flights by remember { mutableStateOf<List<FlightResponseDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var isRoundTrip by remember { mutableStateOf(false) }
    var showDepartDatePicker by remember { mutableStateOf(false) }
    var showReturnDatePicker by remember { mutableStateOf(false) }
    var showFromAirportDialog by remember { mutableStateOf(false) }
    var showToAirportDialog by remember { mutableStateOf(false) }
    val initialPassengerString = "1 người lớn"
    val (initialAdults, initialChildren, initialInfants) = remember {
        parsePassengers(initialPassengerString)
    }
    var adults by remember { mutableStateOf(initialAdults) }
    var children by remember { mutableStateOf(initialChildren) }
    var infants by remember { mutableStateOf(initialInfants) }
    val passengers by remember { derivedStateOf { formatPassengers(adults, children, infants) } }
    var showPassengerDialog by remember { mutableStateOf(false) }
    var promoCode by remember { mutableStateOf("") }
    var findCheapest by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9C27B0),
                        Color(0xFFEFB8C8),
                        Color(0xFFBDBDBD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val styledText = buildAnnotatedString {
                append("Let's book your \nflight")
            }
            Text(
                text = styledText,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 16.dp)
            )
            
            Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isRoundTrip,
                            onClick = { isRoundTrip = true },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9C27B0))
                        )
                        Text("Khứ hồi", color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = !isRoundTrip,
                            onClick = { isRoundTrip = false },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9C27B0))
                        )
                        Text("Một chiều", color = Color.Black)
                    }
                    
                    DialogInputField(
                        Icons.Default.FlightTakeoff,
                        "Điểm khởi hành",
                        from,
                        { showFromAirportDialog = true },
                        Icons.Default.ArrowDropDown
                    )
                    
                    DialogInputField(
                        Icons.Default.DateRange,
                        "Ngày đi",
                        date?.format(dateFormatter) ?: "",
                        { showDepartDatePicker = true },
                        Icons.Default.DateRange
                    )
                    
                    if (isRoundTrip) {
                        DialogInputField(
                            Icons.Default.FlightLand,
                            "Điểm đến",
                            to,
                            { showToAirportDialog = true },
                            Icons.Default.ArrowDropDown
                        )
                        DialogInputField(
                            Icons.Default.DateRange,
                            "Ngày về",
                            returnDate?.format(dateFormatter) ?: "",
                            { showReturnDatePicker = true },
                            Icons.Default.DateRange
                        )
                    } else {
                        DialogInputField(
                            Icons.Default.Place,
                            "Điểm đến",
                            to,
                            { showToAirportDialog = true },
                            Icons.Default.ArrowDropDown
                        )
                    }
                    
                    DialogInputField(
                        Icons.Default.Person,
                        "Hành khách",
                        passengers,
                        { showPassengerDialog = true },
                        Icons.Default.ArrowDropDown
                    )
                    
                    TextInputField(
                        Icons.Default.CardGiftcard,
                        "Mã khuyến mãi",
                        promoCode
                    ) { promoCode = it }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = findCheapest,
                            onCheckedChange = { findCheapest = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Black,
                                uncheckedColor = Color.Black,
                                checkmarkColor = Color.White
                            )
                        )
                        Text("Tìm vé rẻ nhất", color = Color.Black)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                )
                            )
                            .clickable {
                                coroutineScope.launch {
                                    isLoading = true
                                    error = null
                                    try {
                                        val param = SearchParamDTO(
                                            tripType = if (isRoundTrip) "round_trip" else "oneway",
                                            tripFrom = fromCode,
                                            tripTo = toCode,
                                            tripStartDate = date?.format(dateFormatter)
                                                ?: LocalDate.now().format(dateFormatter),
                                            tripReturnDate = if (isRoundTrip) returnDate?.format(
                                                dateFormatter
                                            ) else null,
                                            tripPassengers = adults + children + infants,
                                            tripPassengersAdult = adults,
                                            tripPassengersChild = children,
                                            tripPassengersInfant = infants,
                                            coupon = promoCode.ifEmpty { null },
                                            is_find_cheapest = findCheapest
                                        )

                                        val response = RetrofitInstance.flightApi.searchFlights(param)
                                        if (response.status == 200 && response.data != null) {
                                            val travelOptionsMap = response.data.travelOptions.firstOrNull() // Get the first map, assuming it holds the itinerary

                                            if (travelOptionsMap != null && travelOptionsMap.isNotEmpty()) {
                                                val intent = Intent(context, FlightListActivity::class.java)
                                                intent.putExtra("sessionToken", response.data.sessionToken)

                                                // --- MODIFICATION FOR ROUND TRIP ---
                                                if (isRoundTrip) {
                                                    val flightLists = travelOptionsMap.values.toList()

                                                    if (flightLists.size >= 2) {
                                                        val outboundFlights = ArrayList(flightLists[0]) // Assuming first list is outbound
                                                        val returnFlights = ArrayList(flightLists[1])   // Assuming second list is return

                                                        if (outboundFlights.isNotEmpty() && returnFlights.isNotEmpty()) {
                                                            intent.putExtra("isRoundTrip", true)
                                                            intent.putExtra("outboundFlights", outboundFlights)
                                                            intent.putExtra("returnFlights", returnFlights)
                                                            context.startActivity(intent)
                                                        } else {
                                                            error = "Thiếu thông tin chuyến bay đi hoặc về."
                                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                        }
                                                    } else {
                                                        // Handle error: Round trip selected but API didn't return two distinct flight lists
                                                        error = "Không tìm thấy đủ thông tin chuyến bay khứ hồi."
                                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                    }
                                                } else { // One-way trip
                                                    // Use the first list found as the one-way flights
                                                    val flights = ArrayList(travelOptionsMap.values.firstOrNull() ?: emptyList())
                                                    if (flights.isNotEmpty()) {
                                                        intent.putExtra("isRoundTrip", false)
                                                        intent.putExtra("flights", flights) // Use the original key for one-way
                                                        context.startActivity(intent)
                                                    } else {
                                                        error = "Không tìm thấy chuyến bay phù hợp."
                                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                // --- END OF MODIFICATION ---

                                            } else {
                                                error = "Không tìm thấy chuyến bay phù hợp."
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            error = response.message ?: "Lỗi tìm kiếm chuyến bay (${response.status})."
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        error = "⚠️ Lỗi kết nối: ${e.message}"
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Tìm chuyến bay", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (flights.isNotEmpty()) {
                        FlightList(flights = flights)
                    }
                }
            }
        }
        
        if (showPassengerDialog) {
            PassengerSelectionDialog(
                initialAdults,
                initialChildren,
                initialInfants,
                { showPassengerDialog = false }) { newAdults, newChildren, newInfants ->
                adults = newAdults
                children = newChildren
                infants = newInfants
                showPassengerDialog = false
            }
        }
        if (showFromAirportDialog) {
            AirportSelectionDialog(
                onDismiss = { showFromAirportDialog = false },
                onAirportSelected = { airport ->
                    from = "${airport.airportName} (${airport.airportCode})"
                    fromCode = airport.airportCode
                    showFromAirportDialog = false
                }
            )
        }
        if (showToAirportDialog) {
            AirportSelectionDialog(
                onDismiss = { showToAirportDialog = false },
                onAirportSelected = { airport ->
                    to = "${airport.airportName} (${airport.airportCode})"
                    toCode = airport.airportCode
                    showToAirportDialog = false
                }
            )
        }
        ShowDatePickerDialog(showDepartDatePicker, { showDepartDatePicker = false }) {
            date = it
        }
        ShowDatePickerDialog(showReturnDatePicker, { showReturnDatePicker = false }) {
            returnDate = it
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(icon: ImageVector, label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF9C27B0),
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black,
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogInputField(icon: ImageVector, label: String, value: String, onClick: () -> Unit, trailingIcon: ImageVector? = null) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) },
        trailingIcon = {
            trailingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = "Open Dialog",
                    modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledBorderColor = Color.Gray,
            disabledTextColor = Color.Black,
            disabledLabelColor = Color.Gray,
            disabledLeadingIconColor = Color.Gray,
            disabledTrailingIconColor = Color.Gray,
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PassengerSelectionDialog(
    initialAdults: Int,
    initialChildren: Int,
    initialInfants: Int,
    onDismiss: () -> Unit,
    onConfirm: (adults: Int, children: Int, infants: Int) -> Unit
) {
    var adults by remember { mutableStateOf(initialAdults) }
    var children by remember { mutableStateOf(initialChildren) }
    var infants by remember { mutableStateOf(initialInfants) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn hành khách") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PassengerCounter("Người lớn", "12 tuổi trở lên", adults) { newCount ->
                    adults = maxOf(1, newCount)
                }
                PassengerCounter("Trẻ em", "2-11 tuổi", children) { children = it }
                PassengerCounter("Em bé", "Dưới 2 tuổi", infants) { infants = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(adults, children, infants) }) {
                Text("Xong")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PassengerCounter(
    label: String,
    description: String,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val minCount = if (label == "Người lớn") 1 else 0
            CounterButton(icon = Icons.Default.Remove, enabled = count > minCount) {
                onCountChange(count - 1)
            }
            Text(count.toString(), fontSize = 16.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            CounterButton(icon = Icons.Default.Add, enabled = true) {
                onCountChange(count + 1)
            }
        }
    }
}

@Composable
fun CounterButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) Color.LightGray.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.2f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (icon == Icons.Default.Add) "Increase count" else "Decrease count",
            tint = if (enabled) Color.Black else Color.Gray
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowDatePickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    if (showDialog) {
        val datePickerDialog = remember {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    onDateSelected(selectedDate)
                    onDismiss()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnCancelListener { onDismiss() }
                setOnDismissListener { onDismiss() }
            }
        }

        LaunchedEffect(Unit) {
            datePickerDialog.show()
        }
    }
}

@Composable
fun FlightTicketCard(flight: FlightResponseDTO) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Flight header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = flight.flightNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = flight.flightModelCode,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = flight.route.originAirport.airportCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = flight.scheduledDeparture,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Flight path visualization
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0)
                )
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    color = Color(0xFF9C27B0)
                )
                Icon(
                    imageVector = Icons.Default.FlightLand,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0)
                )
            }

            // Destination info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = flight.route.destinationAirport.airportCode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = flight.scheduledArrival,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Fare classes dropdown
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF9C27B0)
                    )
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    flight.fareClasses.forEach { fareClass ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            backgroundColor = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = fareClass.fareClassName,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${fareClass.availableSeats} seats available",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = "${String.format("%,.0f", fareClass.basePrice)}đ",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlightList(flights: List<FlightResponseDTO>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        flights.forEach { flight ->
            FlightTicketCard(flight = flight)
        }
    }
}
