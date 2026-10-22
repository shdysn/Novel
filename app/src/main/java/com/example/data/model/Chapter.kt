package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "id") val id: String,
    @Json(name = "novel_id") val novelId: String,
    @Json(name = "chapter_number") val chapterNumber: Int,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "word_count") val wordCount: Int = 0,
    @Json(name = "read_time_minutes") val readTimeMinutes: Int = 5
)

data class Bookmark(
    val novelId: String,
    val novelTitle: String,
    val author: String,
    val chapterId: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val scrollProgressPercent: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
