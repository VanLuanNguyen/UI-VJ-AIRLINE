package com.vietjoke.vn.Activities.Booking

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vietjoke.vn.model.FlightBookingModel
import com.vietjoke.vn.model.PassengerModel
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

class InsuranceSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsuranceSelectionScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceSelectionScreen() {
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
    val selectedInsurance = remember { mutableStateMapOf<Pair<Int, Int>, AddonDTO?>() }
    
    // Add pagination state
    var currentPage by remember { mutableStateOf(1) }
    var hasMorePages by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }

    // Fetch addons for each leg
    suspend fun fetchAddons(flightNumber: String, isOutbound: Boolean, page: Int = 1, append: Boolean = false) {
        try {
            val sessionToken = FlightBookingModel.sessionToken
            if (sessionToken == null) {
                Toast.makeText(context, "Lỗi: Session token không hợp lệ", Toast.LENGTH_SHORT).show()
                return
            }
            val response = RetrofitInstance.addonApi.getAddons(
                addonCode = "INSURANCE",
                sessionToken = sessionToken,
                flightNumber = flightNumber,
                pageNumber = page,
                pageSize = 10
            )
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.status == 200) {
                    val flightInfo = apiResponse.data.flight.firstOrNull()
                    val addons = flightInfo?.addonDTOs?.content ?: emptyList()
                    val totalPages = flightInfo?.addonDTOs?.page?.totalPages ?: 0
                    
                    if (isOutbound) {
                        outboundAddons = if (append) outboundAddons + addons else addons
                        outboundRouteCode = flightInfo?.route?.routeCode
                    } else {
                        returnAddons = if (append) returnAddons + addons else addons
                        returnRouteCode = flightInfo?.route?.routeCode
                    }
                    
                    hasMorePages = page < totalPages
                } else {
                    Toast.makeText(context, apiResponse.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Lỗi khi lấy danh sách bảo hiểm", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("InsuranceSelection", "Error fetching addons", e)
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
    val selectedAddon = selectedInsurance[Pair(currentLegIndex, currentPassengerIndex)]

    // Function to load more items
    fun loadMore() {
        if (!isLoadingMore && hasMorePages) {
            isLoadingMore = true
            coroutineScope.launch {
                val nextPage = currentPage + 1
                val flightNumber = if (currentLegIndex == 0) FlightBookingModel.flightNumber else FlightBookingModel.returnFlightNumber
                fetchAddons(flightNumber ?: "", currentLegIndex == 0, nextPage, true)
                currentPage = nextPage
                isLoadingMore = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn bảo hiểm du lịch") },
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
                    // Xác nhận mua bảo hiểm: tổng hợp kết quả và trả về
                    val resultList = mutableListOf<com.vietjoke.vn.model.SelectedInsuranceInfoForLeg>()
                    val legs = if (isRoundTrip) listOf(0, 1) else listOf(0)
                    for (leg in legs) {
                        val flightNumber = if (leg == 0) FlightBookingModel.flightNumber else FlightBookingModel.returnFlightNumber
                        val routeCode = if (leg == 0) outboundRouteCode else returnRouteCode
                        val originCode = routeCode?.split("-")?.getOrNull(0)
                        val destinationCode = routeCode?.split("-")?.getOrNull(1)
                        val insuranceByPassenger = passengers.mapIndexed { idx, _ ->
                            val addon = selectedInsurance[Pair(leg, idx)]
                            idx to (if (addon != null) listOf(addon) else emptyList())
                        }.toMap()
                        resultList.add(
                            com.vietjoke.vn.model.SelectedInsuranceInfoForLeg(
                                flightNumber = flightNumber,
                                originCode = originCode,
                                destinationCode = destinationCode,
                                insuranceByPassengerIndex = insuranceByPassenger
                            )
                        )
                    }
                    val result = com.vietjoke.vn.model.InsuranceSelectionResult(resultList)
                    val intent = android.content.Intent().putExtra("INSURANCE_SELECTION_RESULT", result)
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
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
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
                                text = "${pax.passengerType.take(1)} ${pax.firstName.take(1)} ${pax.lastName.take(1)}",
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
                // Insurance options
                Text(
                    "Chọn bảo hiểm du lịch",
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
                        InsuranceOptionCard(
                            addon = addon,
                            isSelected = selectedAddon?.id == addon.id,
                            onSelect = {
                                selectedInsurance[Pair(currentLegIndex, currentPassengerIndex)] = addon
                            }
                        )
                    }
                    
                    // Add loading indicator at the end
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }
                
                // Add scroll detection
                LaunchedEffect(currentAddons.size) {
                    if (currentAddons.isNotEmpty() && !isLoadingMore && hasMorePages) {
                        loadMore()
                    }
                }
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF9C27B0))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceOptionCard(addon: AddonDTO, isSelected: Boolean, onSelect: () -> Unit) {
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
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = Color(0xFFD50000),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
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
            }
        }
    }
} 