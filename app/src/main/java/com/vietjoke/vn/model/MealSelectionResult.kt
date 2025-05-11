package com.vietjoke.vn.model

import java.io.Serializable
import com.vietjoke.vn.retrofit.ResponseDTO.AddonDTO

data class SelectedMealInfoForLeg(
    val flightNumber: String?,
    val originCode: String?,
    val destinationCode: String?,
    val itemsByPassengerIndex: Map<Int, List<MealItem>>
) : Serializable

data class MealItem(
    val addon: AddonDTO,
    val quantity: Int
) : Serializable

data class MealSelectionResult(
    val selectedMealsForAllLegs: List<SelectedMealInfoForLeg>
) : Serializable 