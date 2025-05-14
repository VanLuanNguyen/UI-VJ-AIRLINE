package com.vietjoke.vn.retrofit.APIService

import com.vietjoke.vn.retrofit.ResponseDTO.AirportListResponseDTO
import com.vietjoke.vn.retrofit.ResponseDTO.ApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.GetSeatsRequest
import com.vietjoke.vn.retrofit.ResponseDTO.SearchFlightApiResponse
import com.vietjoke.vn.retrofit.ResponseDTO.SearchParamDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SeatSelectionData
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightRequestDTO
import com.vietjoke.vn.retrofit.ResponseDTO.SelectFlightResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FlightApiService {
    @POST("api/v1/flight/search")
    suspend fun searchFlights(@Body param: SearchParamDTO): SearchFlightApiResponse

    @GET("api/v1/airports")
    suspend fun getAirports(): AirportListResponseDTO

    @POST("api/v1/flight/select")
    suspend fun selectFlight(@Body request: SelectFlightRequestDTO): SelectFlightResponseDTO

    @GET("api/v1/booking/get-seats") // !!! Đảm bảo là @GET !!!
    suspend fun getSeats(
        @Header("Authorization") authorization: String,
        @Query("sessionToken") token: String // !!! Dùng @Query với tên trùng @RequestParam !!!
    ): Response<ApiResponse<SeatSelectionData>>
}