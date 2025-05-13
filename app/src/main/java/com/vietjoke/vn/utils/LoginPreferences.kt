package com.vietjoke.vn.utils

import android.content.Context
import android.content.SharedPreferences

object LoginPreferences {
    private const val PREF_NAME = "login_preferences"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER_ME = "remember_me"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLoginInfo(context: Context, username: String, password: String, rememberMe: Boolean) {
        val prefs = getPreferences(context)
        prefs.edit().apply {
            if (rememberMe) {
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD, password)
            } else {
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD)
            }
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun getSavedUsername(context: Context): String? {
        return getPreferences(context).getString(KEY_USERNAME, null)
    }

    fun getSavedPassword(context: Context): String? {
        return getPreferences(context).getString(KEY_PASSWORD, null)
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearLoginInfo(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
} 