package com.editz.ui.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.editz.theme.EditzColors

@Composable
fun BottomNavigation(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = EditzColors.Surface
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = { onScreenChange("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentScreen == "create",
            onClick = { onScreenChange("create") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )
        NavigationBarItem(
            selected = currentScreen == "pro",
            onClick = { onScreenChange("pro") },
            icon = { Icon(Icons.Default.Star, contentDescription = "Pro") },
            label = { Text("Pro") }
        )
        NavigationBarItem(
            selected = currentScreen == "files",
            onClick = { onScreenChange("files") },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
            label = { Text("Files") }
        )
        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = { onScreenChange("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
} 