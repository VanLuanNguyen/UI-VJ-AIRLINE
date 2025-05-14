package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class BookingDetailResponse(
    val status: Int,
    val message: String,
    val data: BookingDetail,
    val timestamp: String
)

@Serializable
data class BookingDetail(
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
    val promoCode: String?,
    val promoDescription: String?,
    val promoDiscountAmount: Double?,
    val firstName: String?,
    val lastName: String?,
    val userEmail: String,
    val userPhone: String,
    val passengers: List<PassengerDetails>,
    val payments: List<PaymentDetail>
)

@Serializable
data class PassengerDetails(
    val firstName: String,
    val lastName: String,
    val idType: String?,
    val passengerType: String,
    val idNumber: String?,
    val flights: List<FlightDetail>
)

@Serializable
data class FlightDetail(
    val flightNumber: String,
    val flightRouteCode: String,
    val flightDepartureAirport: String,
    val flightArrivalAirport: String,
    val flightDepartureTime: String,
    val flightArrivalTime: String,
    val flightGate: String,
    val flightTerminal: String,
    val airlineName: String,
    val aircraftRegistrationNumber: String,
    val aircraftModelCode: String,
    val seatNumber: String?,
    val totalAmount: Double,
    val fareName: String,
    val addons: List<AddonDetails>
)

@Serializable
data class AddonDetails(
    val addonId: Int,
    val addonName: String,
    val addonTypeCode: String,
    val quantity: Int,
    val price: Double
)

@Serializable
data class PaymentDetail(
    val paymentMethod: String,
    val transactionId: String,
    val paymentAmount: Double,
    val paymentDate: String,
    val paymentStatus: String
) 