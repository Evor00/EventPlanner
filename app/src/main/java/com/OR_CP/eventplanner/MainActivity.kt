package com.OR_CP.eventplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.OR_CP.eventplanner.IU.screens.EventFormScreen
import com.OR_CP.eventplanner.IU.screens.EventListScreen
import com.OR_CP.eventplanner.IU.screens.LoginScreen
import com.OR_CP.eventplanner.IU.screens.RegisterScreen
import com.OR_CP.eventplanner.ui.theme.EventPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventPlannerTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    composable("login") {
                        LoginScreen(navController)
                    }

                    composable("register") {
                        RegisterScreen(navController)
                    }

                    composable("event_list") {
                        EventListScreen(navController)
                    }

                    composable("event_form") {
                        EventFormScreen(navController)
                    }
                }
            }
        }
    }
}
