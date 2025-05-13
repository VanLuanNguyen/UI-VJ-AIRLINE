package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class CountryResponseDTO(
    val status: Int,
    val message: String,
    val data: List<CountryDTO>,
    val timestamp: String
) 