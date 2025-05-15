package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.UserProfileResponse
import com.vietjoke.vn.retrofit.ResponseDTO.LoginApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.LoginRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.RegisterRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.RegisterApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.VerifyOTPRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.VerifyOTPResponse
import com.vietjoke.vn.retrofit.ResponseDTO.ResendOTPResponse
import com.vietjoke.vn.retrofit.ResponseDTO.UserUpdateRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ForgotPasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDTO): Response<LoginApiResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDTO): Response<RegisterApiResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequestDTO): Response<VerifyOTPResponse>

    @POST("api/v1/auth/resend-otp")
    suspend fun resendOTP(@Query("email") email: String): Response<ResendOTPResponse>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): Response<ForgotPasswordResponse>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String
    ): UserProfileResponse

    @Multipart
    @PUT("api/v1/avatar")
    suspend fun updateAvatar(
        @Header("Authorization") authorization: String,
        @Part avatar: MultipartBody.Part
    ): UserProfileResponse

    @PUT("api/v1/user/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: UserUpdateRequestDTO
    ): Response<UserProfileResponse>
} 