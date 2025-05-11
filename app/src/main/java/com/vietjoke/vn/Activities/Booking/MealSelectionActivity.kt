package com.vietjoke.vn.Activities.Booking

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
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
import com.vietjoke.vn.model.PassengerModel
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch
import com.vietjoke.vn.model.MealSelectionResult
import com.vietjoke.vn.model.SelectedMealInfoForLeg
import com.vietjoke.vn.model.MealItem

class MealSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MealSelectionScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSelectionScreen() {
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
    val selectedMeals = remember { mutableStateMapOf<Pair<Int, Int>, MutableMap<Int, Int>>() }

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
                addonCode = "MEAL",
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
                Toast.makeText(context, "Lỗi khi lấy danh sách suất ăn", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MealSelection", "Error fetching addons", e)
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

    // Helper để lấy số lượng đã chọn
    fun getQuantity(addonId: Int): Int {
        return selectedMeals.getOrPut(Pair(currentLegIndex, currentPassengerIndex)) { mutableMapOf() }[addonId] ?: 0
    }

    // Helper để set số lượng
    fun setQuantity(addonId: Int, quantity: Int) {
        val key = Pair(currentLegIndex, currentPassengerIndex)
        val map = selectedMeals.getOrPut(key) { mutableMapOf() }.toMutableMap()
        if (quantity > 0) map[addonId] = quantity else map.remove(addonId)
        selectedMeals[key] = map // Gán lại để trigger recomposition
    }

    // Helper kiểm tra bước cuối
    fun isLastStep(): Boolean {
        return (currentLegIndex == (if (isRoundTrip) 1 else 0)) && (currentPassengerIndex == passengers.size - 1)
    }

    // Helper chuyển bước hoặc trả kết quả
    fun moveToNextStepOrFinish() {
        if (isLastStep()) {
            // Chuẩn bị kết quả
            val resultList = mutableListOf<SelectedMealInfoForLeg>()
            val legs = if (isRoundTrip) listOf(0, 1) else listOf(0)
            for (leg in legs) {
                val flightNumber = if (leg == 0) FlightBookingModel.flightNumber else FlightBookingModel.returnFlightNumber
                val routeCode = if (leg == 0) outboundRouteCode else returnRouteCode
                val originCode = routeCode?.split("-")?.getOrNull(0)
                val destinationCode = routeCode?.split("-")?.getOrNull(1)
                val itemsByPassenger = passengers.mapIndexed { idx, _ ->
                    val map = selectedMeals[Pair(leg, idx)] ?: emptyMap()
                    idx to map.mapNotNull { addonEntry ->
                        val addon = (if (leg == 0) outboundAddons else returnAddons).find { it.id == addonEntry.key }
                        if (addon != null) MealItem(addon, addonEntry.value) else null
                    }
                }.toMap()
                resultList.add(
                    SelectedMealInfoForLeg(
                        flightNumber = flightNumber,
                        originCode = originCode,
                        destinationCode = destinationCode,
                        itemsByPassengerIndex = itemsByPassenger
                    )
                )
            }
            val result = MealSelectionResult(resultList)
            val intent = Intent().putExtra("MEAL_SELECTION_RESULT", result)
            (context as? Activity)?.apply {
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            // Chuyển bước
            if (isRoundTrip && currentLegIndex == 0) {
                currentLegIndex = 1
            } else {
                currentLegIndex = 0
                currentPassengerIndex++
            }
        }
    }

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
                title = { Text("Chọn suất ăn") },
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
                onClick = { moveToNextStepOrFinish() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                Text(if (isLastStep()) "Xác nhận" else "Tiếp tục", color = Color.Black, fontWeight = FontWeight.Bold)
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
                // Hiển thị danh sách món đã chọn
                val selectedMap = selectedMeals[Pair(currentLegIndex, currentPassengerIndex)] ?: emptyMap()
                if (selectedMap.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("Món đã chọn:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        selectedMap.entries.forEach { entry ->
                            val addon = currentAddons.find { it.id == entry.key }
                            if (addon != null) {
                                Text("- ${addon.name} x${entry.value}", fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
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
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Meal options
                Text(
                    "Chọn suất ăn",
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
                        MealOptionCard(
                            addon = addon,
                            quantity = getQuantity(addon.id),
                            onIncrease = {
                                if (getQuantity(addon.id) < addon.maxQuantity) setQuantity(addon.id, getQuantity(addon.id) + 1)
                            },
                            onDecrease = {
                                if (getQuantity(addon.id) > 0) setQuantity(addon.id, getQuantity(addon.id) - 1)
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
fun MealOptionCard(
    addon: AddonDTO,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            // Stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, enabled = quantity > 0) {
                    Icon(Icons.Filled.Remove, contentDescription = "Giảm")
                }
                Text(quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onIncrease, enabled = quantity < addon.maxQuantity) {
                    Icon(Icons.Filled.Add, contentDescription = "Tăng")
                }
            }
        }
    }
} 