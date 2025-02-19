package com.editz.ui.home

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.editz.theme.EditzColors
import com.editz.theme.EditzTheme
import com.editz.ui.home.components.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make status bar transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setContent {
            EditzTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
            .systemBarsPadding() // Add padding for system bars
            .padding(16.dp)
    ) {
        SearchBar()
        Spacer(modifier = Modifier.height(16.dp))
        NewProjectBanner()
        Spacer(modifier = Modifier.height(24.dp))
        QuickStats()
        FolderSection()
        ProjectsList()
        Spacer(modifier = Modifier.weight(1f))
        BottomNavigation()
    }
} 