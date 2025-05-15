package com.vietjoke.vn.model

import com.vietjoke.vn.retrofit.ResponseDTO.UserProfileData

object UserModel {
    private var _token: String? = null
    private var _currentUserProfile: UserProfileData? = null
    
    var token: String?
        get() = _token
        set(value) {
            _token = value
        }
    
    var currentUserProfile: UserProfileData?
        get() = _currentUserProfile
        set(value) {
            _currentUserProfile = value
        }
    
    fun clearToken() {
        _token = null
        _currentUserProfile = null
    }

    fun clear() {
        clearToken()
    }

    fun updateProfile(profile: UserProfileData) {
        _currentUserProfile = profile
    }
} 