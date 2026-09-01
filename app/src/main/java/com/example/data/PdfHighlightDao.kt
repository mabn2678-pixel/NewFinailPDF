package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfHighlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(highlight: PdfHighlight): Long

    @Update
    suspend fun update(highlight: PdfHighlight)

    @Query("DELETE FROM pdf_highlights WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM pdf_highlights WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PdfHighlight?

    @Query("SELECT * FROM pdf_highlights WHERE pdfPath = :pdfPath ORDER BY pageNumber ASC, sortIndex ASC, id ASC")
    fun getAllForPdf(pdfPath: String): Flow<List<PdfHighlight>>

    @Query("SELECT MAX(sortIndex) FROM pdf_highlights WHERE pdfPath = :pdfPath AND pageNumber = :pageNumber")
    suspend fun getMaxSortIndexForPage(pdfPath: String, pageNumber: Int): Int?
}
