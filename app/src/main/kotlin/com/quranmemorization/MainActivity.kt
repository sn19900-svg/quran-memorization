package com.quranmemorization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quranmemorization.presentation.dashboard.DashboardScreen
import com.quranmemorization.presentation.theme.QuranMemorizationTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — single-activity host for the entire Compose navigation graph.
 *
 * Responsibilities:
 *  • Install the splash screen (shows while Hilt + DB initialise).
 *  • Enable edge-to-edge rendering for modern Android look.
 *  • Set up the NavHost with all top-level destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() so the splash is shown
        // during the first DB seed on a fresh install.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuranMemorizationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController    = navController,
                        startDestination = NavRoutes.DASHBOARD,
                    ) {
                        composable(NavRoutes.DASHBOARD) {
                            DashboardScreen()
                        }
                        // Future destinations:
                        // composable(NavRoutes.SCHEDULE)  { ScheduleScreen(navController) }
                        // composable(NavRoutes.STATISTICS){ StatisticsScreen(navController) }
                        // composable(NavRoutes.SETTINGS)  { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}

/** Centralised navigation route constants — avoids magic strings across the codebase. */
object NavRoutes {
    const val DASHBOARD  = "dashboard"
    const val SCHEDULE   = "schedule"
    const val STATISTICS = "statistics"
    const val SETTINGS   = "settings"
}
