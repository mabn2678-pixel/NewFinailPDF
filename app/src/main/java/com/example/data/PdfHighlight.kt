package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pdf_highlights",
    indices = [Index(value = ["pdfPath"])]
)
data class PdfHighlight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pdfPath: String,
    val pageNumber: Int,
    val selectedText: String,
    val color: Int,
    val quads: String, // JSON array of relative rects: [{"x":0.1,"y":0.2,"w":0.3,"h":0.02}]
    val sortIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
