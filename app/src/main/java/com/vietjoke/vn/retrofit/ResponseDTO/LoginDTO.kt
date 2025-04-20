package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    val identifier: String,
    val password: String
)

@Serializable
data class LoginResponseDTO(
    val token: String
)

@Serializable
data class ErrorDetail(
    val field: String,
    val message: String,
    val type: String
)

@Serializable
data class LoginApiResponse(
    val status: Int,
    val message: String,
    val data: LoginResponseDTO? = null,
    val errors: List<ErrorDetail>? = null,
    val errorCode: String? = null,
    val timestamp: String? = null
)

@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<ErrorDetail>? = null,
    val errorCode: String? = null,
    val timestamp: String? = null
) 