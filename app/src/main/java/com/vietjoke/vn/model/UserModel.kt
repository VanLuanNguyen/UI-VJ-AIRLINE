package com.vietjoke.vn.model

object UserModel {
    private var _token: String? = null
    
    var token: String?
        get() = _token
        set(value) {
            _token = value
        }
    
    fun clearToken() {
        _token = null
    }
} 