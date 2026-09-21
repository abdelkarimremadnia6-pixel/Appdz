package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.AppViewModelFactory
import com.example.ui.navigation.MainNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as InventoryApplication
        val repository = app.repository
        val viewModelFactory = AppViewModelFactory(app, repository)

        setContent {
            val settings by repository.appSettings.collectAsState(initial = null)
            val isDarkTheme = settings?.isDarkTheme ?: true
            val language = settings?.language ?: "en"

            val layoutDirection = if (language == "ar") {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainNavigation(
                            viewModelFactory = viewModelFactory,
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}

