package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOTPResponse(
    val status: Int,
    val message: String,
    val data: String,
    val timestamp: String
) 