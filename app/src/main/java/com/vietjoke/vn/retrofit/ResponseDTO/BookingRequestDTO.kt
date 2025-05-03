package com.vietjoke.vn.retrofit.ResponseDTO

import com.vietjoke.vn.model.PassengerModel
import kotlinx.serialization.Serializable

@Serializable
data class BookingRequestDTO(
    val passengersAdult: List<PassengerModel>,
    val passengersChild: List<PassengerModel>,
    val passengersInfant: List<PassengerModel>,
    val sessionToken: String
)