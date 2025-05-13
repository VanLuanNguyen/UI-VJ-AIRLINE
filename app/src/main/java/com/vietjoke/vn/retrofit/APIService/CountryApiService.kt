package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.CountryResponseDTO
import retrofit2.http.GET
import retrofit2.Response

interface CountryApiService {
    @GET("api/v1/countries")
    suspend fun getCountries(): Response<CountryResponseDTO>
} 