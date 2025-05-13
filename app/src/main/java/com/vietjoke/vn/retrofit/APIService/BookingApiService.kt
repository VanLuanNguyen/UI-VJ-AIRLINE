package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.BookingRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingPreviewResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BookingApiService {
    @POST("api/v1/booking/passenger-info")
    suspend fun createBooking(@Body request: BookingRequestDTO): Response<BookingResponseDTO>

    @POST("api/v1/booking/service/complete")
    suspend fun completeBooking(
        @Query("sessionToken") sessionToken: String
    ): Response<BookingResponseDTO>

    @GET("api/v1/booking/preview")
    suspend fun getBookingPreview(
        @Query("sessionToken") sessionToken: String
    ): Response<BookingPreviewResponseDTO>
} 