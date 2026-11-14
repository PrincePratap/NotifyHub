package com.cody.notifyhub.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cody.notifyhub.ui.detail.DetailRoute
import com.cody.notifyhub.ui.inbox.InboxRoute

object Routes {
    const val INBOX = "inbox"
    const val DETAIL = "detail/{id}"
    fun detail(id: String) = "detail/${Uri.encode(id)}"   // encode: an id containing "/" would break the route
}

@Composable
fun AppNavGraph(openInboxSignal: Int) {
    val navController = rememberNavController()

    // Notification tapped while the app is open → go back to the Inbox
    LaunchedEffect(openInboxSignal) {
        if (openInboxSignal > 0) navController.popBackStack(Routes.INBOX, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Routes.INBOX) {
        composable(Routes.INBOX) {
            InboxRoute(onOpenDetail = { id -> navController.navigate(Routes.detail(id)) })
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            DetailRoute(onBack = { navController.popBackStack() })
        }
    }
}