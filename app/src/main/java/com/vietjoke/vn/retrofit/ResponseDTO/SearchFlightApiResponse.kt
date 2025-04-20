package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class SearchFlightApiResponse(
    val status: Int,
    val message: String? = null,         // ✅ nullable hoặc có default
    val data: SearchFlightResponseDTO? = null,
    val timestamp: String? = null
)

