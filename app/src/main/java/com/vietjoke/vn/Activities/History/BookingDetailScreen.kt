package com.vietjoke.vn.Activities.History

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.vietjoke.vn.retrofit.ResponseDTO.*
import com.vietjoke.vn.retrofit.RetrofitInstance
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingReference: String,
    onBackClick: () -> Unit
) {
    var bookingDetail by remember { mutableStateOf<BookingDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }
    var shouldCancelBooking by remember { mutableStateOf(false) }

    // Effect for loading booking details
    LaunchedEffect(bookingReference) {
        try {
            val token = UserModel.token
            val response = RetrofitInstance.bookingApi.getBookingDetail(
                authorization = "$token",
                bookingReference = bookingReference
            )
            if (response.isSuccessful) {
                response.body()?.let { detailResponse ->
                    if (detailResponse.status == 200) {
                        bookingDetail = detailResponse.data
                    } else {
                        error = detailResponse.message
                    }
                }
            } else {
                error = "Không thể tải thông tin đặt vé"
            }
        } catch (e: Exception) {
            error = "Lỗi kết nối: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Effect for canceling booking
    LaunchedEffect(shouldCancelBooking) {
        if (shouldCancelBooking) {
            isCancelling = true
            try {
                val token = UserModel.token
                val response = RetrofitInstance.bookingApi.cancelBooking(
                    authorization = "$token",
                    bookingReference = bookingReference
                )
                if (response.isSuccessful) {
                    response.body()?.let { cancelResponse ->
                        if (cancelResponse.status == 200) {
                            // Refresh booking details
                            val detailResponse = RetrofitInstance.bookingApi.getBookingDetail(
                                authorization = "$token",
                                bookingReference = bookingReference
                            )
                            if (detailResponse.isSuccessful) {
                                detailResponse.body()?.let { detail ->
                                    if (detail.status == 200) {
                                        bookingDetail = detail.data
                                    }
                                }
                            }
                        } else {
                            error = cancelResponse.message
                        }
                    }
                } else {
                    error = "Không thể hủy chuyến"
                }
            } catch (e: Exception) {
                error = "Lỗi kết nối: ${e.message}"
            } finally {
                isCancelling = false
                shouldCancelBooking = false
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Xác nhận hủy chuyến") },
            text = { Text("Bạn có chắc chắn muốn hủy chuyến này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        shouldCancelBooking = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Hủy chuyến")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp)
                    .offset(y = (-4).dp),
                title = { Text("Chi tiết đặt vé") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                isLoading || isCancelling -> {
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
                bookingDetail != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Booking Status Card
                        BookingStatusCard(bookingDetail!!)

                        // Passenger Information
                        bookingDetail!!.passengers.forEach { passenger ->
                            PassengerCard(passenger = passenger)
                        }

                        // Payment Information
                        PaymentCard(payments = bookingDetail!!.payments)

                        // Contact Information
                        ContactCard(bookingDetail!!)

                        // Cancel Button (only show if booking is not cancelled)
                        if (bookingDetail!!.statusName != "Cancelled") {
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text("Hủy chuyến")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusCard(booking: BookingDetail) {
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đặt vé: ${booking.bookingReference}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                StatusChip(status = booking.statusName)
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                Text(
                    text = formattedAmount,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
fun PassengerCard(passenger: PassengerDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hành khách: ${passenger.firstName} ${passenger.lastName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Text(
                text = "Loại: ${getPassengerTypeName(passenger.passengerType)}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            if (passenger.idType != null) {
                Text(
                    text = "${passenger.idType}: ${passenger.idNumber}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            passenger.flights.forEach { flight ->
                FlightDetailCard(flight = flight)
                if (flight != passenger.flights.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun FlightDetailCard(flight: FlightDetail) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val departureTime = try {
        displayFormat.format(dateFormat.parse(flight.flightDepartureTime))
    } catch (e: Exception) {
        flight.flightDepartureTime
    }
    val arrivalTime = try {
        displayFormat.format(dateFormat.parse(flight.flightArrivalTime))
    } catch (e: Exception) {
        flight.flightArrivalTime
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = flight.flightNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = flight.airlineName,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = flight.fareName,
                fontSize = 14.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = departureTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = flight.flightDepartureAirport,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = "Flight",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = flight.flightRouteCode,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = arrivalTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = flight.flightArrivalAirport,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ghế: ${flight.seatNumber}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Cổng: ${flight.flightGate}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Nhà ga: ${flight.flightTerminal}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (flight.addons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dịch vụ bổ sung:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9C27B0)
            )
            flight.addons.forEach { addon ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${addon.addonName} (x${addon.quantity})",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            .format(addon.price * addon.quantity),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentCard(payments: List<PaymentDetail>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin thanh toán",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            payments.forEach { payment ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val paymentDate = try {
                    displayFormat.format(dateFormat.parse(payment.paymentDate))
                } catch (e: Exception) {
                    payment.paymentDate
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Phương thức: ${getPaymentMethodName(payment.paymentMethod)}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Mã giao dịch: ${payment.transactionId}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Ngày thanh toán: $paymentDate",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                            .format(payment.paymentAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactCard(booking: BookingDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin liên hệ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = booking.userEmail,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = booking.userPhone,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getPassengerTypeName(type: String): String {
    return when (type) {
        "ADULT" -> "Người lớn"
        "CHILD" -> "Trẻ em"
        "INFANT" -> "Em bé"
        else -> type
    }
}

fun getPaymentMethodName(method: String): String {
    return when (method) {
        "PAYPAL" -> "PayPal"
        "VNPAY" -> "VNPay"
        "MOMO" -> "MoMo"
        else -> method
    }
} 