package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.PaymentCaptureResponse
import com.vietjoke.vn.retrofit.ResponseDTO.PaymentResponseDTO
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface PaymentApiService {
    @POST("api/v1/payment/create-order")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Query("sessionToken") sessionToken: String
    ): Response<PaymentResponseDTO>
    @POST("api/v1/payment/capture-order")
    suspend fun captureOrder(
        @Header("Authorization") authorization: String,
        @Query("sessionToken") sessionToken: String,
        @Query("orderId") orderId: String
    ): Response<PaymentCaptureResponse>
} 