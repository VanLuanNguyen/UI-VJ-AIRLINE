package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class CountryDTO(
    val id: Int,
    val createdDate: String,
    val createdBy: String?,
    val modifiedDate: String,
    val modifiedBy: String?,
    val countryCode: String,
    val countryName: String,
    val countryEngName: String,
    val areaCode: String?
) 