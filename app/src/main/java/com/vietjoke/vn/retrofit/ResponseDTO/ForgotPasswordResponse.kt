package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponse(
    val status: Int,
    val message: String,
    val data: ForgotPasswordData? = null
)

@Serializable
data class ForgotPasswordData(
    val message: String
) 