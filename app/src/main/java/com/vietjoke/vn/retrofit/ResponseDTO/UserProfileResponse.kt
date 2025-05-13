package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable
@Serializable
data class UserProfileResponse(
    val status: Int,
    val message: String,
    val data: UserProfileData,
    val timestamp: String
)
@Serializable
data class UserProfileData(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val phone: String,
    val address: String?,
    val avatarUrl: String?,
    val emailVerified: Boolean,
    val isActive: Boolean,
    val roleCode: String
) 