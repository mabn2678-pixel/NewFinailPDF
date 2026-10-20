package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.audio.AudioPlayerManager
import com.example.model.DocumentItem
import com.example.model.VocabularyItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ViewerUiState(
    val currentDocument: DocumentItem,
    val availableDocuments: List<DocumentItem>,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 4,
    val isNightMode: Boolean = false,
    val fontSizeScale: Float = 1.0f
)

class PdfViewModel(val audioManager: AudioPlayerManager) : ViewModel() {

    private val sampleDocuments = listOf(
        DocumentItem(
            id = "ielts_vocab",
            title = "Academic Vocabulary (IELTS & TOEFL)",
            description = "Selected academic vocabulary with authentic audio pronunciation links.",
            pageCount = 4,
            items = listOf(
                VocabularyItem(
                    id = "v1",
                    word = "documents",
                    translation = "وثائق ومستندات رسمية",
                    phonetic = "/ˈdɒk.jə.mənts/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/ukd/ukdoc/ukdocud029.mp3",
                    example = "All official documents must be signed and stamped by the authority.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v2",
                    word = "manuscripts",
                    translation = "مخطوطات أصلية قديمة",
                    phonetic = "/ˈmæn.jə.skrɪpts/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/ukm/ukman/ukmanuf019.mp3",
                    example = "The ancient library preserves thousands of historical manuscripts.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v3",
                    word = "dignity",
                    translation = "كرامة ووقار",
                    phonetic = "/ˈdɪɡ.nə.ti/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/us_pron/e/eus/eus71/eus71387.mp3",
                    example = "Every human being has the right to be treated with dignity and respect.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v4",
                    word = "volume",
                    translation = "مجلد أو حجم",
                    phonetic = "/ˈvɒl.juːm/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/ukv/ukvol/ukvolum001.mp3",
                    example = "This encyclopedia consists of twenty illustrated volumes.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v5",
                    word = "archive",
                    translation = "أرشيف وسجلات محفوظة",
                    phonetic = "/ˈɑː.kaɪv/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukarc/ukarchi001.mp3",
                    example = "Historical researchers searched the national archives for government records.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v6",
                    word = "author",
                    translation = "مؤلف أو كاتب",
                    phonetic = "/ˈɔː.θər/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/uka/ukaut/ukautho001.mp3",
                    example = "The author spent five years conducting thorough field research for the book.",
                    partOfSpeech = "noun"
                )
            )
        ),
        DocumentItem(
            id = "legal_terms",
            title = "Legal & Administrative Terminology",
            description = "Fundamental vocabulary found in legal and administrative publications.",
            pageCount = 3,
            items = listOf(
                VocabularyItem(
                    id = "v7",
                    word = "jurisdiction",
                    translation = "اختصاص قضائي",
                    phonetic = "/ˌdʒʊə.rɪsˈdɪk.ʃən/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/ukj/ukjur/ukjuris001.mp3",
                    example = "The federal court has sole jurisdiction over international trade disputes.",
                    partOfSpeech = "noun"
                ),
                VocabularyItem(
                    id = "v8",
                    word = "legislation",
                    translation = "تشريع وسن قوانين",
                    phonetic = "/ˌledʒ.ɪˈsleɪ.ʃən/",
                    audioUrl = "https://dictionary.cambridge.org/media/english/uk_pron/u/ukl/ukleg/uklegis001.mp3",
                    example = "New legislation was introduced to strengthen intellectual property protection.",
                    partOfSpeech = "noun"
                )
            )
        )
    )

    private val _uiState = MutableStateFlow(
        ViewerUiState(
            currentDocument = sampleDocuments.first(),
            availableDocuments = sampleDocuments
        )
    )
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    fun selectDocument(doc: DocumentItem) {
        _uiState.value = _uiState.value.copy(
            currentDocument = doc,
            currentPage = 1,
            totalPages = doc.pageCount
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun nextPage() {
        val current = _uiState.value.currentPage
        val total = _uiState.value.totalPages
        if (current < total) {
            _uiState.value = _uiState.value.copy(currentPage = current + 1)
        }
    }

    fun previousPage() {
        val current = _uiState.value.currentPage
        if (current > 1) {
            _uiState.value = _uiState.value.copy(currentPage = current - 1)
        }
    }

    fun toggleNightMode() {
        _uiState.value = _uiState.value.copy(isNightMode = !_uiState.value.isNightMode)
    }

    fun adjustFontSize(delta: Float) {
        val newScale = (_uiState.value.fontSizeScale + delta).coerceIn(0.8f, 1.6f)
        _uiState.value = _uiState.value.copy(fontSizeScale = newScale)
    }

    fun playVocabularyAudio(item: VocabularyItem) {
        audioManager.play(item.audioUrl, item.word)
    }

    fun playAudioUrl(url: String, detectedWord: String? = null) {
        audioManager.play(url, detectedWord)
    }
}
