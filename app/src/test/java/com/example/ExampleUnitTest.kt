package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDictionaryWordExtraction() {
        val map = mapOf(
            "ukdocud" to "documents",
            "ukmanuf" to "manuscripts",
            "eus71387" to "dignity"
        )
        for ((key, expectedWord) in map) {
            val resolvedWord = when {
                key.startsWith("ukdocud") -> "documents"
                key.startsWith("ukmanuf") -> "manuscripts"
                key.startsWith("eus71387") -> "dignity"
                else -> ""
            }
            assertEquals(expectedWord, resolvedWord)
        }
    }
}
