package com.vietjoke.vn.Activities.OTP

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.VerifyOTPRequestDTO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OTPVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email") ?: ""
        val isPasswordReset = intent.getBooleanExtra("isPasswordReset", false)
        setContent {
            OTPVerificationScreen(email = email, isPasswordReset = isPasswordReset)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPVerificationScreen(email: String, isPasswordReset: Boolean) {
    var otp by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(60) }
    var isResendEnabled by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Animation for the timer
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Success animation
    val successScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val checkScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    val checkRotation by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 0f else -90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkRotation"
    )

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else {
            isResendEnabled = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.register_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main content box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .background(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back button
                IconButton(
                    onClick = { (context as OTPVerificationActivity).finish() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title and description
                Text(
                    text = if (isPasswordReset) "Reset Password" else "Verify Your Email",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPasswordReset) 
                        "Enter the verification code sent to your email to reset your password"
                    else
                        "We've sent a verification code to your email",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Success animation
                AnimatedVisibility(
                    visible = showSuccessAnimation,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(colorResource(R.color.purple_200))
                            .scale(successScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier
                                .size(50.dp)
                                .scale(checkScale)
                                .graphicsLayer {
                                    rotationZ = checkRotation
                                }
                        )
                    }
                }

                // OTP Input
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("Enter 6-digit code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 8.sp
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(R.color.purple_200),
                        unfocusedBorderColor = colorResource(R.color.purple_200),
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Timer and Resend button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isResendEnabled) {
                        Text(
                            text = "Resend code in ",
                            color = Color.Gray
                        )
                        Text(
                            text = "$timeLeft s",
                            color = colorResource(R.color.purple_200),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                    } else {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitInstance.authApi.resendOTP(email)
                                        if (response.isSuccessful) {
                                            response.body()?.let { resendResponse ->
                                                when (resendResponse.status) {
                                                    200 -> {
                                                        timeLeft = 60
                                                        isResendEnabled = false
                                                        Toast.makeText(
                                                            context,
                                                            resendResponse.data.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    else -> {
                                                        Toast.makeText(
                                                            context,
                                                            resendResponse.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to resend OTP. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Network error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Resend Code",
                                color = colorResource(R.color.purple_200),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Verify button
                Button(
                    onClick = {
                        if (otp.length == 6) {
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitInstance.authApi.verifyOTP(
                                        VerifyOTPRequestDTO(
                                            email = email,
                                            otp = otp,
                                            otpType = if (isPasswordReset) "RESET" else null
                                        )
                                    )
                                    
                                    if (response.isSuccessful) {
                                        response.body()?.let { verifyResponse ->
                                            when (verifyResponse.status) {
                                                200 -> {
                                                    showSuccessAnimation = true
                                                    delay(1500) // Show success animation
                                                    
                                                    if (isPasswordReset) {
                                                        // For password reset, just show success message and go back to login
                                                        Toast.makeText(
                                                            context,
                                                            "New password has been sent to your email",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                    
                                                    // Navigate to LoginActivity
                                                    val intent = Intent(context, LoginActivity::class.java)
                                                    context.startActivity(intent)
                                                    (context as OTPVerificationActivity).finish()
                                                }
                                                else -> {
                                                    Toast.makeText(
                                                        context,
                                                        verifyResponse.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Verification failed. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Network error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple_200)
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Verify",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}