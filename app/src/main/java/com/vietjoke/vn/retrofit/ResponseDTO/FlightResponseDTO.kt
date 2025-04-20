package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable
@Serializable
data class FlightResponseDTO(
    val flightNumber: String,
    val flightModelCode: String,
    val scheduledDeparture: String,
    val scheduledArrival: String,
    val fareClasses: List<FareClassDTO>,
    val route: RouteDTO
)

@Serializable
data class FareClassDTO(
    val fareClassCode: String,
    val fareClassName: String,
    val availableSeats: Int,
    val basePrice: Double,
    val notEnoughSeats: Boolean
)

@Serializable
data class RouteDTO(
    val originAirport: AirportDTO,
    val destinationAirport: AirportDTO
)

@Serializable
data class AirportDTO(
    val airportCode: String,
    val airportName: String
)

@Serializable
data class SearchFlightResponseDTO(
    val travelOptions: List<Map<String, List<FlightResponseDTO>>>,
    val sessionToken: String,
    val expireAt: String
)

@Serializable
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
    val timestamp: String? = null
)
