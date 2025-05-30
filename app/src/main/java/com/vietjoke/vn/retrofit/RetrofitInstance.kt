package com.vietjoke.vn.retrofit

import com.vietjoke.vn.retrofit.APIService.AddonApiService
import com.vietjoke.vn.retrofit.APIService.AuthApiService
import com.vietjoke.vn.retrofit.APIService.BookingApiService
import com.vietjoke.vn.retrofit.APIService.CountryApiService
import com.vietjoke.vn.retrofit.APIService.FlightApiService
import com.vietjoke.vn.retrofit.APIService.PaymentApiService
import com.vietjoke.vn.retrofit.APIService.SeatApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:8080/"
    private const val BASE_URL_DEVICE = "http://192.168.1.8:8080/" // Thay đổi IP này thành IP máy tính của bạn
    private const val TIMEOUT_SECONDS = 30L

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EMULATOR) // Sử dụng BASE_URL_DEVICE cho thiết bị thật
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val flightApi: FlightApiService by lazy {
        retrofit.create(FlightApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val bookingApi: BookingApiService by lazy {
        retrofit.create(BookingApiService::class.java)
    }

    val seatApi: SeatApiService by lazy {
        retrofit.create(SeatApiService::class.java)
    }

    val addonApi: AddonApiService by lazy {
        retrofit.create(AddonApiService::class.java)
    }

    val paymentApi: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }

    val countryApi: CountryApiService by lazy {
        retrofit.create(CountryApiService::class.java)
    }
}
