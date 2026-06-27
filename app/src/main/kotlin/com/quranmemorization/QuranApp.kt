package com.quranmemorization

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * QuranApp — the Hilt-enabled Application class.
 *
 * The @HiltAndroidApp annotation triggers Hilt's code generation and creates
 * the application-level dependency container.  Every @Singleton binding lives
 * for the lifetime of this class.
 *
 * No manual initialisation is needed here — Hilt injects the ScheduleSeeder
 * into the repository, which calls seedIfEmpty() the first time the Dashboard
 * is opened.
 */
@HiltAndroidApp
class QuranApp : Application()
