package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PortfolioViewModel
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.AdminScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        // Enforce RTL Layout Direction
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val navController = rememberNavController()
            val viewModel: PortfolioViewModel = viewModel()
            
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(navController = navController, startDestination = "portfolio") {
                    composable("portfolio") {
                        PortfolioScreen(
                            innerPadding = innerPadding,
                            viewModel = viewModel,
                            onNavigateToAdmin = { navController.navigate("admin") },
                            onNavigateToChat = { navController.navigate("chat") }
                        )
                    }
                    composable("admin") {
                        AdminScreen(innerPadding = innerPadding, viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
                    }
                    composable("chat") {
                        com.example.ui.screens.ChatScreen(innerPadding = innerPadding, onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
      }
    }
  }
}
