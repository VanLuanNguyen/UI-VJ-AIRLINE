package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val dateOfBirth: String, // Will be formatted as yyyy-MM-dd
    val roleCode: String = "CUSTOMER"
)

@Serializable
data class RegisterApiResponse(
    val status: Int,
    val message: String,
    val data: String? = null,
    val errors: List<ErrorDetail>? = null,
    val errorCode: String? = null,
    val timestamp: String? = null
) 