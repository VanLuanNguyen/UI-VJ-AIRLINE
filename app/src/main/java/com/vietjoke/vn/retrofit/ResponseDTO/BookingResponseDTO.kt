package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class BookingResponseDTO(
    val status: Int,
    val message: String,
    val data: BookingDataDTO? = null,
    val timestamp: String
)

@Serializable
data class BookingDataDTO(
    val sessionToken: String
) 