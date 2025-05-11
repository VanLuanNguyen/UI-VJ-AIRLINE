package com.vietjoke.vn.retrofit.ResponseDTO

import com.google.gson.annotations.SerializedName


@kotlinx.serialization.Serializable
data class AddonResponse(
    val status: Int,
    val message: String,
    val data: AddonData,
    val timestamp: String
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class AddonData(
    val sessionToken: String,
    val flight: List<FlightAddonInfo>
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class FlightAddonInfo(
    val flightNumber: String,
    val flightModelCode: String,
    val scheduledDeparture: String,
    val scheduledArrival: String,
    val fareClasses: List<String>,
    val route: Route,
    val addonDTOs: AddonDTOs
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class Route(
    val id: Int,
    val routeCode: String,
    val distance: Int,
    val estimatedDuration: Int,
    val originAirport: Airport,
    val destinationAirport: Airport
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class Airport(
    val id: Int,
    val airportCode: String,
    val airportName: String,
    val airportEngName: String,
    val province: Province
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class Province(
    val id: Int,
    val provinceCode: String,
    val provinceName: String,
    val provinceEngName: String,
    val country: Country
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class Country(
    val id: Int,
    val countryCode: String,
    val countryName: String,
    val countryEngName: String,
    val areaCode: String
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class AddonDTOs(
    val content: List<AddonDTO>,
    val page: PageInfo
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class AddonDTO(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val currency: String,
    val isActive: Boolean,
    val addonTypeCode: String,
    val imgUrl: String? = null,
    val maxQuantity: Int,
    val isFree: Boolean
) : java.io.Serializable
@kotlinx.serialization.Serializable
data class PageInfo(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
) : java.io.Serializable