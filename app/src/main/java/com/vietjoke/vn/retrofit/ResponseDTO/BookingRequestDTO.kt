package com.vietjoke.vn.retrofit.ResponseDTO

import com.vietjoke.vn.model.PassengerAdultModel
import com.vietjoke.vn.model.PassengerChildModel
import com.vietjoke.vn.model.PassengerInfantModel
import kotlinx.serialization.Serializable

@Serializable
data class BookingRequestDTO(
    val passengersAdult: List<PassengerAdultModel>,
    val passengersChild: List<PassengerChildModel>,
    val passengersInfant: List<PassengerInfantModel>,
    val sessionToken: String
)