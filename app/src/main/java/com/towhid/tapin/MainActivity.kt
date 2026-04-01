package com.towhid.tapin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.towhid.tapin.presentation.attendance.HomeScreen
import com.towhid.tapin.ui.theme.TapInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapInTheme {
                HomeScreen()
            }
        }
    }
}
