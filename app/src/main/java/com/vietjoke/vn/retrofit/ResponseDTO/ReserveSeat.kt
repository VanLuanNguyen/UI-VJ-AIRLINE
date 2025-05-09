package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class ReserveSeatRequest(
    val flightNumber: String,
    val seatNumber: String,
    val passengerUUID: String, // Đổi tên thành camelCase cho nhất quán Kotlin
    val sessionToken: String
) : JavaSerializable // Serializable nếu cần

@Serializable
data class ReserveSeatResponseData(
    val nextStep: String?, // Có thể null
    val sessionToken: String,
    val currentStep: String? // Có thể null
) : JavaSerializable

