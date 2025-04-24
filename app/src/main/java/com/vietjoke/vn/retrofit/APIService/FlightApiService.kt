package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.AirportListResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SearchFlightApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.SearchParamDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FlightApiService {
    @POST("api/v1/flight/search")
    suspend fun searchFlights(@Body param: SearchParamDTO): SearchFlightApiResponse

    @GET("api/v1/airports")
    suspend fun getAirports(): AirportListResponseDTO

    @POST("api/v1/flight/select")
    suspend fun selectFlight(@Body request: SelectFlightRequestDTO): SelectFlightResponseDTO
}