package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.ApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.ReleaseSeatResponseData
import com.vietjoke.vn.retrofit.ResponseDTO.ReserveSeatRequest
import com.vietjoke.vn.retrofit.ResponseDTO.ReserveSeatResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface SeatApiService {
    @POST("api/v1/booking/reserve-seat")
    suspend fun reserveSeat(
        @Body request: ReserveSeatRequest
    ): Response<ApiResponse<ReserveSeatResponseData>>
    @HTTP(method = "DELETE", path = "api/v1/booking/release-seat", hasBody = true)
    suspend fun releaseSeat(@Body request: ReserveSeatRequest): Response<ApiResponse<ReleaseSeatResponseData>>
}