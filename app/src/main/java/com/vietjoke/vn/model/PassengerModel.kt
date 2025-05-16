package com.vietjoke.vn.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PassengerAdultModel(
    var uuid: String = UUID.randomUUID().toString(),
    var firstName: String = "",
    var lastName: String = "",
    var dateOfBirth: String = "",
    var gender: String = "MALE",
    var passengerType: String = "ADULT",
    var countryCode: String = "",
    var idType: String = "",
    var idNumber: String = "",
    var phone: String = "",
    var email: String = ""
)
@Serializable
data class PassengerChildModel(
    var uuid: String = UUID.randomUUID().toString(),
    var firstName: String = "",
    var lastName: String = "",
    var dateOfBirth: String = "",
    var gender: String = "MALE",
    var passengerType: String = "CHILD"
)
@Serializable
data class PassengerInfantModel(
    var uuid: String = UUID.randomUUID().toString(),
    var firstName: String = "",
    var lastName: String = "",
    var dateOfBirth: String = "",
    var gender: String = "MALE",
    var passengerType: String = "INFANT",
    var accompanyingAdultFirstName: String = "",
    var accompanyingAdultLastName: String = ""
) 