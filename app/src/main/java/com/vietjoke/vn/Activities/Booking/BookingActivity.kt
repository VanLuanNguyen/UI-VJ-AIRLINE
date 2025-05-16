package com.vietjoke.vn.Activities.Booking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.vietjoke.vn.model.PassengerAdultModel
import com.vietjoke.vn.model.PassengerChildModel
import com.vietjoke.vn.model.PassengerInfantModel
import java.text.SimpleDateFormat
import java.util.*
import com.vietjoke.vn.retrofit.RetrofitInstance
import com.vietjoke.vn.retrofit.ResponseDTO.BookingRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import com.vietjoke.vn.retrofit.ResponseDTO.BookingResponseDTO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import com.vietjoke.vn.retrofit.ResponseDTO.CountryDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.zIndex
import com.vietjoke.vn.model.UserModel

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
                    PassengerSection(
                        title = "Adult Passengers",
                        icon = Icons.Default.Person,
                        startExpanded = true
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

                Spacer(modifier = Modifier.height(8.dp))

                // Child Passengers
                if (FlightBookingModel.passengersChild.isNotEmpty()) {
                    PassengerSection(
                        title = "Child Passengers",
                        icon = Icons.Default.ChildCare,
                        startExpanded = false
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

                Spacer(modifier = Modifier.height(8.dp))

                // Infant Passengers
                if (FlightBookingModel.passengersInfant.isNotEmpty()) {
                    PassengerSection(
                        title = "Infant Passengers",
                        icon = Icons.Default.ChildFriendly,
                        startExpanded = false
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
                                    val response = RetrofitInstance.bookingApi.createBooking(
                                        authorization = UserModel.token ?: "",
                                        request = bookingRequest
                                    )

                                    if (response.isSuccessful) {
                                        response.body()?.let { bookingResponse ->
                                            when (bookingResponse.status) {
                                                200 -> {
                                                    Toast.makeText(context, bookingResponse.message, Toast.LENGTH_SHORT).show()
                                                    bookingResponse.data?.sessionToken?.let { newSessionToken ->
                                                        FlightBookingModel.sessionToken = newSessionToken
                                                    }
                                                    val intent = Intent(context, AncillaryActivity::class.java)
                                                    context.startActivity(intent)
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
                        color = Color.White
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
    startExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(startExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
        ,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp)
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
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(0xFF9C27B0)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
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
    passenger: Any,
    title: String,
    showAdditionalFields: Boolean,
    showAccompanyingAdult: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var countryCode by remember { mutableStateOf("") }
    var countrySearchText by remember { mutableStateOf("") }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var filteredCountries by remember { mutableStateOf(emptyList<CountryDTO>()) }
    val coroutineScope = rememberCoroutineScope()
    val countries by countries.collectAsState()

    // Load countries when the form is first displayed
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            loadCountries()
        }
    }

    // Update filtered countries when search text changes
    LaunchedEffect(countrySearchText, countries) {
        filteredCountries = if (countrySearchText.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.countryName.contains(countrySearchText, ignoreCase = true)
            }
        }
    }

    // State variables for each field
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("MALE") }
    var idType by remember { mutableStateOf("") }
    var idTypeExpanded by remember { mutableStateOf(false) }
    var idNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var accompanyingAdultFirstName by remember { mutableStateOf("") }
    var accompanyingAdultLastName by remember { mutableStateOf("") }

    // Initialize state based on passenger type
    LaunchedEffect(passenger) {
        when (passenger) {
            is PassengerAdultModel -> {
                firstName = passenger.firstName
                lastName = passenger.lastName
                dateOfBirth = passenger.dateOfBirth
                selectedGender = passenger.gender
                countryCode = passenger.countryCode
                idType = passenger.idType
                idNumber = passenger.idNumber
                phone = passenger.phone
                email = passenger.email
            }
            is PassengerChildModel -> {
                firstName = passenger.firstName
                lastName = passenger.lastName
                dateOfBirth = passenger.dateOfBirth
                selectedGender = passenger.gender
            }
            is PassengerInfantModel -> {
                firstName = passenger.firstName
                lastName = passenger.lastName
                dateOfBirth = passenger.dateOfBirth
                selectedGender = passenger.gender
                accompanyingAdultFirstName = passenger.accompanyingAdultFirstName
                accompanyingAdultLastName = passenger.accompanyingAdultLastName
            }
        }
    }

    // Update passenger model when state changes
    LaunchedEffect(firstName, lastName, dateOfBirth, selectedGender, countryCode, idType, idNumber, phone, email, accompanyingAdultFirstName, accompanyingAdultLastName) {
        when (passenger) {
            is PassengerAdultModel -> {
                passenger.firstName = firstName
                passenger.lastName = lastName
                passenger.dateOfBirth = dateOfBirth
                passenger.gender = selectedGender
                passenger.countryCode = countryCode
                passenger.idType = idType
                passenger.idNumber = idNumber
                passenger.phone = phone
                passenger.email = email
            }
            is PassengerChildModel -> {
                passenger.firstName = firstName
                passenger.lastName = lastName
                passenger.dateOfBirth = dateOfBirth
                passenger.gender = selectedGender
            }
            is PassengerInfantModel -> {
                passenger.firstName = firstName
                passenger.lastName = lastName
                passenger.dateOfBirth = dateOfBirth
                passenger.gender = selectedGender
                passenger.accompanyingAdultFirstName = accompanyingAdultFirstName
                passenger.accompanyingAdultLastName = accompanyingAdultLastName
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 12.dp, top=4.dp)
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
            onValueChange = {},
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
            datePickerDialog.setOnDismissListener { showDatePicker = false }
            datePickerDialog.show()
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gender
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gender:",
                modifier = Modifier.padding(end = 16.dp),
                color = Color(0xFF1A237E),
                fontSize = 14.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGender == "MALE",
                    onClick = { selectedGender = "MALE" },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF9C27B0),
                        unselectedColor = Color(0xFF757575)
                    )
                )
                Text(
                    "Male",
                    modifier = Modifier.clickable { selectedGender = "MALE" }.padding(end = 16.dp),
                    color = Color(0xFF1A237E)
                )
                RadioButton(
                    selected = selectedGender == "FEMALE",
                    onClick = { selectedGender = "FEMALE" },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF9C27B0),
                        unselectedColor = Color(0xFF757575)
                    )
                )
                Text(
                    "Female",
                    modifier = Modifier.clickable { selectedGender = "FEMALE" },
                    color = Color(0xFF1A237E)
                )
            }
        }

        if (showAdditionalFields) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = countrySearchText,
                    onValueChange = { 
                        countrySearchText = it
                        showCountryDropdown = true
                    },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Public, contentDescription = null)
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF9C27B0),
                        focusedLabelColor = Color(0xFF9C27B0),
                        cursorColor = Color(0xFF9C27B0)
                    ),
                    singleLine = true
                )

                if (showCountryDropdown && filteredCountries.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .offset(y = 56.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn {
                            items(filteredCountries) { country ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            countrySearchText = country.countryName
                                            countryCode = country.countryCode
                                            showCountryDropdown = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = country.countryName,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (country != filteredCountries.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showCountryDropdown = false
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = idTypeExpanded,
                onExpandedChange = { idTypeExpanded = !idTypeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = idType.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ID Type") },
                    leadingIcon = {
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
                    val idTypes = listOf("DRIVER_LICENSE", "NATIONAL_ID", "OTHER", "PASSPORT")
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("ID Number") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
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

        if (showAccompanyingAdult) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Accompanying Adult",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = accompanyingAdultFirstName,
                onValueChange = { accompanyingAdultFirstName = it },
                label = { Text("Accompanying Adult First Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
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

            OutlinedTextField(
                value = accompanyingAdultLastName,
                onValueChange = { accompanyingAdultLastName = it },
                label = { Text("Accompanying Adult Last Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
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

private val _countries = MutableStateFlow<List<CountryDTO>>(emptyList())
val countries: StateFlow<List<CountryDTO>> = _countries.asStateFlow()
private suspend fun loadCountries() {
    try {
        val response = RetrofitInstance.countryApi.getCountries()
        if (response.isSuccessful) {
            response.body()?.let { countryResponse ->
                if (countryResponse.status == 200) {
                    _countries.update { countryResponse.data }
                } else {
                    Log.e("CountryError", "Failed to load countries: ${countryResponse.message}")
                }
            }
        } else {
            Log.e("CountryError", "Failed to load countries: ${response.message()}")
        }
    } catch (e: Exception) {
        Log.e("CountryError", "Failed to load countries", e)
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
    return null
}