package com.vietjoke.vn.Activities.Profile

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import com.vietjoke.vn.retrofit.ResponseDTO.UserProfileData
import com.vietjoke.vn.retrofit.ResponseDTO.UserUpdateRequestDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.vietjoke.vn.utils.LoginPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    userProfile: UserProfileData
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    
    // Form state
    var firstName by remember { mutableStateOf(userProfile.firstName) }
    var lastName by remember { mutableStateOf(userProfile.lastName) }
    var phone by remember { mutableStateOf(userProfile.phone) }
    var dateOfBirth by remember { mutableStateOf(userProfile.dateOfBirth) }
    var address by remember { mutableStateOf(userProfile.address ?: "") }
    
    // Password change state
    var showPasswordFields by remember { mutableStateOf(false) }
    var previousPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Validation state
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var dateOfBirthError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var previousPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateForm(): Boolean {
        var isValid = true
        
        // Validate first name
        if (firstName.length > 50) {
            firstNameError = "First name must not exceed 50 characters"
            isValid = false
        } else {
            firstNameError = null
        }
        
        // Validate last name
        if (lastName.length > 50) {
            lastNameError = "Last name must not exceed 50 characters"
            isValid = false
        } else {
            lastNameError = null
        }
        
        // Validate phone number
        val phoneRegex = "^((\\+84)|0)(3[2-9]|5[2-9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$".toRegex()
        if (!phone.matches(phoneRegex)) {
            phoneError = "Invalid Vietnamese phone number format"
            isValid = false
        } else {
            phoneError = null
        }
        
        // Validate date of birth
        try {
            val dob = LocalDate.parse(dateOfBirth)
            if (dob.isAfter(LocalDate.now())) {
                dateOfBirthError = "Date of birth must be a past date"
                isValid = false
            } else {
                dateOfBirthError = null
            }
        } catch (e: Exception) {
            dateOfBirthError = "Invalid date format"
            isValid = false
        }
        
        // Validate address
        if (address.length > 255) {
            addressError = "Address must not exceed 255 characters"
            isValid = false
        } else {
            addressError = null
        }
        
        // Validate password fields if shown
        if (showPasswordFields) {
            if (previousPassword.isBlank()) {
                previousPasswordError = "Previous password is required"
                isValid = false
            } else {
                previousPasswordError = null
            }
            
            if (newPassword.isNotBlank()) {
                val passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$".toRegex()
                if (newPassword.length < 8) {
                    newPasswordError = "Password must be at least 8 characters long"
                    isValid = false
                } else if (!newPassword.matches(passwordRegex)) {
                    newPasswordError = "Password must contain at least one uppercase letter, one number, and one special character"
                    isValid = false
                } else {
                    newPasswordError = null
                }
                
                if (newPassword != confirmPassword) {
                    confirmPasswordError = "Passwords do not match"
                    isValid = false
                } else {
                    confirmPasswordError = null
                }
            }
        }
        
        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa thông tin") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    isError = firstNameError != null,
                    supportingText = { firstNameError?.let { error -> Text(error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    isError = lastNameError != null,
                    supportingText = { lastNameError?.let { error -> Text(error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { error -> Text(error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = dateOfBirthError != null,
                    supportingText = { dateOfBirthError?.let { error -> Text(error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = addressError != null,
                    supportingText = { addressError?.let { error -> Text(error) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Password Change Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Change Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = showPasswordFields,
                        onCheckedChange = { showPasswordFields = it }
                    )
                }
                
                if (showPasswordFields) {
                    // Previous Password
                    OutlinedTextField(
                        value = previousPassword,
                        onValueChange = { previousPassword = it },
                        label = { Text("Previous Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        isError = previousPasswordError != null,
                        supportingText = { previousPasswordError?.let { error -> Text(error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        isError = newPasswordError != null,
                        supportingText = { newPasswordError?.let { error -> Text(error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        isError = confirmPasswordError != null,
                        supportingText = { confirmPasswordError?.let { error -> Text(error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save Button
                Button(
                    onClick = {
                        if (validateForm()) {
                            coroutineScope.launch {
                                try {
                                    isLoading = true
                                    val request = UserUpdateRequestDTO(
                                        firstName = firstName,
                                        lastName = lastName,
                                        phone = phone,
                                        dateOfBirth = dateOfBirth,
                                        address = address.takeIf { it.isNotBlank() },
                                        previousPassword = if (showPasswordFields) previousPassword else null,
                                        password = if (showPasswordFields) newPassword else null,
                                        confirmPassword = if (showPasswordFields) confirmPassword else null
                                    )
                                    
                                    val response = RetrofitInstance.authApi.updateProfile(
                                        authorization = "${UserModel.token}",
                                        request = request
                                    )
                                    
                                    if (response.isSuccessful) {
                                        // Update user profile in UserModel
                                        response.body()?.data?.let { updatedProfile ->
                                            UserModel.updateProfile(updatedProfile)
                                        }

                                        // If password was changed and remember me is enabled, update saved password
                                        if (showPasswordFields && LoginPreferences.isRememberMeEnabled(context)) {
                                            val savedUsername = LoginPreferences.getSavedUsername(context)
                                            if (savedUsername != null) {
                                                LoginPreferences.saveLoginInfo(
                                                    context = context,
                                                    username = savedUsername,
                                                    password = newPassword,
                                                    rememberMe = true
                                                )
                                            }
                                        }

                                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    } else {
                                        // Handle error response
                                        val errorBody = response.errorBody()?.string()
                                        if (errorBody != null) {
                                            try {
                                                val errorResponse = Json { ignoreUnknownKeys = true }
                                                    .decodeFromString<ErrorResponse>(errorBody)
                                                val errorMessage = errorResponse.errors?.firstOrNull()?.message 
                                                    ?: errorResponse.message 
                                                    ?: "Failed to update profile"
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Save Changes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
} 