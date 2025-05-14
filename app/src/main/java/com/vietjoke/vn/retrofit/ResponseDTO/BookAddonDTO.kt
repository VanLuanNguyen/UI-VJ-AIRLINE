package com.vietjoke.vn.retrofit.ResponseDTO

import kotlinx.serialization.Serializable

@Serializable
data class BookAddonsRequest(
    val authorization: String,
    val sessionToken: String,
    val flightNumber: String,
    val passengerUuid: String,
    val addons: List<AddonBookingItem>
)

@Serializable
data class AddonBookingItem(
    val addonId: Int,
    val quantity: Int
)

@Serializable
data class BookAddonsResponse(
    val status: Int,
    val message: String,
    val data: BookAddonsData,
    val timestamp: String
)

@Serializable
data class BookAddonsData(
    val nextStep: String,
    val currentStep: String,
    val sessionToken: String
)
