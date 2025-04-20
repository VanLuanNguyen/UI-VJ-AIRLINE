package com.vietjoke.vn.Activities.Dashboard

import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vietjoke.vn.R

data class BottomMenuItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun prepareBottomMenuItems(): List<BottomMenuItem> {
    return listOf(
        BottomMenuItem(label = "Home", icon = Icons.Default.Home),
        BottomMenuItem(label = "Cart", icon = Icons.Default.ShoppingCart),
        BottomMenuItem(label = "Favorite", icon = Icons.Default.Favorite),
        BottomMenuItem(label = "Order", icon = Icons.Default.List)
    )
}

@Composable
@Preview
fun MyBottomBar() {
    val bottomMenuItemsList = prepareBottomMenuItems()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Home") }

    BottomAppBar(
        backgroundColor = colorResource(id = R.color.white),
        elevation = 3.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected = selectedItem == bottomMenuItem.label,
                onClick = { selectedItem = bottomMenuItem.label },
                icon = { Icon(bottomMenuItem.icon, contentDescription = bottomMenuItem.label) },
                label = { Text(bottomMenuItem.label) }
            )
        }
    }
}