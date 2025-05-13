package com.vietjoke.vn.Activities.OTP

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OTPVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OTPVerificationScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPVerificationScreen() {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(60) }
    var isResendEnabled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Background Image
        Image(
            painter = painterResource(R.drawable.register_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.1f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF9C27B0),
                                Color(0xFFEFB8C8)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OTP Verification",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please enter the verification code sent to your email",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Field
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Enter OTP") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resend OTP Button
            TextButton(
                onClick = {
                    if (isResendEnabled) {
                        // TODO: Implement resend OTP logic
                        Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                        timeLeft = 60
                        isResendEnabled = false
                        coroutineScope.launch {
                            while (timeLeft > 0) {
                                delay(1000)
                                timeLeft--
                            }
                            isResendEnabled = true
                        }
                    }
                },
                enabled = isResendEnabled,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = if (isResendEnabled) "Resend OTP" else "Resend OTP in $timeLeft seconds",
                    color = if (isResendEnabled) Color(0xFF9C27B0) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = {
                    // TODO: Implement OTP verification logic
                    if (otp.length == 6) {
                        Toast.makeText(context, "Verifying OTP...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter a valid OTP", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "Verify OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // Start countdown timer when screen is first displayed
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        isResendEnabled = true
    }
} 