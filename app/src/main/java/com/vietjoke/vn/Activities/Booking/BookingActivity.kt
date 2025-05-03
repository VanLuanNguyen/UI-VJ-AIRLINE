package com.vietjoke.vn.Activities.Booking

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility // Thêm import này
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Thêm import này
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.R
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.PassengerModel
import java.text.SimpleDateFormat
import java.util.*
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.BookingRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import com.vietjoke.vn.retrofit.ResponseDTO.BookingResponseDTO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// --- BookingActivity, BookingScreen, PassengerForm, validatePassengers giữ nguyên ---
// --- Bạn chỉ cần thay thế hoặc thêm hàm PassengerSection bên dưới ---

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookingScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BookingScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val json = Json { ignoreUnknownKeys = true }

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
                .padding(16.dp)
                .verticalScroll(scrollState)
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
                        text = "Passenger Information",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please fill in the details for all passengers",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Check if there are any passengers
            if (FlightBookingModel.passengersAdult.isEmpty() &&
                FlightBookingModel.passengersChild.isEmpty() &&
                FlightBookingModel.passengersInfant.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No passengers to book",
                            color = Color(0xFF9C27B0),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please select a flight first",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Adult Passengers
                if (FlightBookingModel.passengersAdult.isNotEmpty()) {
                    PassengerSection( // Sử dụng PassengerSection đã được cập nhật
                        title = "Adult Passengers",
                        icon = Icons.Default.Person,
                        startExpanded = true // Bắt đầu mở rộng
                    ) {
                        FlightBookingModel.passengersAdult.forEachIndexed { index, passenger ->
                            PassengerForm(
                                passenger = passenger,
                                title = "Adult ${index + 1}",
                                showAdditionalFields = true
                            )
                            if (index < FlightBookingModel.passengersAdult.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Thêm khoảng cách giữa các Card

                // Child Passengers
                if (FlightBookingModel.passengersChild.isNotEmpty()) {
                    PassengerSection( // Sử dụng PassengerSection đã được cập nhật
                        title = "Child Passengers",
                        icon = Icons.Default.ChildCare,
                        startExpanded = false // Bắt đầu thu gọn (hoặc true nếu muốn)
                    ) {
                        FlightBookingModel.passengersChild.forEachIndexed { index, passenger ->
                            PassengerForm(
                                passenger = passenger,
                                title = "Child ${index + 1}",
                                showAdditionalFields = false
                            )
                            if (index < FlightBookingModel.passengersChild.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Thêm khoảng cách giữa các Card

                // Infant Passengers
                if (FlightBookingModel.passengersInfant.isNotEmpty()) {
                    PassengerSection( // Sử dụng PassengerSection đã được cập nhật
                        title = "Infant Passengers",
                        icon = Icons.Default.ChildFriendly,
                        startExpanded = false // Bắt đầu thu gọn (hoặc true nếu muốn)
                    ) {
                        FlightBookingModel.passengersInfant.forEachIndexed { index, passenger ->
                            PassengerForm(
                                passenger = passenger,
                                title = "Infant ${index + 1}",
                                showAdditionalFields = false,
                                showAccompanyingAdult = true
                            )
                            if (index < FlightBookingModel.passengersInfant.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val validationError = validatePassengers()
                        if (validationError == null) {
                            coroutineScope.launch {
                                try {
                                    val bookingRequest = BookingRequestDTO(
                                        passengersAdult = FlightBookingModel.passengersAdult,
                                        passengersChild = FlightBookingModel.passengersChild,
                                        passengersInfant = FlightBookingModel.passengersInfant,
                                        sessionToken = FlightBookingModel.sessionToken ?: ""
                                    )
                                    Log.d("BookingDebug", "Sending Request: ${json.encodeToString(BookingRequestDTO.serializer(), bookingRequest)}")
                                    val response = RetrofitInstance.bookingApi.createBooking(bookingRequest)

                                    if (response.isSuccessful) {
                                        response.body()?.let { bookingResponse ->
                                            when (bookingResponse.status) {
                                                200 -> {
                                                    Toast.makeText(context, bookingResponse.message, Toast.LENGTH_SHORT).show()
                                                    bookingResponse.data?.sessionToken?.let { newSessionToken ->
                                                        FlightBookingModel.sessionToken = newSessionToken
                                                    }
                                                    // TODO: Navigate
                                                }
                                                else -> {
                                                    Toast.makeText(context, bookingResponse.message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        if (errorBody != null) {
                                            try {
                                                val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)
                                                val errorMessage = errorResponse.errors?.firstOrNull()?.message ?: errorResponse.message ?: "Error: ${response.code()}"
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("BookingError", "Network/API Call Error", e)
                                }
                            }
                        } else {
                            Toast.makeText(context, validationError, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        "Submit Booking",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // Thêm màu trắng cho chữ
                    )
                }
            }
        }
    }
}

// ============================================================
// ===== PHẦN CẬP NHẬT CHÍNH: PassengerSection Composable =====
// ============================================================
@Composable
fun PassengerSection(
    title: String,
    icon: ImageVector,
    startExpanded: Boolean = true, // Thêm tham số để kiểm soát trạng thái ban đầu
    content: @Composable () -> Unit
) {
    // State để theo dõi trạng thái mở rộng/thu gọn
    var isExpanded by remember { mutableStateOf(startExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
        // .padding(vertical = 8.dp) // Có thể bỏ padding ở đây và thêm Spacer giữa các Card
        ,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Thêm độ nổi bật
    ) {
        Column(
            // Thêm padding bên trong Card thay vì bên ngoài
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Hàng tiêu đề, có thể nhấp để mở rộng/thu gọn
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded } // Làm cho Row có thể nhấp
                    .padding(vertical = 8.dp) // Thêm padding cho vùng nhấp
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f) // Cho phép tiêu đề chiếm không gian còn lại
                )
                // Icon chỉ báo mở rộng/thu gọn
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF9C27B0)
                )
            }

            // Nội dung có thể thu gọn/mở rộng với hiệu ứng animation
            AnimatedVisibility(visible = isExpanded) {
                // Bọc nội dung trong Column để đảm bảo layout đúng khi có nhiều item
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}

// ============================================================
// ===== PassengerForm và validatePassengers giữ nguyên ======
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerForm(
    passenger: PassengerModel,
    title: String,
    showAdditionalFields: Boolean,
    showAccompanyingAdult: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // State variables for each field
    var firstName by remember { mutableStateOf(passenger.firstName) }
    var lastName by remember { mutableStateOf(passenger.lastName) }
    var dateOfBirth by remember { mutableStateOf(passenger.dateOfBirth) }
    var gender by remember { mutableStateOf(passenger.gender) }
    var countryCode by remember { mutableStateOf(passenger.countryCode) }
    // ---- ID Type State ----
    val idTypes = listOf("DRIVER_LICENSE", "NATIONAL_ID", "OTHER", "PASSPORT")
    var idType by remember { mutableStateOf(passenger.idType) }
    var idTypeExpanded by remember { mutableStateOf(false) }
    // -----------------------
    var idNumber by remember { mutableStateOf(passenger.idNumber) }
    var phone by remember { mutableStateOf(passenger.phone) }
    var email by remember { mutableStateOf(passenger.email) }
    var accompanyingAdultFirstName by remember { mutableStateOf(passenger.accompanyingAdultFirstName) }
    var accompanyingAdultLastName by remember { mutableStateOf(passenger.accompanyingAdultLastName) }

    // Update passenger model when state changes
    LaunchedEffect(firstName) { passenger.firstName = firstName }
    LaunchedEffect(lastName) { passenger.lastName = lastName }
    LaunchedEffect(dateOfBirth) { passenger.dateOfBirth = dateOfBirth }
    LaunchedEffect(gender) { passenger.gender = gender }
    LaunchedEffect(countryCode) { passenger.countryCode = countryCode }
    LaunchedEffect(idType) { passenger.idType = idType } // Update passenger model with selected ID Type
    LaunchedEffect(idNumber) { passenger.idNumber = idNumber }
    LaunchedEffect(phone) { passenger.phone = phone }
    LaunchedEffect(email) { passenger.email = email }
    LaunchedEffect(accompanyingAdultFirstName) { passenger.accompanyingAdultFirstName = accompanyingAdultFirstName }
    LaunchedEffect(accompanyingAdultLastName) { passenger.accompanyingAdultLastName = accompanyingAdultLastName }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp) // Chỉ cần padding bottom ở đây
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 12.dp, top=4.dp) // Điều chỉnh padding tiêu đề form
        )

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF9C27B0),
                focusedLabelColor = Color(0xFF9C27B0),
                cursorColor = Color(0xFF9C27B0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF9C27B0),
                focusedLabelColor = Color(0xFF9C27B0),
                cursorColor = Color(0xFF9C27B0)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date of Birth
        OutlinedTextField(
            value = dateOfBirth,
            onValueChange = {}, // ReadOnly
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            readOnly = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF9C27B0),
                focusedLabelColor = Color(0xFF9C27B0),
                cursorColor = Color(0xFF9C27B0)
            )
        )

        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            try {
                if (dateOfBirth.isNotBlank()) {
                    calendar.time = dateFormat.parse(dateOfBirth) ?: Date()
                }
            } catch (e: Exception) { /* Use current date if parsing fails */ }

            val datePickerDialog = android.app.DatePickerDialog(
                LocalContext.current,
                { _, year, month, day ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, day)
                    dateOfBirth = dateFormat.format(selectedCalendar.time)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            // datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis // Uncomment if needed
            datePickerDialog.setOnDismissListener { showDatePicker = false }
            datePickerDialog.show()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gender
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp), // Thêm chút padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gender:",
                modifier = Modifier.padding(end = 16.dp), // Tăng khoảng cách
                color = Color(0xFF1A237E),
                fontSize = 14.sp // Giảm cỡ chữ label một chút
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "MALE",
                    onClick = { gender = "MALE" },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF9C27B0),
                        unselectedColor = Color(0xFF757575) // Màu xám nhạt hơn khi chưa chọn
                    )
                )
                // Nhấp vào Text cũng chọn RadioButton
                Text(
                    "Male",
                    modifier = Modifier.clickable { gender = "MALE" }.padding(end = 16.dp),
                    color = Color(0xFF1A237E)
                )
                RadioButton(
                    selected = gender == "FEMALE",
                    onClick = { gender = "FEMALE" },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF9C27B0),
                        unselectedColor = Color(0xFF757575)
                    )
                )
                // Nhấp vào Text cũng chọn RadioButton
                Text(
                    "Female",
                    modifier = Modifier.clickable { gender = "FEMALE" },
                    color = Color(0xFF1A237E)
                )
            }
        }

        if (showAdditionalFields) { // Fields specific to Adults
            Spacer(modifier = Modifier.height(8.dp)) // Thêm Spacer trước nhóm field này

            // Country Code
            OutlinedTextField(
                value = countryCode,
                onValueChange = { countryCode = it },
                label = { Text("Country Code") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { // Thêm icon gợi ý
                    Icon(Icons.Default.Public, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- ID Type Dropdown ---
            ExposedDropdownMenuBox(
                expanded = idTypeExpanded,
                onExpandedChange = { idTypeExpanded = !idTypeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = idType.replace("_", " "), // Hiển thị giá trị dễ đọc hơn
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ID Type") },
                    leadingIcon = { // Thêm icon gợi ý
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = idTypeExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF9C27B0),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF9C27B0)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = idTypeExpanded,
                    onDismissRequest = { idTypeExpanded = false }
                ) {
                    idTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.replace("_", " ")) },
                            onClick = {
                                idType = selectionOption
                                idTypeExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
            // ------------------------

            Spacer(modifier = Modifier.height(8.dp))

            // ID Number
            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("ID Number") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { // Thêm icon gợi ý
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )
        }

        if (showAccompanyingAdult) { // Fields specific to Infants
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Accompanying Adult",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(bottom = 8.dp) // Thêm padding bottom
            )

            // Accompanying Adult First Name
            OutlinedTextField(
                value = accompanyingAdultFirstName,
                onValueChange = { accompanyingAdultFirstName = it },
                label = { Text("Accompanying Adult First Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { // Thêm icon gợi ý
                    Icon(Icons.Default.PersonOutline, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accompanying Adult Last Name
            OutlinedTextField(
                value = accompanyingAdultLastName,
                onValueChange = { accompanyingAdultLastName = it },
                label = { Text("Accompanying Adult Last Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { // Thêm icon gợi ý
                    Icon(Icons.Default.PersonOutline, contentDescription = null)
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )
        }
    }
}


private fun validatePassengers(): String? {
    // Validate Adult Passengers
    FlightBookingModel.passengersAdult.forEachIndexed { index, passenger ->
        val passengerLabel = "Adult ${index + 1}"
        if (passenger.firstName.isBlank()) return "$passengerLabel: First Name is required."
        if (passenger.lastName.isBlank()) return "$passengerLabel: Last Name is required."
        if (passenger.dateOfBirth.isBlank()) return "$passengerLabel: Date of Birth is required."
        if (passenger.gender.isBlank()) return "$passengerLabel: Gender is required."
        if (passenger.countryCode.isBlank()) return "$passengerLabel: Country Code is required."
        if (passenger.idType.isBlank()) return "$passengerLabel: ID Type is required."
        if (passenger.idNumber.isBlank()) return "$passengerLabel: ID Number is required."
        if (passenger.phone.isBlank()) return "$passengerLabel: Phone is required."
        // Basic phone number validation (example: must contain only digits and be a certain length)
        // if (!passenger.phone.all { it.isDigit() } || passenger.phone.length < 9) {
        //     return "$passengerLabel: Invalid Phone number format."
        // }
        if (passenger.email.isBlank()) return "$passengerLabel: Email is required."
        if (!Patterns.EMAIL_ADDRESS.matcher(passenger.email).matches()) {
            return "$passengerLabel: Invalid Email format."
        }
    }

    // Validate Child Passengers
    FlightBookingModel.passengersChild.forEachIndexed { index, passenger ->
        val passengerLabel = "Child ${index + 1}"
        if (passenger.firstName.isBlank()) return "$passengerLabel: First Name is required."
        if (passenger.lastName.isBlank()) return "$passengerLabel: Last Name is required."
        if (passenger.dateOfBirth.isBlank()) return "$passengerLabel: Date of Birth is required."
        if (passenger.gender.isBlank()) return "$passengerLabel: Gender is required."
    }

    // Validate Infant Passengers
    FlightBookingModel.passengersInfant.forEachIndexed { index, passenger ->
        val passengerLabel = "Infant ${index + 1}"
        if (passenger.firstName.isBlank()) return "$passengerLabel: First Name is required."
        if (passenger.lastName.isBlank()) return "$passengerLabel: Last Name is required."
        if (passenger.dateOfBirth.isBlank()) return "$passengerLabel: Date of Birth is required."
        if (passenger.gender.isBlank()) return "$passengerLabel: Gender is required."
        if (passenger.accompanyingAdultFirstName.isBlank()) return "$passengerLabel: Accompanying Adult First Name is required."
        if (passenger.accompanyingAdultLastName.isBlank()) return "$passengerLabel: Accompanying Adult Last Name is required."
    }

    Log.d("Validation", "All passengers validated successfully.")
    return null // Return null if all validations pass
}