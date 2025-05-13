package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingPreviewResponseDTO(
    @SerialName("status")
    val status: Int,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: BookingPreviewData,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class BookingPreviewData(
    @SerialName("sessionToken")
    val sessionToken: String,
    @SerialName("flights")
    val flights: List<FlightPreview>,
    @SerialName("passengerDetails")
    val passengerDetails: List<PassengerDetail>,
    @SerialName("coupon")
    val coupon: CouponInfo,
    @SerialName("totalBookingPrice")
    val totalBookingPrice: Double
)

@Serializable
data class FlightPreview(
    @SerialName("flightNumber")
    val flightNumber: String,
    @SerialName("totalTicketPrice")
    val totalTicketPrice: Double,
    @SerialName("totalAddonPrice")
    val totalAddonPrice: Double
)

@Serializable
data class PassengerDetail(
    @SerialName("passengerUuid")
    val passengerUuid: String,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("passengerType")
    val passengerType: String,
    @SerialName("passengerFlightDetailDTOS")
    val passengerFlightDetailDTOS: List<PassengerFlightDetail>,
    @SerialName("accompanyingAdultFirstName")
    val accompanyingAdultFirstName: String?,
    @SerialName("accompanyingAdultLastName")
    val accompanyingAdultLastName: String?
)

@Serializable
data class PassengerFlightDetail(
    @SerialName("flightNumber")
    val flightNumber: String,
    @SerialName("fareClass")
    val fareClass: String,
    @SerialName("routeCode")
    val routeCode: String,
    @SerialName("ticketPrice")
    val ticketPrice: Double,
    @SerialName("addonPrice")
    val addonPrice: Double,
    @SerialName("addons")
    val addons: List<AddonDetail>
)

@Serializable
data class AddonDetail(
    @SerialName("addonId")
    val addonId: Int,
    @SerialName("addonName")
    val addonName: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("price")
    val price: Double
)

@Serializable
data class CouponInfo(
    @SerialName("code")
    val code: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("amount")
    val amount: Double?,
    @SerialName("isPercentage")
    val isPercentage: Boolean?,
    @SerialName("isAvailable")
    val isAvailable: Boolean
) 