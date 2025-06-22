package com.example.facedetectorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.facedetectorapp.ui.screens.DashboardScreen
import com.example.facedetectorapp.ui.screens.UsersScreen
import com.example.facedetectorapp.ui.screens.CreateUserScreen
import com.example.facedetectorapp.ui.screens.EditUserScreen
import com.example.facedetectorapp.ui.screens.CompareImageScreen
import com.example.facedetectorapp.ui.screens.UserDetailScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("users") {
            UsersScreen(navController = navController)
        }
        composable("create_user") {
            CreateUserScreen(navController = navController)
        }
        composable("edit_user/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            EditUserScreen(navController = navController, userId = userId)
        }
        composable("compare_image") {
            CompareImageScreen(navController = navController)
        }
        composable("user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            UserDetailScreen(navController = navController, userId = userId)
        }
    }
}