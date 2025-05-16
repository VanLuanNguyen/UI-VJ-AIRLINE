package com.vietjoke.vn.Activities.Booking

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.PassengerAdultModel
import com.vietjoke.vn.model.PassengerChildModel
import com.vietjoke.vn.model.PassengerInfantModel
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class OtherServiceSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OtherServiceSelectionScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherServiceSelectionScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isRoundTrip = FlightBookingModel.isRoundTrip
    val passengers = remember {
        FlightBookingModel.passengersAdult + FlightBookingModel.passengersChild
    }
    var currentLegIndex by remember { mutableStateOf(0) } // 0: đi, 1: về
    var currentPassengerIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var outboundAddons by remember { mutableStateOf<List<AddonDTO>>(emptyList()) }
    var returnAddons by remember { mutableStateOf<List<AddonDTO>>(emptyList()) }
    var outboundRouteCode by remember { mutableStateOf<String?>(null) }
    var returnRouteCode by remember { mutableStateOf<String?>(null) }
    val selectedService = remember { mutableStateMapOf<Pair<Int, Int>, AddonDTO?>() }

    // Fetch addons for each leg
    suspend fun fetchAddons(flightNumber: String, isOutbound: Boolean) {
        try {
            val sessionToken = FlightBookingModel.sessionToken
            if (sessionToken == null) {
                Toast.makeText(context, "Lỗi: Session token không hợp lệ", Toast.LENGTH_SHORT).show()
                return
            }
            val response = RetrofitInstance.addonApi.getAddons(
                authorization = UserModel.token ?: "",
                addonCode = "OTHER",
                sessionToken = sessionToken,
                flightNumber = flightNumber
            )
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.status == 200) {
                    val flightInfo = apiResponse.data.flight.firstOrNull()
                    val addons = flightInfo?.addonDTOs?.content ?: emptyList()
                    if (isOutbound) {
                        outboundAddons = addons
                        outboundRouteCode = flightInfo?.route?.routeCode
                    } else {
                        returnAddons = addons
                        returnRouteCode = flightInfo?.route?.routeCode
                    }
                } else {
                    Toast.makeText(context, apiResponse.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Lỗi khi lấy danh sách dịch vụ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("OtherServiceSelection", "Error fetching addons", e)
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch on launch
    LaunchedEffect(Unit) {
        isLoading = true
        coroutineScope.launch {
            fetchAddons(FlightBookingModel.flightNumber ?: "", true)
            if (isRoundTrip) fetchAddons(FlightBookingModel.returnFlightNumber ?: "", false)
            isLoading = false
        }
    }

    val currentAddons = if (currentLegIndex == 0) outboundAddons else returnAddons
    val currentPassenger = passengers.getOrNull(currentPassengerIndex)
    val selectedAddon = selectedService[Pair(currentLegIndex, currentPassengerIndex)]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dịch vụ khác") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    // Xác nhận mua dịch vụ khác: tổng hợp kết quả và trả về
                    val resultList = mutableListOf<com.vietjoke.vn.model.SelectedOtherServiceInfoForLeg>()
                    val legs = if (isRoundTrip) listOf(0, 1) else listOf(0)
                    for (leg in legs) {
                        val flightNumber = if (leg == 0) FlightBookingModel.flightNumber else FlightBookingModel.returnFlightNumber
                        val routeCode = if (leg == 0) outboundRouteCode else returnRouteCode
                        val originCode = routeCode?.split("-")?.getOrNull(0)
                        val destinationCode = routeCode?.split("-")?.getOrNull(1)
                        val serviceByPassenger = passengers.mapIndexed { idx, _ ->
                            val addon = selectedService[Pair(leg, idx)]
                            idx to (if (addon != null) listOf(addon) else emptyList())
                        }.toMap()
                        resultList.add(
                            com.vietjoke.vn.model.SelectedOtherServiceInfoForLeg(
                                flightNumber = flightNumber,
                                originCode = originCode,
                                destinationCode = destinationCode,
                                serviceByPassengerIndex = serviceByPassenger
                            )
                        )
                    }
                    val result = com.vietjoke.vn.model.OtherServiceSelectionResult(resultList)
                    val intent = android.content.Intent().putExtra("OTHER_SERVICE_SELECTION_RESULT", result)
                    (context as? android.app.Activity)?.apply {
                        setResult(android.app.Activity.RESULT_OK, intent)
                        finish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Xác nhận", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Tabs for legs
                if (isRoundTrip) {
                    TabRow(selectedTabIndex = currentLegIndex, containerColor = Color.White) {
                        Tab(selected = currentLegIndex == 0, onClick = { currentLegIndex = 0 }, text = { Text("Chuyến đi") })
                        Tab(selected = currentLegIndex == 1, onClick = { currentLegIndex = 1 }, text = { Text("Chuyến về") })
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Passenger selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD50000), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    passengers.forEachIndexed { idx, pax ->
                        val isSelected = idx == currentPassengerIndex
                        val (passengerType, firstName, lastName) = when (pax) {
                            is PassengerAdultModel -> Triple("A", pax.firstName, pax.lastName)
                            is PassengerChildModel -> Triple("C", pax.firstName, pax.lastName)
                            is PassengerInfantModel -> Triple("I", pax.firstName, pax.lastName)
                            else -> Triple("", "", "")
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .background(
                                    if (isSelected) Color.White else Color(0xFFD50000),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { currentPassengerIndex = idx }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$passengerType ${firstName.take(1)} ${lastName.take(1)}",
                                color = if (isSelected) Color(0xFFD50000) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Flight info
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = if (currentLegIndex == 0) {
                                outboundRouteCode ?: (FlightBookingModel.flightNumber ?: "")
                            } else {
                                returnRouteCode ?: (FlightBookingModel.returnFlightNumber ?: "")
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (selectedAddon != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                Spacer(Modifier.width(4.dp))
                                Text("Bạn đã chọn ${selectedAddon.name}", color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Service options
                Text(
                    "Chọn dịch vụ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(currentAddons) { addon ->
                        ServiceOptionCard(
                            addon = addon,
                            isSelected = selectedAddon?.id == addon.id,
                            onSelect = {
                                selectedService[Pair(currentLegIndex, currentPassengerIndex)] = addon
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceOptionCard(addon: AddonDTO, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh dịch vụ
            addon.imgUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(url),
                    contentDescription = addon.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            } ?: run {
                Icon(
                    Icons.Filled.MoreHoriz,
                    contentDescription = null,
                    tint = Color(0xFFD50000),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(addon.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                addon.description?.let { desc ->
                    Text(desc, fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    addon.price.toString() + " " + addon.currency,
                    color = Color(0xFFD50000),
                    fontWeight = FontWeight.Bold
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    Icons.Filled.PlusOne,
                    contentDescription = null,
                    tint = Color(0xFFD50000),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
} 