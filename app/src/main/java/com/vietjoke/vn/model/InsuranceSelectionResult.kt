package com.vietjoke.vn.model

import java.io.Serializable
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO

// Represents selected insurance for a flight leg
// Key: PassengerIndex, Value: List of AddonDTO (for multiple insurance packages, if allowed)
data class SelectedInsuranceInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val insuranceByPassengerIndex: Map<Int, List<AddonDTO>>
) : Serializable

data class InsuranceSelectionResult(
    val selectedInsuranceForAllLegs: List<SelectedInsuranceInfoForLeg>
) : Serializable 