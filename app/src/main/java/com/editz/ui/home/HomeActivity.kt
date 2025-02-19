package com.editz.ui.home

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.editz.theme.EditzColors
import com.editz.theme.EditzTheme
import com.editz.ui.create.CreateVideoScreen
import com.editz.ui.editor.VideoEditorScreen
import com.editz.ui.home.components.*
import com.editz.data.VideoDetails
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
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedVideo: VideoDetails? by remember { mutableStateOf(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditzColors.Background)
            .systemBarsPadding()
    ) {
        when (currentScreen) {
            Screen.Home -> HomeScreen()
            Screen.Create -> CreateVideoScreen(
                onNavigateToEditor = { videoDetails ->
                    selectedVideo = videoDetails
                    currentScreen = Screen.Editor
                }
            )
            Screen.Editor -> {
                selectedVideo?.let { video ->
                    VideoEditorScreen(
                        videoDetails = video,
                        onBack = {
                            selectedVideo = null
                            currentScreen = Screen.Create
                        }
                    )
                }
            }
            else -> HomeScreen()
        }
        
        if (currentScreen != Screen.Editor) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BottomNavigation(
                    currentScreen = currentScreen.name.lowercase(),
                    onScreenChange = { screenName ->
                        currentScreen = when (screenName.lowercase()) {
                            "home" -> Screen.Home
                            "create" -> Screen.Create
                            "pro" -> Screen.Pro
                            "files" -> Screen.Files
                            "profile" -> Screen.Profile
                            else -> Screen.Home
                        }
                    }
                )
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
            .padding(16.dp)
    ) {
        SearchBar()
        Spacer(modifier = Modifier.height(16.dp))
        NewProjectBanner()
        Spacer(modifier = Modifier.height(24.dp))
        QuickStats()
        FolderSection()
        ProjectsList()
    }
}

enum class Screen {
    Home,
    Create,
    Editor,
    Pro,
    Files,
    Profile
} 