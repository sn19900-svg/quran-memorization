package com.quranmemorization

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import java.io.File

// لا تستخدم Hilt هنا — Activity عادي بـ Android Views وليس Compose
class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اقرأ الخطأ من Intent أو من الملف
        val errorText = intent.getStringExtra("error")
            ?: runCatching { File(filesDir, "crash_log.txt").readText() }.getOrNull()
            ?: "لم يتم العثور على تفاصيل الخطأ"

        // بناء الـ UI بدون XML وبدون Compose
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A0000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 64, 32, 32)
        }

        val title = TextView(this).apply {
            text = "💥 خطأ عند تشغيل التطبيق"
            setTextColor(Color.RED)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "\nتفاصيل الخطأ (أرسل هذا النص للمطور):\n"
            setTextColor(Color.YELLOW)
            textSize = 13f
        }

        val errorView = TextView(this).apply {
            text = errorText
            setTextColor(Color.WHITE)
            textSize = 10f
            typeface = Typeface.MONOSPACE
            setHorizontallyScrolling(true)
        }

        val scrollView = ScrollView(this).apply {
            addView(errorView)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val closeBtn = Button(this).apply {
            text = "إغلاق"
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            setOnClickListener { finish() }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(scrollView)
        root.addView(closeBtn)

        setContentView(root)
    }

    companion object {
        fun launch(context: Context, error: String) {
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra("error", error)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}
