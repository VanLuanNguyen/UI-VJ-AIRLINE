package com.vietjoke.vn.Activities.Payment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.paypal.android.cardpayments.*
import com.paypal.android.cardpayments.threedsecure.SCA
import com.paypal.android.corepayments.Address
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.vietjoke.vn.Activities.Dashboard.DashboardActivity
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightDataDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightResponseDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class PaymentActivity : ComponentActivity(), CardApproveOrderCallback {
    private lateinit var cardClient: CardClient
    private var showSuccessDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PayPal Core Config
        val config = CoreConfig(
            "AXBIiIO9kpWZrhGkNr8h-iCE64QzdY5o_TdPDv9_jSabL21wsbDTyaKix63q1y3XzkEI8oWrgLp6meit",
            Environment.SANDBOX
        )

        // Initialize CardClient
        cardClient = CardClient(this, config)

        setContent {
            PaymentScreen(
                onPaymentSubmit = { cardNumber, expiryMonth, expiryYear, cvv ->
                    processPayment(cardNumber, expiryMonth, expiryYear, cvv)
                }
            )
            
            if (showSuccessDialog) {
                SuccessDialog(
                    onDismiss = {
                        showSuccessDialog = false
                        finish()
                    }
                )
            }
        }
    }

    private fun processPayment(
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String
    ) {
        val orderId = FlightBookingModel.orderId ?: run {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            return
        }

        val billingAddress = Address(
            "VN",                     // countryCode
            "123 Main St.",           // streetAddress
            "Apt. 1A",               // extendedAddress
            "Ho Chi Minh",           // locality
            "HCM",                   // region
            "700000"                 // postalCode
        )

        val card = Card(
            cardNumber,
            expiryMonth,
            expiryYear,
            cvv,
            billingAddress.toString()
        )

        val cardRequest = CardRequest(
            orderId,
            card,
            "vietjoke://return_url",
            SCA.SCA_WHEN_REQUIRED
        )

        cardClient.approveOrder(cardRequest, this)
    }

    override fun onCardApproveOrderResult(result: CardApproveOrderResult) {
        when (result) {
            is CardApproveOrderResult.Success -> {
                // Call capture order API
                val sessionToken = FlightBookingModel.sessionToken
                val orderId = FlightBookingModel.orderId
                
                if (sessionToken != null && orderId != null) {
                    // Call capture order API
                    lifecycleScope.launch {
                        try {
                            val token = UserModel.token
                            if (token == null) {
                                Toast.makeText(this@PaymentActivity, "Lỗi: Không tìm thấy token xác thực", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            val response = RetrofitInstance.paymentApi.captureOrder(
                                authorization = "$token",
                                sessionToken = sessionToken,
                                orderId = orderId
                            )
                            
                            if (response.isSuccessful && response.body() != null) {
                                val apiResponse = response.body()!!
                                if (apiResponse.status == 200) {
                                    // Clear session data after successful payment
                                    FlightBookingModel.clearSessionData()
                                    SelectFlightResponseDTO.clear()
                                    SelectFlightDataDTO.clear()
                                    showSuccessDialog = true
                                } else {
                                    Toast.makeText(this@PaymentActivity, apiResponse.message, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@PaymentActivity, "Lỗi khi xác nhận thanh toán", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("PaymentActivity", "Error capturing order", e)
                            Toast.makeText(this@PaymentActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            is CardApproveOrderResult.Failure -> {
                val error = result.error
                Toast.makeText(this, "Lỗi thanh toán: ${error.message}", Toast.LENGTH_LONG).show()
                Log.d("PaymentActivity", "Lỗi thanh toán: ${error.message}")
            }
            is CardApproveOrderResult.AuthorizationRequired -> TODO()
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(0f) }
    val rotation = remember { Animatable(0f) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        // Start scale animation
        scale = 1f
        // Start rotation animation
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer {
                                rotationZ = rotation.value
                            }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Thanh toán thành công!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        onDismiss()
                        val intent = android.content.Intent(context, DashboardActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onPaymentSubmit: (String, String, String, String) -> Unit) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<ValidationResult?>(null) }
    val context = LocalContext.current

    // Show toast when validation fails
    LaunchedEffect(validationResult) {
        validationResult?.let { result ->
            if (!result.isValid) {
                Toast.makeText(context, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(Color(0xFFF5F5F5)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Number Input
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) cardNumber = it },
                label = { Text("Số thẻ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Expiry Date Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = expiryMonth,
                    onValueChange = { if (it.length <= 2) expiryMonth = it },
                    label = { Text("Tháng") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiryYear,
                    onValueChange = { if (it.length <= 4) expiryYear = it },
                    label = { Text("Năm") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // CVV Input
            OutlinedTextField(
                value = cvv,
                onValueChange = { if (it.length <= 3) cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Payment Button
            Button(
                onClick = {
                        isLoading = true
                        onPaymentSubmit(cardNumber, expiryMonth, expiryYear, cvv)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
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
private fun validateInputs(
    cardNumber: String,
    expiryMonth: String,
    expiryYear: String,
    cvv: String
): ValidationResult {
    if (cardNumber.length != 16) {
        return ValidationResult(false, "Số thẻ phải có 16 chữ số")
    }
    if (expiryMonth.isEmpty() || expiryMonth.toIntOrNull() !in 1..12) {
        return ValidationResult(false, "Tháng hết hạn không hợp lệ")
    }
    if (expiryYear.length != 4) {
        return ValidationResult(false, "Năm hết hạn không hợp lệ")
    }
    if (cvv.length != 3) {
        return ValidationResult(false, "CVV phải có 3 chữ số")
    }
    return ValidationResult(true, "")
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
) 