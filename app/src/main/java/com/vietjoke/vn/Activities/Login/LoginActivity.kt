package com.vietjoke.vn.Activities.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.Activities.Register.RegisterActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.LoginRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.FlightSelectionDTO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Response
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.Activities.Booking.BookingActivity
import com.vietjoke.vn.Activities.Dashboard.DashboardActivity
import com.vietjoke.vn.Activities.FlightList.FlightListActivity
import com.vietjoke.vn.model.PassengerCountModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                LoginScreen()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val json = Json { ignoreUnknownKeys = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.register_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_login),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(30.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Login",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        fontSize = 35.sp
                    )
                    //Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            usernameError = null
                        },
                        label = { Text("Username", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Username Icon",
                                tint = Color.Black
                            )
                        },
                        isError = usernameError != null,
                        supportingText = {
                            if (usernameError != null) {
                                Text(
                                    text = usernameError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White.copy(alpha = 0.8f),
                            focusedTextColor = Color.Black,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color.Black,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red,
                            errorCursorColor = Color.Red
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    //Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password", color = Color.Gray) },
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password Icon",
                                tint = Color.Black
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                    tint = Color.Black
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(
                                    text = passwordError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White.copy(alpha = 0.8f),
                            focusedTextColor = Color.Black,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = Color.Black,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red,
                            errorCursorColor = Color.Red
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    //Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Black,
                                    uncheckedColor = Color.Black,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                "Remember Me",
                                color = Color.Black,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 14.sp
                            )
                        }

                        TextButton(onClick = { /* TODO: Handle forgot password */ }) {
                            Text(
                                text = "Forgot Password?",
                                color = colorResource(R.color.orange),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                                fontSize = 14.sp
                            )
                        }
                    }

                    //Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Reset error states
                            usernameError = null
                            passwordError = null
                            
                            // Validate input fields
                            var hasError = false
                            
                            if (username.isBlank()) {
                                usernameError = "Username is required"
                                hasError = true
                            }
                            
                            if (password.isBlank()) {
                                passwordError = "Password is required"
                                hasError = true
                            }
                            
                            if (!hasError) {
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitInstance.authApi.login(
                                            LoginRequestDTO(
                                                identifier = username,
                                                password = password
                                            )
                                        )

                                        if (response.isSuccessful) {
                                            response.body()?.let { loginResponse ->
                                                when (loginResponse.status) {
                                                    200 -> {
                                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                                                        // --- CẬP NHẬT LOGIC XỬ LÝ CHUYẾN BAY ---
                                                        val currentIntent = (context as? LoginActivity)?.intent
                                                        val isRoundTrip = currentIntent?.getBooleanExtra(
                                                            FlightListActivity.EXTRA_IS_ROUND_TRIP, false) ?: FlightBookingModel.isRoundTrip

                                                        // Lấy thông tin chuyến đi (ưu tiên Intent)
                                                        val outboundFlightNumber = currentIntent?.getStringExtra(if(isRoundTrip) "outboundFlightNumber" else "flightNumber") ?: FlightBookingModel.flightNumber
                                                        val outboundFareCode = currentIntent?.getStringExtra(if(isRoundTrip) "outboundFareCode" else "fareCode") ?: FlightBookingModel.fareCode
                                                        val sessionToken = currentIntent?.getStringExtra(FlightListActivity.EXTRA_SESSION_TOKEN) ?: FlightBookingModel.sessionToken // Lấy sessionToken

                                                        // Tạo danh sách chuyến bay cần chọn
                                                        val flightSelections = mutableListOf<FlightSelectionDTO>()

                                                        if (outboundFlightNumber != null && outboundFareCode != null) {
                                                            flightSelections.add(
                                                                FlightSelectionDTO(
                                                                    flightNumber = outboundFlightNumber,
                                                                    fareCode = outboundFareCode
                                                                )
                                                            )
                                                            Log.d("LoginSelectFlight", "Added Outbound: $outboundFlightNumber / $outboundFareCode")

                                                            // Nếu là khứ hồi, lấy và thêm thông tin chuyến về
                                                            if (isRoundTrip) {
                                                                val returnFlightNumber = currentIntent?.getStringExtra("returnFlightNumber") ?: FlightBookingModel.returnFlightNumber
                                                                val returnFareCode = currentIntent?.getStringExtra("returnFareCode") ?: FlightBookingModel.returnFareCode

                                                                if (returnFlightNumber != null && returnFareCode != null) {
                                                                    flightSelections.add(
                                                                        FlightSelectionDTO(
                                                                            flightNumber = returnFlightNumber,
                                                                            fareCode = returnFareCode
                                                                        )
                                                                    )
                                                                    Log.d("LoginSelectFlight", "Added Return: $returnFlightNumber / $returnFareCode")
                                                                } else {
                                                                    // Log cảnh báo nếu là khứ hồi nhưng thiếu thông tin chuyến về
                                                                    Log.w("LoginSelectFlight", "Round trip but return flight details missing.")
                                                                    // Có thể hiển thị Toast hoặc xử lý khác nếu cần
                                                                }
                                                            }
                                                        }

                                                        // Chỉ gọi API selectFlight nếu có thông tin chuyến bay cần chọn và có sessionToken
                                                        if (flightSelections.isNotEmpty() && sessionToken != null) {
                                                            Log.d("LoginSelectFlight", "Calling selectFlight API with ${flightSelections.size} flights and token: $sessionToken")
                                                            try {
                                                                // Gọi API selectFlight với danh sách chuyến bay đã tạo
                                                                val selectResponse = RetrofitInstance.flightApi.selectFlight(
                                                                    SelectFlightRequestDTO(
                                                                        sessionToken = sessionToken,
                                                                        flights = flightSelections // <-- Sử dụng danh sách đã tạo
                                                                    )
                                                                )

                                                                if (selectResponse.status == 200 && selectResponse.data != null) {
                                                                    Toast.makeText(context, "Flight(s) selected successfully", Toast.LENGTH_SHORT).show()
                                                                    // Cập nhật sessionToken mới từ response (QUAN TRỌNG)
                                                                    FlightBookingModel.sessionToken = selectResponse.data.sessionToken

                                                                    // Cập nhật số lượng hành khách từ response (nếu bạn dùng PassengerCountModel)
                                                                    PassengerCountModel.setCounts(
                                                                        adult = selectResponse.data.tripPassengersAdult ?: 1,
                                                                        child = selectResponse.data.tripPassengersChildren ?: 0,
                                                                        infant = selectResponse.data.tripPassengersInfant ?: 0
                                                                    )
                                                                    // Khởi tạo danh sách hành khách trong model
                                                                    FlightBookingModel.initializePassengers()

                                                                    // Chuyển sang màn hình Booking
                                                                    val intent = Intent(context, BookingActivity::class.java)
                                                                    context.startActivity(intent)
                                                                    // Tùy chọn: finish() LoginActivity nếu không muốn quay lại
                                                                    (context as? ComponentActivity)?.finish()

                                                                } else {
                                                                    // Lỗi từ API selectFlight
                                                                    Toast.makeText(context, "Error selecting flight: ${selectResponse.message}", Toast.LENGTH_SHORT).show()
                                                                    Log.e("LoginSelectFlight", "API selectFlight error: ${selectResponse.message} (Status: ${selectResponse.status})")
                                                                    // Có thể ở lại màn hình Login hoặc quay lại FlightList?
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Error selecting flight: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                Log.e("LoginSelectFlight", "Exception during selectFlight call", e)
                                                                // Xử lý lỗi mạng/ngoại lệ
                                                            }
                                                        } else {
                                                            // Đăng nhập thành công nhưng không có thông tin chuyến bay từ trước
                                                            // -> Chuyển hướng đến màn hình chính (Dashboard)
                                                            Log.d("LoginFlow", "Login successful, no pending flight selection. Navigating to Dashboard.")
                                                            val intent = Intent(context, DashboardActivity::class.java) // Đảm bảo tên Activity đúng
                                                            context.startActivity(intent)
                                                            // Tùy chọn: finish() LoginActivity
                                                            (context as? ComponentActivity)?.finish()
                                                        }
                                                        // --- KẾT THÚC CẬP NHẬT LOGIC ---
                                                    }
                                                    else -> {
                                                        // ... (Xử lý lỗi login khác giữ nguyên) ...
                                                        val errorMessage = loginResponse.errors?.firstOrNull()?.message ?: loginResponse.message ?: "Unknown error occurred"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            // Handle HTTP error responses
                                            val errorBody = response.errorBody()?.string()
                                            if (errorBody != null) {
                                                try {
                                                    val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                                                    when (errorResponse.status) {
                                                        401 -> {

                                                            Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show()
                                                        }
                                                        404 -> {

                                                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
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
                                            } else {
                                                Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            colorResource(R.color.purple),
                                            colorResource(R.color.purple_700),
                                            colorResource(R.color.pink)
                                        )
                                    ),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .fillMaxWidth()
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ){
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 1.5.sp)
                        )}
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Or continue with",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { /* TODO: Handle Facebook login */ },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Facebook,
                                contentDescription = "Facebook Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Facebook",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { /* TODO: Handle Google login */ },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB4437))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_google2),
                                contentDescription = "Google Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Don't have an account? ",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp
                        )

                        TextButton(onClick = {
                            val intent = Intent(context, RegisterActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Text(
                                text = "Get started",
                                color = colorResource(R.color.orange),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
