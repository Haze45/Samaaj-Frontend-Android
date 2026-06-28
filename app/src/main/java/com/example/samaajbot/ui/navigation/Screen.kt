package com.example.samaajbot.ui.navigation

sealed class Screen(val route: String) {
    object Splash     : Screen("splash")
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Home       : Screen("home")
    object Chat       : Screen("chat/{communityId}/{communityName}/{isAdmin}") {
        fun createRoute(communityId: Int, communityName: String, isAdmin: Boolean) =
            "chat/$communityId/$communityName/$isAdmin"
    }
    object Documents  : Screen("documents/{communityId}/{communityName}/{isAdmin}") {
        fun createRoute(communityId: Int, communityName: String, isAdmin: Boolean) =
            "documents/$communityId/$communityName/$isAdmin"
    }
}
