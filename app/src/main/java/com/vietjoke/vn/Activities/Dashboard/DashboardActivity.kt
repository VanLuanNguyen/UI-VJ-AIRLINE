package com.vietjoke.vn.Activities.Dashboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.vietjoke.vn.Activities.Splash.StatusTopBarColor
import com.vietjoke.vn.navigation.AppNavigation

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StatusTopBarColor()
            AppNavigation()
        }
    }
}