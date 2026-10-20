package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AudioPlayerManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentWord = MutableStateFlow("")
    val currentWord: StateFlow<String> = _currentWord.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _isPlayerVisible = MutableStateFlow(false)
    val isPlayerVisible: StateFlow<Boolean> = _isPlayerVisible.asStateFlow()

    // Map of known Cambridge/Oxford dictionary ID prefixes to true words
    private val knownDictionaryMap = mapOf(
        "ukdocud" to "documents",
        "usdocud" to "documents",
        "ukmanuf" to "manuscripts",
        "usmanuf" to "manuscripts",
        "eus71387" to "dignity",
        "ukdigni" to "dignity",
        "usdigni" to "dignity",
        "ukvolum" to "volume",
        "usvolum" to "volume",
        "ukarchi" to "archive",
        "usarchi" to "archive",
        "ukautho" to "author",
        "usautho" to "author"
    )

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.US)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun extractWordFromUrl(url: String): String {
        try {
            val uri = Uri.parse(url)
            val queryText = uri.getQueryParameter("q")
                ?: uri.getQueryParameter("text")
                ?: uri.getQueryParameter("word")
                ?: uri.getQueryParameter("term")
            if (!queryText.isNullOrBlank()) {
                return Uri.decode(queryText).trim()
            }

            val pathSegments = uri.pathSegments
            if (pathSegments != null) {
                val wordIdx = pathSegments.indexOfFirst {
                    it.equals("word", ignoreCase = true) || it.equals("words", ignoreCase = true)
                }
                if (wordIdx != -1 && wordIdx + 1 < pathSegments.size) {
                    val w = Uri.decode(pathSegments[wordIdx + 1]).substringBefore(".").trim()
                    if (w.isNotBlank()) return w
                }
            }

            val lastSegment = uri.lastPathSegment ?: return "نطق الكلمة"
            var cleanName = if (lastSegment.contains(".")) {
                lastSegment.substringBeforeLast(".")
            } else {
                lastSegment
            }
            cleanName = Uri.decode(cleanName).trim()

            // Check dictionary ID mapping
            val lowerName = cleanName.lowercase(Locale.ROOT)
            for ((key, value) in knownDictionaryMap) {
                if (lowerName.startsWith(key)) {
                    return value
                }
            }

            cleanName = cleanName.replace(Regex("^(en[-_]|en[-_]us[-_]|en[-_]uk[-_]|en[-_]gb[-_])", RegexOption.IGNORE_CASE), "")
            cleanName = cleanName.replace(Regex("(__gb_\\d+|__us_\\d+|_gb_\\d+|_us_\\d+|_\\d+)$", RegexOption.IGNORE_CASE), "")
            cleanName = cleanName.replace("_", " ").replace("-", " ").trim()

            val isOpaqueId = cleanName.matches(Regex("^(uk[a-z]{3,7}|us[a-z]{0,7}|eus)\\d*$", RegexOption.IGNORE_CASE)) ||
                    cleanName.equals("eus", ignoreCase = true) ||
                    cleanName.matches(Regex("^[0-9_\\-\\s]+$"))

            if (isOpaqueId) {
                return "نطق الكلمة"
            }

            return cleanName.ifBlank { "نطق الكلمة" }
        } catch (e: Exception) {
            return "نطق الكلمة"
        }
    }

    fun play(url: String, explicitWord: String? = null) {
        val word = if (!explicitWord.isNullOrBlank() && explicitWord != "نطق الكلمة" && explicitWord != "كلمة غير معروفة") {
            explicitWord.trim()
        } else {
            extractWordFromUrl(url)
        }

        _currentWord.value = word
        _currentUrl.value = url
        _isPlayerVisible.value = true
        _isLoading.value = true
        _isPlaying.value = false

        releaseMediaPlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    _isLoading.value = false
                    _isPlaying.value = true
                    mp.start()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _isLoading.value = false
                }
                setOnErrorListener { _, _, _ ->
                    _isLoading.value = false
                    _isPlaying.value = false
                    // Fallback to offline TextToSpeech for reliability
                    playTtsFallback(word)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _isLoading.value = false
            playTtsFallback(word)
        }
    }

    private fun playTtsFallback(word: String) {
        if (isTtsReady && word.isNotBlank() && word != "نطق الكلمة") {
            _isPlaying.value = true
            textToSpeech?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "vocab_tts")
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(1200)
                _isPlaying.value = false
            }
        } else {
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null && player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else if (player != null) {
            player.start()
            _isPlaying.value = true
        } else {
            val url = _currentUrl.value
            if (url != null) {
                play(url, _currentWord.value)
            } else if (_currentWord.value.isNotBlank()) {
                playTtsFallback(_currentWord.value)
            }
        }
    }

    fun replay() {
        val player = mediaPlayer
        if (player != null) {
            try {
                player.seekTo(0)
                if (!player.isPlaying) {
                    player.start()
                    _isPlaying.value = true
                }
            } catch (e: Exception) {
                _currentUrl.value?.let { play(it, _currentWord.value) }
            }
        } else {
            _currentUrl.value?.let { play(it, _currentWord.value) }
                ?: playTtsFallback(_currentWord.value)
        }
    }

    fun dismiss() {
        releaseMediaPlayer()
        textToSpeech?.stop()
        _isPlayerVisible.value = false
        _isPlaying.value = false
        _isLoading.value = false
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun onDestroy() {
        releaseMediaPlayer()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
