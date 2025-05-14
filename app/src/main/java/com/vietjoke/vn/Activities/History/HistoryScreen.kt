package com.vietjoke.vn.Activities.History

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.ResponseDTO.BookingHistoryItem
import com.vietjoke.vn.retrofit.ResponseDTO.FlightInfo
import com.vietjoke.vn.retrofit.RetrofitInstance
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBookingClick: (String) -> Unit
) {
    var bookings by remember { mutableStateOf<List<BookingHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = UserModel.token
            val response = RetrofitInstance.bookingApi.getBookingHistory(
                authorization = "$token"
            )
            if (response.isSuccessful) {
                response.body()?.let { historyResponse ->
                    if (historyResponse.status == 200) {
                        bookings = historyResponse.data
                    } else {
                        error = historyResponse.message
                    }
                }
            } else {
                error = "Không thể tải lịch sử đặt vé"
            }
        } catch (e: Exception) {
            error = "Lỗi kết nối: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp)
                    .offset(y = (-4).dp),
                title = { Text("Lịch sử đặt vé") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF9C27B0)
                    )
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No History",
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có lịch sử đặt vé",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookings) { booking ->
                            BookingHistoryCard(
                                booking = booking,
                                onClick = { onBookingClick(booking.bookingReference) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(
    booking: BookingHistoryItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val bookingDate = try {
        displayFormat.format(dateFormat.parse(booking.bookingDate))
    } catch (e: Exception) {
        booking.bookingDate
    }

    val numberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val formattedAmount = numberFormat.format(booking.totalAmount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with booking reference and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đặt vé: ${booking.bookingReference}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                StatusChip(status = booking.statusName)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Flight information
            booking.flights.forEach { flight ->
                FlightInfoRow(flight = flight)
                if (flight != booking.flights.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Booking details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ngày đặt: $bookingDate",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Loại chuyến: ${getTripTypeName(booking.tripType)}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        text = "${booking.adultCount} người lớn, ${booking.childCount} trẻ em, ${booking.infantCount} em bé",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "COMPLETED" -> Color(0xFF4CAF50) to Color.White
        "CANCELLED" -> Color(0xFFF44336) to Color.White
        "PENDING" -> Color(0xFFFFC107) to Color.Black
        else -> Color(0xFF9C27B0) to Color.White
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FlightInfoRow(flight: FlightInfo) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val departureTime = try {
        displayFormat.format(dateFormat.parse(flight.departureTime))
    } catch (e: Exception) {
        flight.departureTime
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = "Flight",
            tint = Color(0xFF9C27B0),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = flight.flightNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = flight.routeCode,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = departureTime,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9C27B0)
        )
    }
}

fun getTripTypeName(tripType: String): String {
    return when (tripType) {
        "ROUND_TRIP" -> "Khứ hồi"
        "ONE_WAY" -> "Một chiều"
        else -> tripType
    }
} 