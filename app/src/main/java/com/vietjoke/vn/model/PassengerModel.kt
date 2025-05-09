package com.vietjoke.vn.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PassengerModel(
    var uuid: String = UUID.randomUUID().toString(),
    var firstName: String = "",
    var lastName: String = "",
    var dateOfBirth: String = "",
    var gender: String = "MALE",
    var passengerType: String = "",
    var countryCode: String = "",
    var idType: String = "",
    var idNumber: String = "",
    var phone: String = "",
    var email: String = "",
    var accompanyingAdultFirstName: String = "",
    var accompanyingAdultLastName: String = "",
) 