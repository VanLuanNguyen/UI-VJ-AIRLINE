package com.vietjoke.vn.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vietjoke.vn.Activities.Dashboard.MyBottomBar
import com.vietjoke.vn.Activities.History.BookingDetailScreen
import com.vietjoke.vn.Activities.History.HistoryScreen
import com.vietjoke.vn.Activities.Login.LoginActivity
import com.vietjoke.vn.Activities.Profile.ProfileScreen
import com.vietjoke.vn.Activities.Profile.EditProfileScreen
import com.vietjoke.vn.Activities.SearchFlight.SearchFlightActivity
import com.vietjoke.vn.R
import com.vietjoke.vn.model.UserModel
import com.vietjoke.vn.utils.LoginPreferences
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SearchFlight : Screen("search_flight")
    object History : Screen("history")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object BookingDetail : Screen("booking_detail/{bookingReference}") {
        fun createRoute(bookingReference: String) = "booking_detail/$bookingReference"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    showBottomBar = when (currentRoute) {
        Screen.Profile.route, Screen.BookingDetail.route, Screen.EditProfile.route -> false
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
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colorResource(id = R.color.white))
                ) {
                    // Home screen content
                }
            }
            
            composable(
                route = Screen.SearchFlight.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Launch SearchFlightActivity
                LaunchedEffect(Unit) {
                    val intent = Intent(context, SearchFlightActivity::class.java)
                    context.startActivity(intent)
                    navController.navigateUp() // Return to previous screen
                }
            }
            
            composable(
                route = Screen.History.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                HistoryScreen(
                    onBookingClick = { bookingReference ->
                        navController.navigate(Screen.BookingDetail.createRoute(bookingReference))
                    }
                )
            }
            
            composable(
                route = Screen.Profile.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
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
                    },
                    onEditClick = { navController.navigate(Screen.EditProfile.route) }
                )
            }
            
            composable(
                route = Screen.EditProfile.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                EditProfileScreen(
                    onBackClick = { navController.navigateUp() },
                    userProfile = UserModel.currentUserProfile ?: return@composable
                )
            }
            
            composable(
                route = Screen.BookingDetail.route,
                arguments = listOf(
                    navArgument("bookingReference") {
                        type = NavType.StringType
                    }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val bookingReference = backStackEntry.arguments?.getString("bookingReference") ?: ""
                BookingDetailScreen(
                    bookingReference = bookingReference,
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
} 