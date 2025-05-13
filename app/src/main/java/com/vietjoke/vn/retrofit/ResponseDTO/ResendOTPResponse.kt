package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class ResendOTPResponse(
    val status: Int,
    val message: String,
    val data: ResendOTPData,
    val timestamp: String
)

@Serializable
data class ResendOTPData(
    val status: String,
    val message: String
) 