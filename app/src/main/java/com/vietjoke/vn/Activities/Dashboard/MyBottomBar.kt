package com.vietjoke.vn.Activities.Dashboard

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vietjoke.vn.Activities.Profile.ProfileActivity
import com.vietjoke.vn.Activities.SearchFlight.SearchFlightActivity
import com.vietjoke.vn.R

data class BottomMenuItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val action: (Context) -> Unit
)

@Composable
fun prepareBottomMenuItems(): List<BottomMenuItem> {
    val context = LocalContext.current
    return listOf(
        BottomMenuItem(
            label = "Trang chủ",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            action = { /* Stay on current screen */ }
        ),
        BottomMenuItem(
            label = "Đặt vé",
            selectedIcon = Icons.Filled.Flight,
            unselectedIcon = Icons.Outlined.Flight,
            action = { context ->
                context.startActivity(Intent(context, SearchFlightActivity::class.java))
            }
        ),
        BottomMenuItem(
            label = "Lịch sử",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            action = { /* TODO: Navigate to history screen */ }
        ),
        BottomMenuItem(
            label = "Tài khoản",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            action = { context ->
                context.startActivity(Intent(context, ProfileActivity::class.java))
            }
        )
    )
}

@Composable
@Preview
fun MyBottomBar() {
    val bottomMenuItemsList = prepareBottomMenuItems()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Trang chủ") }

    BottomAppBar(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected = selectedItem == bottomMenuItem.label,
                onClick = { 
                    selectedItem = bottomMenuItem.label
                    bottomMenuItem.action(context)
                },
                icon = { 
                    Icon(
                        imageVector = if (selectedItem == bottomMenuItem.label) 
                            bottomMenuItem.selectedIcon 
                        else 
                            bottomMenuItem.unselectedIcon,
                        contentDescription = bottomMenuItem.label,
                        tint = if (selectedItem == bottomMenuItem.label) 
                            colorResource(id = R.color.purple) 
                        else 
                            Color.Gray
                    )
                },
                label = { 
                    Text(
                        text = bottomMenuItem.label,
                        color = if (selectedItem == bottomMenuItem.label) 
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