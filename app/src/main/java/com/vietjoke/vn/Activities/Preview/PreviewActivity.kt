package com.vietjoke.vn.Activities.Preview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.Payment.PaymentActivity
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.*
import kotlinx.coroutines.launch

class PreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreviewScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var previewData by remember { mutableStateOf<BookingPreviewData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val sessionToken = FlightBookingModel.sessionToken
        if (sessionToken == null) {
            error = "Session token không hợp lệ"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val response = RetrofitInstance.bookingApi.getBookingPreview(
                authorization = UserModel.token ?: "",
                sessionToken = sessionToken
            )
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.status == 200) {
                    previewData = apiResponse.data
                    FlightBookingModel.sessionToken = apiResponse.data.sessionToken
                } else {
                    error = apiResponse.message
                }
            } else {
                error = "Lỗi khi lấy thông tin đặt chỗ"
            }
        } catch (e: Exception) {
            error = "Lỗi: ${e.message}"
            Log.e("PreviewActivity", "Error getting preview", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xem trước đặt chỗ") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
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
                previewData != null -> {
                    PreviewContent(previewData!!)
                }
            }
        }
    }
}

@Composable
fun PreviewContent(data: BookingPreviewData) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Flight Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Thông tin chuyến bay",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                data.flights.forEach { flight ->
                    FlightInfoItem(flight)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Passenger Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Thông tin hành khách",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                data.passengerDetails.forEach { passenger ->
                    PassengerInfoItem(passenger)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coupon Information
        if (data.coupon.isAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Mã giảm giá",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Mã: ${data.coupon.code}",
                        fontSize = 16.sp
                    )
                    Text(
                        "Mô tả: ${data.coupon.description}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    if (data.coupon.amount != null) {
                        Text(
                            "Giảm giá: ${if (data.coupon.isPercentage == true) "${data.coupon.amount}%" else "${data.coupon.amount} VND"}",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Price
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tổng tiền",
                    fontSize = 18.sp,
                    color = Color.White
                )
                Text(
                    "${String.format("%,.0f", data.totalBookingPrice)} VND",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    val sessionToken = FlightBookingModel.sessionToken
                    if (sessionToken == null) {
                        Toast.makeText(context, "Lỗi: Session token không hợp lệ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.paymentApi.createOrder(
                                authorization = UserModel.token ?: "",
                                sessionToken)
                            if (response.isSuccessful && response.body() != null) {
                                val apiResponse = response.body()!!
                                if (apiResponse.status == 200) {
                                    // Save orderId and update session token
                                    FlightBookingModel.orderId = apiResponse.data.orderId
                                    FlightBookingModel.sessionToken = apiResponse.data.sessionToken
                                    Toast.makeText(context, "Tạo đơn hàng thành công", Toast.LENGTH_SHORT).show()
                                    // Navigate to payment screen
                                    val intent = Intent(context, PaymentActivity::class.java)
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, apiResponse.message, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("PreviewActivity", "Error creating order", e)
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Thanh toán",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FlightInfoItem(flight: FlightPreview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Chuyến bay: ${flight.flightNumber}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Giá vé: ${String.format("%,.0f", flight.totalTicketPrice)} VND",
                fontSize = 14.sp
            )
            Text(
                "Dịch vụ: ${String.format("%,.0f", flight.totalAddonPrice)} VND",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PassengerInfoItem(passenger: PassengerDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "${passenger.firstName} ${passenger.lastName} (${passenger.passengerType})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        passenger.passengerFlightDetailDTOS.forEach { detail ->
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            ) {
                Text(
                    "Chuyến: ${detail.flightNumber}",
                    fontSize = 14.sp
                )
                Text(
                    "Hạng vé: ${detail.fareClass}",
                    fontSize = 14.sp
                )
                Text(
                    "Tuyến: ${detail.routeCode}",
                    fontSize = 14.sp
                )
                if (detail.addons.isNotEmpty()) {
                    Text(
                        "Dịch vụ đã chọn:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    detail.addons.forEach { addon ->
                        Text(
                            "• ${addon.addonName} x${addon.quantity} - ${String.format("%,.0f", addon.price)} VND",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
} 