package com.example.model

data class VocabularyItem(
    val id: String,
    val word: String,
    val translation: String,
    val phonetic: String,
    val audioUrl: String,
    val example: String,
    val partOfSpeech: String = "noun"
)

data class DocumentItem(
    val id: String,
    val title: String,
    val description: String,
    val pageCount: Int,
    val items: List<VocabularyItem> = emptyList()
)
