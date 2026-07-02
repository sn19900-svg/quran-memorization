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
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // تحقق إذا كان هناك crash log من جلسة سابقة
        val crashLog = File(filesDir, "crash_log.txt")
        if (crashLog.exists()) {
            val errorText = crashLog.readText()
            crashLog.delete()
            CrashActivity.launch(this, errorText)
            finish()
            return
        }

        setContent {
            QuranMemorizationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController    = navController,
                        startDestination = "dashboard",
                    ) {
                        composable("dashboard") { DashboardScreen() }
                    }
                }
            }
        }
    }
}
