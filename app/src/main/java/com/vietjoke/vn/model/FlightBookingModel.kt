package com.vietjoke.vn.model

// Thêm các import cần thiết cho Compose State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.getValue // Cần cho delegate 'by'
import androidx.compose.runtime.setValue // Cần cho delegate 'by'
import androidx.compose.runtime.mutableStateOf // Cho sessionToken

object FlightBookingModel {
    var flightNumber: String? by mutableStateOf(null)
    var fareCode: String? by mutableStateOf(null)
    // Thêm các chi tiết khác nếu cần (vd: departureTime, originAirport...)

    // --- THÊM TRƯỜNG CHO CHUYẾN BAY VỀ ---
    var returnFlightNumber: String? by mutableStateOf(null)
    var returnFareCode: String? by mutableStateOf(null)
    // Thêm các chi tiết khác nếu cần (vd: returnDepartureTime, returnOriginAirport...)

    // --- THÊM CỜ TRẠNG THÁI KHỨ HỒI ---
    var isRoundTrip: Boolean by mutableStateOf(false)

    // Token phiên làm việc
    var sessionToken: String? by mutableStateOf(null)
    var orderId: String? = null

    var seatAssignmentsPerFlight: Map<String, Map<Int, String>> by mutableStateOf(emptyMap())

    fun clearSeats() {
        seatAssignmentsPerFlight = emptyMap()
    }

    // Danh sách hành khách với các model mới
    val passengersAdult: SnapshotStateList<PassengerAdultModel> = mutableStateListOf()
    val passengersChild: SnapshotStateList<PassengerChildModel> = mutableStateListOf()
    val passengersInfant: SnapshotStateList<PassengerInfantModel> = mutableStateListOf()

    // --- CẬP NHẬT HÀM CLEAR ---
    fun clear() {
        flightNumber = null
        fareCode = null
        returnFlightNumber = null // Reset trường chuyến về
        returnFareCode = null     // Reset trường chuyến về
        isRoundTrip = false       // Reset cờ khứ hồi
        sessionToken = null
        passengersAdult.clear()
        passengersChild.clear()
        passengersInfant.clear()
        // Reset các model hoặc state khác nếu có
        // PassengerCountModel.clear()
    }

    // Add new function to clear session data
    fun clearSessionData() {
        sessionToken = null
        orderId = null
        clearSeats()
    }

    // Hàm initializePassengers với các model mới
    fun initializePassengers() {
        // Giả sử PassengerCountModel tồn tại
        val adultCount = PassengerCountModel.adultCount
        val childCount = PassengerCountModel.childCount
        val infantCount = PassengerCountModel.infantCount

        passengersAdult.clear()
        passengersChild.clear()
        passengersInfant.clear()

        repeat(adultCount) {
            passengersAdult.add(PassengerAdultModel())
        }
        repeat(childCount) {
            passengersChild.add(PassengerChildModel())
        }
        repeat(infantCount) {
            passengersInfant.add(PassengerInfantModel())
        }
    }
}