package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class SelectFlightRequestDTO(
    val sessionToken: String,
    val flights: List<FlightSelectionDTO>
)

@Serializable
data class FlightSelectionDTO(
    val flightNumber: String,
    val fareCode: String
)

@Serializable
data class SelectFlightResponseDTO(
    val status: Int,
    val message: String,
    val data: SelectFlightDataDTO?,
    val timestamp: String
) {
    companion object {
        fun clear() {
            // Clear any static data if needed
        }
    }
}

@Serializable
data class SelectFlightDataDTO(
    val sessionToken: String,
    val expireAt: String,
    val tripPassengersAdult: Int,
    val tripPassengersChildren: Int,
    val tripPassengersInfant: Int
) {
    companion object {
        fun clear() {
            // Clear any static data if needed
        }
    }
} 