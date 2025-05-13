package com.vietjoke.vn.Activities.Register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.Activities.OTP.OTPVerificationActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.RegisterRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.vietjoke.vn.model.FlightBookingModel

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }
}

@Preview
@Composable
fun RegisterScreen() {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val json = Json { ignoreUnknownKeys = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(R.drawable.register_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Box chỉ bao phần nội dung
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .background(
                    color = Color.White.copy(alpha = 0.85f), // Nền sáng hơn cho Light Mode
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text("Register", fontSize = 30.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))

                // Input fields
                CustomTextField(value = username, label = "Username", onValueChange = { username = it }, leadingIcon = Icons.Filled.Person)
                CustomTextField(value = email, label = "Email", onValueChange = { email = it }, leadingIcon = Icons.Filled.Email)

                // First Name & Last Name trên cùng một hàng
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = firstName, label = "First Name",
                        onValueChange = { firstName = it },
                        leadingIcon = Icons.Filled.AccountCircle,
                        modifier = Modifier.weight(1f)
                    )
                    CustomTextField(
                        value = lastName, label = "Last Name",
                        onValueChange = { lastName = it },
                        leadingIcon = Icons.Filled.AccountCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                CustomTextField(value = phone, label = "Phone", onValueChange = { phone = it }, keyboardType = KeyboardType.Phone, leadingIcon = Icons.Filled.Phone)
                DatePickerField(value = dateOfBirth, label = "Date of Birth", onDateSelected = { dateOfBirth = it })

                // Password với nút ẩn/hiện
                CustomTextField(
                    value = password,
                    label = "Password",
                    onValueChange = { password = it },
                    keyboardType = KeyboardType.Password,
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible }
                )

                // Confirm Password với nút ẩn/hiện
                CustomTextField(
                    value = confirmPassword,
                    label = "Confirm Password",
                    onValueChange = { confirmPassword = it },
                    keyboardType = KeyboardType.Password,
                    leadingIcon = Icons.Filled.Lock,
                    isPassword = true,
                    isPasswordVisible = confirmPasswordVisible,
                    onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Validate input fields
                        var hasError = false
                        
                        if (username.isBlank() || username.length < 4 || username.length > 20) {
                            Toast.makeText(context, "Username must be between 4 and 20 characters", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (password.isBlank() || password.length < 8 || !password.matches(Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"))) {
                            Toast.makeText(context, "Password must be at least 8 characters with uppercase, number and special character", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (confirmPassword.isBlank() || password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (firstName.isBlank() || firstName.length > 50) {
                            Toast.makeText(context, "First name must not exceed 50 characters", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (lastName.isBlank() || lastName.length > 50) {
                            Toast.makeText(context, "Last name must not exceed 50 characters", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (phone.isBlank()) {
                            Toast.makeText(context, "Invalid Vietnamese phone number format", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (dateOfBirth.isBlank()) {
                            Toast.makeText(context, "Date of birth is required", Toast.LENGTH_SHORT).show()
                            hasError = true
                        }
                        
                        if (!hasError) {
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitInstance.authApi.register(
                                        RegisterRequestDTO(
                                            username = username,
                                            email = email,
                                            password = password,
                                            confirmPassword = confirmPassword,
                                            firstName = firstName,
                                            lastName = lastName,
                                            phone = phone,
                                            dateOfBirth = dateOfBirth
                                        )
                                    )
                                    
                                    if (response.isSuccessful) {
                                        response.body()?.let { registerResponse ->
                                            when (registerResponse.status) {
                                                200 -> {
                                                    Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, OTPVerificationActivity::class.java)
                                                    intent.putExtra("email", email)
                                                    // Pass flight information if available
                                                    FlightBookingModel.flightNumber?.let { intent.putExtra("flightNumber", it) }
                                                    FlightBookingModel.fareCode?.let { intent.putExtra("fareCode", it) }
                                                    FlightBookingModel.sessionToken?.let { intent.putExtra("sessionToken", it) }
                                                    context.startActivity(intent)
                                                }
                                                else -> {
                                                    val errorMessage = registerResponse.errors?.firstOrNull()?.message 
                                                        ?: registerResponse.message 
                                                        ?: "Unknown error occurred"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        if (errorBody != null) {
                                            try {
                                                val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                                                when (errorResponse.status) {
                                                    400 -> {
                                                        val errorMessage = errorResponse.errors?.firstOrNull()?.message 
                                                            ?: errorResponse.message 
                                                            ?: "Invalid input"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                                    409 -> {
                                                        val errorMessage = errorResponse.errors?.firstOrNull()?.message 
                                                            ?: errorResponse.message 
                                                            ?: "Username or email already exists"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> {
                                                        val errorMessage = errorResponse.errors?.firstOrNull()?.message 
                                                            ?: errorResponse.message 
                                                            ?: "Unknown error occurred"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_200))
                ) {
                    Text("Register", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val context = LocalContext.current
                    TextButton(onClick = {
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text(
                            text = "Login",
                            color = colorResource(R.color.orange),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) }, // Change label color to black for Light Mode
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = "$label Icon", tint = Color.Black) }
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onVisibilityChange?.invoke() }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = Color.Black
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
        ),
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier,
        textStyle = TextStyle(color = Color.Black),
        singleLine = true,
        shape = RoundedCornerShape(16.dp), // Add shape property here for rounded corners
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(R.color.purple_200),
            unfocusedBorderColor = colorResource(R.color.purple_200),
            cursorColor = Color.Black,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            onDateSelected(dateFormat.format(selectedDate.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label, color = Color.Black) },
        leadingIcon = {
            Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Date Picker Icon", tint = Color.Black)
        },
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Select Date", tint = colorResource(R.color.purple_200))
            }
        },
        readOnly = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Color.Black),
        singleLine = true,
        shape = RoundedCornerShape(16.dp), // Add shape property here for rounded corners
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(R.color.purple_200),
            unfocusedBorderColor = colorResource(R.color.purple_200),
            cursorColor = Color.Black
        )
    )
}
