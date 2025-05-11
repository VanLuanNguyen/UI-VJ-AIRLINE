package com.vietjoke.vn.model

import java.io.Serializable
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO

// Represents selected other service for a flight leg
// Key: PassengerIndex, Value: List of AddonDTO (for multiple services, if allowed)
data class SelectedOtherServiceInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val serviceByPassengerIndex: Map<Int, List<AddonDTO>>
) : Serializable

data class OtherServiceSelectionResult(
    val selectedServiceForAllLegs: List<SelectedOtherServiceInfoForLeg>
) : Serializable 