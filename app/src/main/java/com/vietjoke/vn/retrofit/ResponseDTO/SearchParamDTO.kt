package com.vietjoke.vn.retrofit.ResponseDTO


import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class SearchParamDTO(
    val tripType: String,
    val tripFrom: String,
    val tripTo: String,
    val tripStartDate: String, // ISO-8601 format: yyyy-MM-dd
    val tripReturnDate: String? = null,
    val tripPassengers: Int,
    val tripPassengersAdult: Int,
    val tripPassengersChild: Int,
    val tripPassengersInfant: Int,
    val coupon: String? = null,
    val is_find_cheapest: Boolean = false
)
