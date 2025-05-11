package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.*
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface AddonApiService {

    @GET("api/v1/booking/addons")
    suspend fun getAddons(
        @Query("addonCode") addonCode: String,
        @Query("sortBy") sortBy: String = "name",
        @Query("sortOrder") sortOrder: String = "asc",
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("status") status: String = "ACTIVE",
        @Query("sessionToken") sessionToken: String,
        @Query("flightNumber") flightNumber: String
    ): Response<AddonResponse>

    @POST("api/v1/booking/addons")
    suspend fun bookAddons(
        @Body request: BookAddonsRequest
    ): Response<BookAddonsResponse>
}

@Serializable
data class BookAddonsRequest(
    val sessionToken: String,
    val flightNumber: String,
    val passengerUuid: String,
    val addons: List<AddonBookingItem>
)

@Serializable
data class AddonBookingItem(
    val addonId: Int,
    val quantity: Int
)

@Serializable
data class BookAddonsResponse(
    val status: Int,
    val message: String,
    val data: BookAddonsData,
    val timestamp: String
)

@Serializable
data class BookAddonsData(
    val nextStep: String,
    val currentStep: String,
    val sessionToken: String
) 