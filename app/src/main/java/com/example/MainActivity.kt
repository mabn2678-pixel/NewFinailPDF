package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.audio.AudioPlayerManager
import com.example.ui.PdfViewModel
import com.example.ui.ViewerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var viewModel: PdfViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        audioPlayerManager = AudioPlayerManager(this)
        viewModel = PdfViewModel(audioPlayerManager)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ViewerScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioPlayerManager.isInitialized) {
            audioPlayerManager.onDestroy()
        }
    }
}
