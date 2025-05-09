package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable // For passing via Intent

// Request Body
@Serializable
data class GetSeatsRequest(
    val sessionToken: String
) : JavaSerializable // Make request Serializable if needed, though usually not passed

// --- Response DTOs ---

// Reusing existing ApiResponse if the structure matches
// Assuming ApiResponse<T> exists as:
// @Serializable data class ApiResponse<T>(val status: Int, val message: String, val data: T?, val timestamp: String?) : JavaSerializable

// Data part of the response
@Serializable
data class SeatSelectionData(
    val sessionToken: String, // Updated session token
    val flight: List<FlightSeatInfo> // List of flight legs (1 for one-way, 2 for round-trip)
) : JavaSerializable

// Information for a single flight leg's seat map
@Serializable
data class FlightSeatInfo(
    val flightNumber: String,
    val flightModelCode: String,
    val scheduledDeparture: String,
    val scheduledArrival: String,
    // Assuming RouteDTO from FlightResponseDTO can be reused if structure is identical
    val route: RouteDTO?, // Make nullable if it might be missing
    val flightSeats: List<FlightSeatDTO> // List of seats for this flight
    // fareClasses is empty in the example, so omitted unless needed
) : JavaSerializable

// Information for a single seat
@Serializable
data class FlightSeatDTO(
    val id: Int,
    val seatNumber: String, // e.g., "BUS-1", "1A"
    val flightNumber: String,
    val fareCode: String, // e.g., "BUS", "ECO"
    val seatStatus: String // e.g., "AVAILABLE", "OCCUPIED", "BLOCKED"
) : JavaSerializable

// --- Reuse existing DTOs if structure matches ---
// Make sure RouteDTO, AirportDTO, ProvinceDTO, CountryDTO are Serializable
// Example (Add : JavaSerializable if not already present):
/*
@Serializable
data class RouteDTO(...) : JavaSerializable
@Serializable
data class AirportDTO(...) : JavaSerializable
// ... and so on for nested DTOs if needed for passing via Intent
*/