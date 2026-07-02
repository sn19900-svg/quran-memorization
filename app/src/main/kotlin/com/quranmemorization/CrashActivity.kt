package com.quranmemorization

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranmemorization.presentation.theme.QuranMemorizationTheme

class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorText = intent.getStringExtra("error") ?: "خطأ غير معروف"

        setContent {
            QuranMemorizationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Color(0xFF1A0000),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text      = "💥 حدث خطأ عند التشغيل",
                            color     = Color.Red,
                            fontSize  = 20.sp,
                            fontWeight= FontWeight.Bold,
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text      = "تفاصيل الخطأ:",
                            color     = Color.Yellow,
                            fontSize  = 14.sp,
                            fontWeight= FontWeight.Bold,
                        )

                        Spacer(Modifier.height(8.dp))

                        // نص الخطأ قابل للتمرير أفقياً وعمودياً
                        Text(
                            text       = errorText,
                            color      = Color.White,
                            fontSize   = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            lineHeight = 16.sp,
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { finish() },
                            colors  = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        ) {
                            Text("إغلاق")
                        }
                    }
                }
            }
        }
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
