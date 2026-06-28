package com.example.samaajbot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.samaajbot.ui.auth.LoginScreen
import com.example.samaajbot.ui.auth.RegisterScreen
import com.example.samaajbot.ui.chat.ChatScreen
import com.example.samaajbot.ui.community.HomeScreen
import com.example.samaajbot.ui.documents.DocumentsScreen
import com.example.samaajbot.utils.SessionManager
import javax.inject.Inject

@Composable
fun NavGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val token by sessionManager.token.collectAsState(initial = null)
    val startDestination = if (token != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCommunityClick = { id, name, isAdmin ->
                    navController.navigate(Screen.Chat.createRoute(id, name, isAdmin))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("communityId") { type = NavType.IntType },
                navArgument("communityName") { type = NavType.StringType },
                navArgument("isAdmin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val communityId   = backStackEntry.arguments?.getInt("communityId") ?: 0
            val communityName = backStackEntry.arguments?.getString("communityName") ?: ""
            val isAdmin       = backStackEntry.arguments?.getBoolean("isAdmin") ?: false
            ChatScreen(
                communityId   = communityId,
                communityName = communityName,
                isAdmin       = isAdmin,
                onBack        = { navController.popBackStack() },
                onDocuments   = {
                    navController.navigate(Screen.Documents.createRoute(communityId, communityName, isAdmin))
                }
            )
        }

        composable(
            route = Screen.Documents.route,
            arguments = listOf(
                navArgument("communityId") { type = NavType.IntType },
                navArgument("communityName") { type = NavType.StringType },
                navArgument("isAdmin") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val communityId   = backStackEntry.arguments?.getInt("communityId") ?: 0
            val communityName = backStackEntry.arguments?.getString("communityName") ?: ""
            val isAdmin       = backStackEntry.arguments?.getBoolean("isAdmin") ?: false
            DocumentsScreen(
                communityId   = communityId,
                communityName = communityName,
                isAdmin       = isAdmin,
                onBack        = { navController.popBackStack() }
            )
        }
    }
}
