package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCaptureData(
    val orderId: String,
    val status: String
)

@Serializable
data class PaymentCaptureResponse(
    val status: Int,
    val message: String,
    val data: PaymentCaptureData,
    val timestamp: String
) 