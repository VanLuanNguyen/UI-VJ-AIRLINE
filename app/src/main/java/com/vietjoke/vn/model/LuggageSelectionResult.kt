package com.vietjoke.vn.model

import java.io.Serializable

// Represents selected luggage for a flight leg
// Key: PassengerIndex, Value: List of AddonDTO (for multiple luggage packages)
data class SelectedLuggageInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val luggageByPassengerIndex: Map<Int, List<com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO>>
) : Serializable

data class LuggageSelectionResult(
    val selectedLuggageForAllLegs: List<SelectedLuggageInfoForLeg>
) : Serializable 