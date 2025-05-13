package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponseDTO(
    @SerialName("status")
    val status: Int,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: PaymentData,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class PaymentData(
    @SerialName("orderId")
    val orderId: String,
    @SerialName("sessionToken")
    val sessionToken: String
) 