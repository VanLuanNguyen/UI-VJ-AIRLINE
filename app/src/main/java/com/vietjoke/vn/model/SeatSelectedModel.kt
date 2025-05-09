package com.vietjoke.vn.model

import java.io.Serializable

data class SelectedSeatInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val seatsByPassengerIndex: Map<Int, String> // Key: PassengerIndex, Value: SeatNumber
) : Serializable

data class SeatSelectionResult(
    val selectedSeatsForAllLegs: List<SelectedSeatInfoForLeg>
) : Serializable