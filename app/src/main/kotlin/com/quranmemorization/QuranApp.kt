package com.quranmemorization

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.PrintWriter
import java.io.StringWriter

@HiltAndroidApp
class QuranApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // تحويل الخطأ كاملاً إلى نص
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val errorText = buildString {
                    appendLine("Thread: ${thread.name}")
                    appendLine("Error: ${throwable::class.java.simpleName}")
                    appendLine("Message: ${throwable.message}")
                    appendLine("─────────────────────")
                    appendLine(sw.toString())
                }

                // عرض شاشة الخطأ
                CrashActivity.launch(applicationContext, errorText)

            } catch (e: Exception) {
                // إذا فشل حتى handler الخاص بنا، نرجع للـ handler الافتراضي
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
