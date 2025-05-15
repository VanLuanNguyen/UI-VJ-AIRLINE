package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOTPRequestDTO(
    val email: String,
    val otp: String,
    val otpType: String? = null
) 