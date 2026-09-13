package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notifications.NotificationHelper
import com.example.ui.EstateIQApp
import com.example.ui.theme.EstateIQTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    // Initialize notification channels
    NotificationHelper.createNotificationChannel(this)

    setContent {
      EstateIQTheme {
        EstateIQApp()
      }
    }
  }
}

