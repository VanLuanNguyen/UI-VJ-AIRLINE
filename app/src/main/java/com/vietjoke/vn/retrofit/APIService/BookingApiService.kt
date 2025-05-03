package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.BookingRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.BookingResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingApiService {
    @POST("api/v1/booking/passenger-info")
    suspend fun createBooking(@Body request: BookingRequestDTO): Response<BookingResponseDTO>
} 