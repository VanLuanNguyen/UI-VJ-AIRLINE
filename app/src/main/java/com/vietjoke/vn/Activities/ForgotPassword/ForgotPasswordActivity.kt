package com.vietjoke.vn.Activities.ForgotPassword

import android.content.Intent
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.OTP.OTPVerificationActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgotPasswordScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen() {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
                    onClick = { (context as ForgotPasswordActivity).finish() },
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
                    text = "Forgot Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your email address to receive a password reset code",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = Color.Black
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(
                                text = emailError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(R.color.purple_200),
                        unfocusedBorderColor = colorResource(R.color.purple_200),
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit button
                Button(
                    onClick = {
                        // Reset error state
                        emailError = null
                        
                        // Validate email
                        if (email.isBlank()) {
                            emailError = "Email is required"
                            return@Button
                        }
                        
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = "Please enter a valid email address"
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                val response = RetrofitInstance.authApi.forgotPassword(email)
                                if (response.isSuccessful) {
                                    response.body()?.let { forgotResponse ->
                                        when (forgotResponse.status) {
                                            200 -> {
                                                Toast.makeText(
                                                    context,
                                                    "Reset code sent to your email",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                
                                                // Navigate to OTP verification
                                                val intent = Intent(context, OTPVerificationActivity::class.java).apply {
                                                    putExtra("email", email)
                                                    putExtra("isPasswordReset", true)
                                                }
                                                context.startActivity(intent)
                                                (context as ForgotPasswordActivity).finish()
                                            }
                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    forgotResponse.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to send reset code. Please try again.",
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
                        text = "Send Reset Code",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
} 