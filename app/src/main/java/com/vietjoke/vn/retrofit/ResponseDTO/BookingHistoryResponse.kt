package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class BookingHistoryResponse(
    val status: Int,
    val message: String,
    val data: List<BookingHistoryItem>,
    val timestamp: String
)

@Serializable
data class BookingHistoryItem(
    val bookingReference: String,
    val statusCode: String,
    val statusName: String,
    val totalAmount: Double,
    val discountAmount: Double,
    val currency: String,
    val bookingDate: String,
    val createdDate: String,
    val modifiedDate: String,
    val tripType: String,
    val adultCount: Int,
    val childCount: Int,
    val infantCount: Int,
    val flights: List<FlightInfo>
)

@Serializable
data class FlightInfo(
    val flightNumber: String,
    val routeCode: String,
    val departureTime: String
) 