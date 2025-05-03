package com.vietjoke.vn.model

object PassengerCountModel {
    var adultCount: Int = 0
    var childCount: Int = 0
    var infantCount: Int = 0

    fun clear() {
        adultCount = 0
        childCount = 0
        infantCount = 0
    }

    fun setCounts(adult: Int, child: Int, infant: Int) {
        adultCount = adult
        childCount = child
        infantCount = infant
    }
} 