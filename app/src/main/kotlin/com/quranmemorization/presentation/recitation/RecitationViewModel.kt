package com.quranmemorization.presentation.recitation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmemorization.domain.model.RecitationWord
import com.quranmemorization.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── حالة جلسة التسميع ────────────────────────────────────────────────────────

data class RecitationUiState(
    val words:           List<RecitationWord> = emptyList(),
    val isListening:     Boolean              = false,
    val isFinished:      Boolean              = false,
    val spokenText:      String               = "",
    val matchedCount:    Int                  = 0,
    val mistakeCount:    Int                  = 0,
    val score:           Int                  = 0,
    val errorMessage:    String?              = null,
    val canSkip:         Boolean              = true,   // تجاوز السورة المحفوظة
    val currentDayIndex: Int                  = 0,      // السورة الحالية في الجلسة
    val totalDays:       Int                  = 1,
    val portionTitle:    String               = "",
    val pageStart:       Int                  = 0,
    val pageEnd:         Int                  = 0,
    val sessionCompleted:Boolean              = false,
)

@HiltViewModel
class RecitationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: QuranRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecitationUiState())
    val state: StateFlow<RecitationUiState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var dayNumbers: List<Int> = emptyList()

    // كلمات الفاتحة كبيانات تجريبية — في الإنتاج تُحمَّل من قاعدة بيانات القرآن
    private val quranData: Map<Int, Pair<String, List<String>>> = mapOf(
        1  to Pair("الفاتحة", listOf(
            "بِسْمِ","اللَّهِ","الرَّحْمَٰنِ","الرَّحِيمِ",
            "الْحَمْدُ","لِلَّهِ","رَبِّ","الْعَالَمِينَ",
            "الرَّحْمَٰنِ","الرَّحِيمِ",
            "مَالِكِ","يَوْمِ","الدِّينِ",
            "إِيَّاكَ","نَعْبُدُ","وَإِيَّاكَ","نَسْتَعِينُ",
            "اهْدِنَا","الصِّرَاطَ","الْمُسْتَقِيمَ",
            "صِرَاطَ","الَّذِينَ","أَنْعَمْتَ","عَلَيْهِمْ",
            "غَيْرِ","الْمَغْضُوبِ","عَلَيْهِمْ","وَلَا","الضَّالِّينَ",
        )),
        2  to Pair("البقرة (أول صفحتين)", listOf(
            "الم","ذَٰلِكَ","الْكِتَابُ","لَا","رَيْبَ","فِيهِ","هُدًى","لِّلْمُتَّقِينَ",
            "الَّذِينَ","يُؤْمِنُونَ","بِالْغَيْبِ","وَيُقِيمُونَ","الصَّلَاةَ",
            "وَمِمَّا","رَزَقْنَاهُمْ","يُنفِقُونَ",
        )),
    )

    // ── تهيئة الجلسة ──────────────────────────────────────────────────────────

    fun initSession(dayNumber: Int, sessionType: String) {
        dayNumbers = listOf(dayNumber)
        loadCurrentDay()
    }

    fun initReviewSession(days: List<Int>) {
        dayNumbers = days
        loadCurrentDay()
    }

    private fun loadCurrentDay() {
        val index = _state.value.currentDayIndex
        if (index >= dayNumbers.size) {
            _state.update { it.copy(sessionCompleted = true) }
            return
        }
        val dayNum = dayNumbers[index]
        val data   = quranData[dayNum % quranData.size + 1] ?: quranData[1]!!
        val words  = data.second.mapIndexed { i, w ->
            RecitationWord(index = i, arabicText = w)
        }
        _state.update { it.copy(
            words        = words,
            portionTitle = data.first,
            pageStart    = dayNum,
            pageEnd      = dayNum + 1,
            totalDays    = dayNumbers.size,
            isFinished   = false,
            spokenText   = "",
            matchedCount = 0,
            mistakeCount = 0,
            score        = 0,
            errorMessage = null,
        )}
    }

    // ── التسميع الصوتي ────────────────────────────────────────────────────────

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.update { it.copy(errorMessage = "التعرف على الصوت غير متاح في هذا الجهاز") }
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.update { it.copy(isListening = true, errorMessage = null) }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken  = matches?.firstOrNull() ?: ""
                processSpokenText(spoken)
                _state.update { it.copy(isListening = false) }
            }
            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH      -> "لم يتم التعرف على الكلام، حاول مجدداً"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT-> "انتهت مهلة الاستماع"
                    SpeechRecognizer.ERROR_NETWORK       -> "خطأ في الشبكة — جرّب وضع عدم الاتصال"
                    else -> "خطأ في التعرف على الصوت ($error)"
                }
                _state.update { it.copy(isListening = false, errorMessage = msg) }
            }
            override fun onBeginningOfSpeech()             {}
            override fun onRmsChanged(rmsdB: Float)        {}
            override fun onBufferReceived(buffer: ByteArray?){}
            override fun onEndOfSpeech()                   {}
            override fun onPartialResults(partial: Bundle?) {
                val spoken = partial
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                _state.update { it.copy(spokenText = spoken) }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _state.update { it.copy(isListening = false) }
    }

    // ── مطابقة النص المنطوق مع الآيات ────────────────────────────────────────

    private fun processSpokenText(spoken: String) {
        val words        = _state.value.words
        val spokenNorm   = normalizeArabic(spoken)
        val spokenTokens = spokenNorm.split(" ").filter { it.isNotBlank() }

        var matchCount = 0
        val updatedWords = words.map { word ->
            val wordNorm = normalizeArabic(word.arabicText)
            val matched  = spokenTokens.any { token ->
                similarity(token, wordNorm) >= 0.75f
            }
            if (matched) {
                matchCount++
                word.copy(isRevealed = true, isMistake = false)
            } else {
                word
            }
        }

        val mistakeCount = words.size - matchCount
        val score        = if (words.isEmpty()) 0
                           else ((matchCount.toFloat() / words.size) * 100).toInt()

        _state.update { it.copy(
            words        = updatedWords,
            spokenText   = spoken,
            matchedCount = matchCount,
            mistakeCount = mistakeCount,
            score        = score,
        )}
    }

    // ── تجاوز السورة (حفظها من قبل) ─────────────────────────────────────────

    fun skipCurrentPortion() {
        val index  = _state.value.currentDayIndex
        val dayNum = dayNumbers.getOrNull(index) ?: return
        viewModelScope.launch {
            repository.markMemorizationComplete(dayNum, score = 100)
        }
        moveToNext()
    }

    // ── إنهاء التسميع وحفظ النتيجة ───────────────────────────────────────────

    fun finishCurrentPortion() {
        val score  = _state.value.score
        val index  = _state.value.currentDayIndex
        val dayNum = dayNumbers.getOrNull(index) ?: return
        viewModelScope.launch {
            repository.markMemorizationComplete(dayNum, score)
        }
        _state.update { it.copy(isFinished = true) }
    }

    fun moveToNext() {
        val nextIndex = _state.value.currentDayIndex + 1
        _state.update { it.copy(currentDayIndex = nextIndex, isFinished = false) }
        loadCurrentDay()
    }

    fun repeatCurrentPortion() {
        val words = _state.value.words.map {
            it.copy(isRevealed = false, isMistake = false)
        }
        _state.update { it.copy(
            words      = words,
            isFinished = false,
            spokenText = "",
            matchedCount = 0,
            mistakeCount = 0,
            score = 0,
        )}
    }

    // ── أدوات مساعدة ─────────────────────────────────────────────────────────

    /** تطبيع النص العربي: حذف التشكيل والهمزات للمطابقة المرنة */
    private fun normalizeArabic(text: String): String = text
        .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")  // حذف التشكيل
        .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
        .replace("ة", "ه").replace("ى", "ي")
        .replace("ؤ", "و").replace("ئ", "ي")
        .trim()

    /** حساب التشابه بين كلمتين (0.0 إلى 1.0) */
    private fun similarity(a: String, b: String): Float {
        if (a == b) return 1f
        if (a.isEmpty() || b.isEmpty()) return 0f
        val longer  = if (a.length > b.length) a else b
        val shorter = if (a.length > b.length) b else a
        if (longer.contains(shorter) || shorter.contains(longer)) return 0.85f
        val distance = levenshtein(a, b)
        return 1f - distance.toFloat() / longer.length.toFloat()
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                       else minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]) + 1
        }
        return dp[a.length][b.length]
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
