package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class AirportListResponseDTO(
    val status: Int,
    val message: String,
    val data: List<AirportDetailDTO>
)

@Serializable
data class AirportDetailDTO(
    val id: Long,
    val createdDate: String,
    val createdBy: String?,
    val modifiedDate: String,
    val modifiedBy: String?,
    val airportCode: String,
    val airportName: String,
    val airportEngName: String,
    val province: ProvinceDetailDTO
)

@Serializable
data class ProvinceDetailDTO(
    val id: Long,
    val createdDate: String,
    val createdBy: String?,
    val modifiedDate: String,
    val modifiedBy: String?,
    val provinceCode: String,
    val provinceName: String,
    val provinceEngName: String,
    val country: CountryDetailDTO
)

@Serializable
data class CountryDetailDTO(
    val id: Long,
    val createdDate: String,
    val createdBy: String?,
    val modifiedDate: String,
    val modifiedBy: String?,
    val countryCode: String,
    val countryName: String,
    val countryEngName: String,
    val areaCode: String
) 