package com.vietjoke.vn.Activities.SearchFlight

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vietjoke.vn.retrofit.ResponseDTO.AirportDetailDTO
import com.vietjoke.vn.retrofit.ResponseDTO.CountryDetailDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ProvinceDetailDTO
import com.vietjoke.vn.retrofit.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportSelectionDialog(
    onDismiss: () -> Unit,
    onAirportSelected: (AirportDetailDTO) -> Unit
) {
    var countries by remember { mutableStateOf<List<CountryDetailDTO>>(emptyList()) }
    var selectedCountry by remember { mutableStateOf<CountryDetailDTO?>(null) }
    var provinces by remember { mutableStateOf<List<ProvinceDetailDTO>>(emptyList()) }
    var selectedProvince by remember { mutableStateOf<ProvinceDetailDTO?>(null) }
    var airports by remember { mutableStateOf<List<AirportDetailDTO>>(emptyList()) }
    var selectedAirport by remember { mutableStateOf<AirportDetailDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var airportResponse by remember { mutableStateOf<List<AirportDetailDTO>>(emptyList()) }

    var expandedCountry by remember { mutableStateOf(false) }
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedAirport by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.flightApi.getAirports()
            if (response.status == 200) {
                airportResponse = response.data
                val uniqueCountries = response.data.map { it.province.country }.distinctBy { it.id }
                countries = uniqueCountries
            } else {
                error = response.message
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn sân bay") },
        text = {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Lỗi: $error")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Country Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCountry,
                        onExpandedChange = { expandedCountry = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCountry?.countryName ?: "Chọn quốc gia",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountry) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCountry,
                            onDismissRequest = { expandedCountry = false }
                        ) {
                            countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country.countryName) },
                                    onClick = {
                                        selectedCountry = country
                                        selectedProvince = null
                                        selectedAirport = null
                                        airports = emptyList()
                                        provinces = airportResponse
                                            .filter { it.province.country.id == country.id }
                                            .map { it.province }
                                            .distinctBy { it.id }
                                        expandedCountry = false
                                    }
                                )
                            }
                        }
                    }

                    // Province Dropdown
                    if (selectedCountry != null) {
                        ExposedDropdownMenuBox(
                            expanded = expandedProvince,
                            onExpandedChange = { expandedProvince = it }
                        ) {
                            OutlinedTextField(
                                value = selectedProvince?.provinceName ?: "Chọn thành phố",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvince) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedProvince,
                                onDismissRequest = { expandedProvince = false }
                            ) {
                                provinces.forEach { province ->
                                    DropdownMenuItem(
                                        text = { Text(province.provinceName) },
                                        onClick = {
                                            selectedProvince = province
                                            selectedAirport = null
                                            airports = airportResponse
                                                .filter { it.province.id == province.id }
                                            expandedProvince = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Airport Dropdown
                    if (selectedProvince != null) {
                        ExposedDropdownMenuBox(
                            expanded = expandedAirport,
                            onExpandedChange = { expandedAirport = it }
                        ) {
                            OutlinedTextField(
                                value = selectedAirport?.let { "${it.airportName} (${it.airportCode})" } ?: "Chọn sân bay",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAirport) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedAirport,
                                onDismissRequest = { expandedAirport = false }
                            ) {
                                airports.forEach { airport ->
                                    DropdownMenuItem(
                                        text = { Text("${airport.airportName} (${airport.airportCode})") },
                                        onClick = {
                                            selectedAirport = airport
                                            onAirportSelected(airport)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
} 