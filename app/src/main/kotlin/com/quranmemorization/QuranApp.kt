package com.quranmemorization

import android.app.Application
import android.content.Intent
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@HiltAndroidApp
class QuranApp : Application() {

    override fun onCreate() {
        // يجب أن يكون هذا أول شيء قبل super.onCreate()
        setupCrashHandler()
        super.onCreate()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val errorText = buildString {
                    appendLine("Thread: ${thread.name}")
                    appendLine("Error: ${throwable::class.java.name}")
                    appendLine("Message: ${throwable.message}")
                    appendLine("─────────────────────")
                    appendLine(sw.toString())
                }

                // 1. اكتب للـ logcat
                Log.e("QURAN_CRASH", errorText)

                // 2. اكتب لملف في التخزين الداخلي
                try {
                    val file = File(filesDir, "crash_log.txt")
                    file.writeText(errorText)
                } catch (e: Exception) {
                    Log.e("QURAN_CRASH", "Failed to write crash file: ${e.message}")
                }

                // 3. حاول فتح شاشة الخطأ
                try {
                    val intent = Intent(this, CrashActivity::class.java).apply {
                        putExtra("error", errorText)
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                    }
                    startActivity(intent)
                    Thread.sleep(500)
                } catch (e: Exception) {
                    Log.e("QURAN_CRASH", "Failed to show crash screen: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e("QURAN_CRASH", "Error in crash handler: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
