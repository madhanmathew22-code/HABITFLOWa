package com.habitflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.ui.navigation.HabitFlowNavHost
import com.habitflow.app.ui.theme.HabitFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitFlowApp()
        }
    }
}

@Composable
private fun HabitFlowApp() {
    HabitFlowTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            HabitFlowNavHost(navController = navController)
        }
    }
}
