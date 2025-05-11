package com.vietjoke.vn.model

import java.io.Serializable
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO

data class SelectedDrinkInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val itemsByPassengerIndex: Map<Int, List<DrinkItem>>
) : Serializable

data class DrinkItem(
    val addon: AddonDTO,
    val quantity: Int
) : Serializable

data class DrinkSelectionResult(
    val selectedDrinksForAllLegs: List<SelectedDrinkInfoForLeg>
) : Serializable 