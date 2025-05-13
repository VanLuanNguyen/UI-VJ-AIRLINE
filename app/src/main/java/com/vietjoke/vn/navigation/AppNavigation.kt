package com.vietjoke.vn.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vietjoke.vn.Activities.Dashboard.MyBottomBar
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.Activities.Profile.ProfileScreen
import com.vietjoke.vn.Activities.SearchFlight.SearchFlightActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.utils.LoginPreferences

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SearchFlight : Screen("search_flight")
    object History : Screen("history")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    showBottomBar = when (currentRoute) {
        Screen.Profile.route -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MyBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.white))
                ) {
                    // Home screen content
                }
            }
            composable(Screen.SearchFlight.route) {
                // Launch SearchFlightActivity
                LaunchedEffect(Unit) {
                    val intent = Intent(context, SearchFlightActivity::class.java)
                    context.startActivity(intent)
                    navController.navigateUp() // Return to previous screen
                }
            }
            composable(Screen.History.route) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.white))
                ) {
                    // History screen content
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.navigateUp() },
                    onLogoutClick = {
                        // Clear user data
                        UserModel.token = null
                        
                        // Clear saved login preferences
                        LoginPreferences.clearLoginInfo(context)
                        
                        // Navigate to login screen with flags to clear back stack
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
} 