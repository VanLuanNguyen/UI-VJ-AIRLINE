package com.vietjoke.vn.Activities.Dashboard

import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vietjoke.vn.R
import com.vietjoke.vn.navigation.Screen

data class BottomMenuItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun prepareBottomMenuItems(): List<BottomMenuItem> {
    return listOf(
        BottomMenuItem(
            label = "Trang chủ",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Screen.Home.route
        ),
        BottomMenuItem(
            label = "Đặt vé",
            selectedIcon = Icons.Filled.Flight,
            unselectedIcon = Icons.Outlined.Flight,
            route = Screen.SearchFlight.route
        ),
        BottomMenuItem(
            label = "Lịch sử",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            route = Screen.History.route
        ),
        BottomMenuItem(
            label = "Tài khoản",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = Screen.Profile.route
        )
    )
}

@Composable
fun MyBottomBar(navController: NavController) {
    val bottomMenuItemsList = prepareBottomMenuItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected = currentRoute == bottomMenuItem.route,
                onClick = { 
                    navController.navigate(bottomMenuItem.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { 
                    Icon(
                        imageVector = if (currentRoute == bottomMenuItem.route) 
                            bottomMenuItem.selectedIcon 
                        else 
                            bottomMenuItem.unselectedIcon,
                        contentDescription = bottomMenuItem.label,
                        tint = if (currentRoute == bottomMenuItem.route) 
                            colorResource(id = R.color.purple) 
                        else 
                            Color.Gray
                    )
                },
                label = { 
                    Text(
                        text = bottomMenuItem.label,
                        color = if (currentRoute == bottomMenuItem.route) 
                            colorResource(id = R.color.purple) 
                        else 
                            Color.Gray
                    )
                },
                selectedContentColor = colorResource(id = R.color.purple),
                unselectedContentColor = Color.Gray
            )
        }
    }
}