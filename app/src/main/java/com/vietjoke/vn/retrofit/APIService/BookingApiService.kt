package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.BookingRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingPreviewResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingHistoryResponse
import com.vietjoke.vn.retrofit.ResponseDTO.BookingDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Header

interface BookingApiService {
    @POST("api/v1/booking/passenger-info")
    suspend fun createBooking(
        @Header("Authorization") authorization: String,
        @Body request: BookingRequestDTO
    ): Response<BookingResponseDTO>

    @POST("api/v1/booking/service/complete")
    suspend fun completeBooking(
        @Header("Authorization") authorization: String,
        @Query("sessionToken") sessionToken: String
    ): Response<BookingResponseDTO>

    @GET("api/v1/booking/preview")
    suspend fun getBookingPreview(
        @Header("Authorization") authorization: String,
        @Query("sessionToken") sessionToken: String
    ): Response<BookingPreviewResponseDTO>

    @GET("api/v1/booking/history")
    suspend fun getBookingHistory(
        @Header("Authorization") authorization: String
    ): Response<BookingHistoryResponse>

    @GET("api/v1/booking/detail")
    suspend fun getBookingDetail(
        @Header("Authorization") authorization: String,
        @Query("bookingReference") bookingReference: String
    ): Response<BookingDetailResponse>

    @POST("api/v1/booking/cancel")
    suspend fun cancelBooking(
        @Header("Authorization") authorization: String,
        @Query("bookingReference") bookingReference: String
    ): Response<BookingResponseDTO>
} 