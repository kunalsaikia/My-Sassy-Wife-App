package com.example.tappi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tappi.data.service.TtsManager
import com.example.tappi.ui.screens.ChatScreen
import com.example.tappi.ui.screens.SplashScreen
import com.example.tappi.ui.theme.TappiTheme
import com.example.tappi.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(this)

        setContent {
            val settings by viewModel.settings.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            TappiTheme(darkTheme = settings.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = showSplash,
                        label = "splash_crossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(
                                onDismiss = { showSplash = false }
                            )
                        } else {
                            ChatScreen(
                                viewModel = viewModel,
                                ttsManager = ttsManager
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
