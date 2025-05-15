package com.vietjoke.vn.Activities.Profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.PassengerCountModel
import com.vietjoke.vn.retrofit.ResponseDTO.UserProfileData
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightDataDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfileData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isUpdatingAvatar by remember { mutableStateOf(false) }

    // Function to clear all data when logging out
    fun clearAllData() {
        // Clear UserModel data
        UserModel.clearToken()
        
        // Clear FlightBookingModel data
        FlightBookingModel.clear()
        FlightBookingModel.clearSessionData()
        
        // Clear PassengerCountModel data
        PassengerCountModel.clear()
        
        // Clear SelectFlight data
        SelectFlightResponseDTO.clear()
        SelectFlightDataDTO.clear()
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch {
                try {
                    isUpdatingAvatar = true
                    val token = UserModel.token
                    
                    // Convert Uri to File
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val file = File(context.cacheDir, "temp_avatar.jpg")
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Create MultipartBody.Part
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)

                    // Call API to update avatar
                    val response = RetrofitInstance.authApi.updateAvatar(
                        authorization = "$token",
                        avatar = avatarPart
                    )

                    if (response.status == 200) {
                        userProfile = response.data
                        Toast.makeText(context, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, response.message ?: "Cập nhật ảnh đại diện thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUpdatingAvatar = false
                }
            }
        }
    }

    // Fetch user profile when screen is created
    LaunchedEffect(Unit) {
        try {
            val token = UserModel.token
            val response = RetrofitInstance.authApi.getUserProfile(
                authorization = "$token"
            )
            if (response.status == 200) {
                userProfile = response.data
                UserModel.updateProfile(response.data)
            } else {
                error = response.message
            }
        } catch (e: Exception) {
            error = "Lỗi kết nối: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp)
                    .offset(y = (-4).dp),
                title = { Text("Tài khoản")},
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF9C27B0)
                )
            } else if (error != null) {
                Text(
                    text = error!!,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header with Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUpdatingAvatar) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color.White
                            )
                        } else if (userProfile?.avatarUrl != null) {
                            AsyncImage(
                                model = userProfile?.avatarUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                            
                            // Camera icon overlay - only show when no avatar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name
                    Text(
                        text = "${userProfile?.firstName} ${userProfile?.lastName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )

                    // Username
                    Text(
                        text = "@${userProfile?.username}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Information Cards
                    ProfileInfoCard(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value = userProfile?.email ?: "",
                        isVerified = userProfile?.emailVerified ?: false
                    )

                    ProfileInfoCard(
                        icon = Icons.Default.Phone,
                        title = "Số điện thoại",
                        value = userProfile?.phone ?: ""
                    )

                    ProfileInfoCard(
                        icon = Icons.Default.Cake,
                        title = "Ngày sinh",
                        value = userProfile?.dateOfBirth ?: ""
                    )

                    if (!userProfile?.address.isNullOrEmpty()) {
                        ProfileInfoCard(
                            icon = Icons.Default.LocationOn,
                            title = "Địa chỉ",
                            value = userProfile?.address ?: ""
                        )
                    }

                    ProfileInfoCard(
                        icon = Icons.Default.Badge,
                        title = "Vai trò",
                        value = when (userProfile?.roleCode) {
                            "CUSTOMER" -> "Khách hàng"
                            "ADMIN" -> "Quản trị viên"
                            else -> userProfile?.roleCode ?: ""
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout Button
                    Button(
                        onClick = {
                            clearAllData()
                            onLogoutClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đăng xuất",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    isVerified: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (isVerified) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 