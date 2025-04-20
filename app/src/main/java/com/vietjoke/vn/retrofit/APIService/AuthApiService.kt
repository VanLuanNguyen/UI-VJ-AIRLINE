package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.LoginApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.LoginRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ErrorResponse
import com.vietjoke.vn.retrofit.ResponseDTO.RegisterRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.RegisterApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginApiResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDTO): Response<RegisterApiResponse>
} 