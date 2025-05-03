package com.vietjoke.vn.model

// Thêm các import cần thiết cho Compose State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.getValue // Cần cho delegate 'by'
import androidx.compose.runtime.setValue // Cần cho delegate 'by'
import androidx.compose.runtime.mutableStateOf // Cho sessionToken

object FlightBookingModel {
    var flightNumber: String? = null
    var fareCode: String? = null
    // Nên dùng delegate 'by mutableStateOf' nếu bạn muốn UI khác cũng phản ứng với thay đổi token
    var sessionToken: String? by mutableStateOf(null)

    // --- THAY ĐỔI QUAN TRỌNG ---
    // Sử dụng SnapshotStateList và mutableStateListOf()
    // Chuyển từ 'var' thành 'val' vì bản thân đối tượng list không thay đổi, chỉ nội dung thay đổi
    val passengersAdult: SnapshotStateList<PassengerModel> = mutableStateListOf()
    val passengersChild: SnapshotStateList<PassengerModel> = mutableStateListOf()
    val passengersInfant: SnapshotStateList<PassengerModel> = mutableStateListOf()
    // --- KẾT THÚC THAY ĐỔI ---

    fun clear() {
        flightNumber = null
        fareCode = null
        sessionToken = null // Gán lại giá trị null cho state
        passengersAdult.clear() // clear() hoạt động với SnapshotStateList
        passengersChild.clear()
        passengersInfant.clear()
        // Giả sử PassengerCountModel cũng tồn tại và có hàm clear
        // PassengerCountModel.clear()
    }

    // Hàm initializePassengers không cần thay đổi nhiều vì add() hoạt động với SnapshotStateList
    fun initializePassengers() {
        // Giả sử PassengerCountModel tồn tại
        val adultCount = PassengerCountModel.adultCount
        val childCount = PassengerCountModel.childCount
        val infantCount = PassengerCountModel.infantCount

        passengersAdult.clear()
        passengersChild.clear()
        passengersInfant.clear()

        repeat(adultCount) {
            passengersAdult.add(PassengerModel(passengerType = "ADULT"))
        }
        repeat(childCount) {
            passengersChild.add(PassengerModel(passengerType = "CHILD"))
        }
        repeat(infantCount) {
            passengersInfant.add(PassengerModel(passengerType = "INFANT"))
        }
    }
}