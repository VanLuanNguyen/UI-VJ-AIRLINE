package com.vietjoke.vn.retrofit.ResponseDTO

import com.google.gson.annotations.SerializedName

data class UserUpdateRequestDTO(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("dateOfBirth")
    val dateOfBirth: String,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("previousPassword")
    val previousPassword: String? = null,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("confirmPassword")
    val confirmPassword: String? = null
) 