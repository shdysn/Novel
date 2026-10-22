package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.SampleUrduNovels
import com.example.data.model.Author
import com.example.data.model.Bookmark
import com.example.data.model.Chapter
import com.example.data.model.FontStyleType
import com.example.data.model.Novel
import com.example.data.model.ReaderSettings
import com.example.data.model.ReaderTheme
import com.example.data.model.SupabaseConfig
import com.example.data.network.SupabaseApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class NovelRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("urdu_novel_prefs", Context.MODE_PRIVATE)

    // State flows
    private val _novels = MutableStateFlow<List<Novel>>(emptyList())
    val novels: StateFlow<List<Novel>> = _novels.asStateFlow()

    private val _authors = MutableStateFlow<List<Author>>(SampleUrduNovels.sampleAuthors)
    val authors: StateFlow<List<Author>> = _authors.asStateFlow()

    private val _customChapters = mutableMapOf<String, MutableList<Chapter>>()

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("پیرِ کامل", "عمیرہ احمد", "جنت کے پتے", "جاسوسی ناولز", "عمران سیریز", "محبت")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val popularSearches: List<String> = listOf(
        "پیرِ کامل", "راجہ گدھ", "نمل", "جنت کے پتے", "خدا اور محبت", "ابنِ صفی", "بانو قدسیہ", "رومانوی", "سسپنس"
    )

    private val _readerSettings = MutableStateFlow(ReaderSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    private val _supabaseConfig = MutableStateFlow(SupabaseConfig())
    val supabaseConfig: StateFlow<SupabaseConfig> = _supabaseConfig.asStateFlow()

    private val _syncingState = MutableStateFlow(false)
    val syncingState: StateFlow<Boolean> = _syncingState.asStateFlow()

    // Profile settings
    val notificationsEnabled = MutableStateFlow(prefs.getBoolean("pref_notif", true))
    val wifiOnlyDownload = MutableStateFlow(prefs.getBoolean("pref_wifi", true))

    init {
        loadSettings()
        loadSupabaseConfig()
        loadInitialNovels()
        loadBookmarks()
        loadRecentSearches()
    }

    private fun loadSettings() {
        val fontSize = prefs.getFloat("font_size", 21f)
        val lineSpacing = prefs.getFloat("line_spacing", 1.8f)
        val themeName = prefs.getString("reader_theme", ReaderTheme.PAPER.name) ?: ReaderTheme.PAPER.name
        val theme = try { ReaderTheme.valueOf(themeName) } catch (e: Exception) { ReaderTheme.PAPER }
        val fontStyleName = prefs.getString("font_style", FontStyleType.NASTALEEQ.name) ?: FontStyleType.NASTALEEQ.name
        val fontStyle = try { FontStyleType.valueOf(fontStyleName) } catch (e: Exception) { FontStyleType.NASTALEEQ }

        _readerSettings.value = ReaderSettings(
            fontSizeSp = fontSize,
            lineHeightMultiplier = lineSpacing,
            theme = theme,
            fontStyle = fontStyle
        )
    }

    fun updateReaderSettings(settings: ReaderSettings) {
        _readerSettings.value = settings
        prefs.edit()
            .putFloat("font_size", settings.fontSizeSp)
            .putFloat("line_spacing", settings.lineHeightMultiplier)
            .putString("reader_theme", settings.theme.name)
            .putString("font_style", settings.fontStyle.name)
            .apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
        prefs.edit().putBoolean("pref_notif", enabled).apply()
    }

    fun setWifiOnlyDownload(enabled: Boolean) {
        wifiOnlyDownload.value = enabled
        prefs.edit().putBoolean("pref_wifi", enabled).apply()
    }

    private fun loadSupabaseConfig() {
        val url = prefs.getString("sb_url", "") ?: ""
        val key = prefs.getString("sb_key", "") ?: ""
        val isConnected = prefs.getBoolean("sb_connected", false)
        val lastMsg = prefs.getString("sb_msg", "کنکشن کا انتظار ہے") ?: "کنکشن کا انتظار ہے"
        val totalSynced = prefs.getInt("sb_synced_count", 0)

        _supabaseConfig.value = SupabaseConfig(
            url = url,
            anonKey = key,
            isConnected = isConnected,
            lastSyncMessage = lastMsg,
            totalSyncedNovels = totalSynced
        )
    }

    fun saveSupabaseConfig(url: String, key: String) {
        val cleanUrl = url.trim()
        val cleanKey = key.trim()
        _supabaseConfig.value = _supabaseConfig.value.copy(
            url = cleanUrl,
            anonKey = cleanKey,
            lastSyncMessage = "ترتیبات محفوظ ہو گئیں"
        )
        prefs.edit()
            .putString("sb_url", cleanUrl)
            .putString("sb_key", cleanKey)
            .putString("sb_msg", "ترتیبات محفوظ ہو گئیں")
            .apply()
    }

    private fun loadInitialNovels() {
        val list = SampleUrduNovels.novelsList.toMutableList()
        _novels.value = list
    }

    fun getNovelById(id: String): Novel? {
        return _novels.value.firstOrNull { it.id == id }
    }

    fun getChapters(novelId: String): List<Chapter> {
        val defaultList = SampleUrduNovels.chaptersMap[novelId] ?: emptyList()
        val customList = _customChapters[novelId] ?: emptyList()
        return (defaultList + customList).sortedBy { it.chapterNumber }
    }

    fun getChapter(novelId: String, chapterId: String): Chapter? {
        return getChapters(novelId).firstOrNull { it.id == chapterId }
    }

    fun toggleFavorite(novelId: String) {
        _novels.value = _novels.value.map {
            if (it.id == novelId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun updateReadingProgress(novelId: String, progress: Float) {
        _novels.value = _novels.value.map {
            if (it.id == novelId) it.copy(readingProgress = progress) else it
        }
    }

    fun saveBookmark(novel: Novel, chapter: Chapter, progressPercent: Float) {
        val existing = _bookmarks.value.filterNot { it.novelId == novel.id }
        val newBookmark = Bookmark(
            novelId = novel.id,
            novelTitle = novel.title,
            author = novel.author,
            chapterId = chapter.id,
            chapterNumber = chapter.chapterNumber,
            chapterTitle = chapter.title,
            scrollProgressPercent = progressPercent,
            timestamp = System.currentTimeMillis()
        )
        _bookmarks.value = listOf(newBookmark) + existing
        updateReadingProgress(novel.id, progressPercent)
        saveBookmarksToPrefs()
    }

    fun removeBookmark(novelId: String) {
        _bookmarks.value = _bookmarks.value.filterNot { it.novelId == novelId }
        saveBookmarksToPrefs()
    }

    private fun saveBookmarksToPrefs() {
        val joined = _bookmarks.value.joinToString(";") {
            "${it.novelId}|${it.novelTitle}|${it.author}|${it.chapterId}|${it.chapterNumber}|${it.chapterTitle}|${it.scrollProgressPercent}"
        }
        prefs.edit().putString("saved_bookmarks", joined).apply()
    }

    private fun loadBookmarks() {
        val raw = prefs.getString("saved_bookmarks", "") ?: ""
        if (raw.isBlank()) return
        val list = mutableListOf<Bookmark>()
        raw.split(";").forEach { item ->
            val parts = item.split("|")
            if (parts.size >= 7) {
                list.add(
                    Bookmark(
                        novelId = parts[0],
                        novelTitle = parts[1],
                        author = parts[2],
                        chapterId = parts[3],
                        chapterNumber = parts[4].toIntOrNull() ?: 1,
                        chapterTitle = parts[5],
                        scrollProgressPercent = parts[6].toFloatOrNull() ?: 0f
                    )
                )
            }
        }
        _bookmarks.value = list
    }

    // Searches management
    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val updated = (listOf(trimmed) + _recentSearches.value.filterNot { it.equals(trimmed, ignoreCase = true) }).take(10)
        _recentSearches.value = updated
        prefs.edit().putString("recent_searches", updated.joinToString(",")).apply()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        prefs.edit().remove("recent_searches").apply()
    }

    private fun loadRecentSearches() {
        val saved = prefs.getString("recent_searches", "") ?: ""
        if (saved.isNotBlank()) {
            _recentSearches.value = saved.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    fun clearReadingHistory() {
        _novels.value = _novels.value.map { it.copy(readingProgress = 0f) }
        _bookmarks.value = emptyList()
        prefs.edit().remove("saved_bookmarks").apply()
    }

    // Publisher Studio functions
    suspend fun createCustomNovel(
        title: String,
        author: String,
        category: String,
        synopsis: String,
        initialChapterTitle: String,
        initialChapterContent: String
    ): Pair<Novel, Chapter> = withContext(Dispatchers.IO) {
        val novelId = "custom_" + UUID.randomUUID().toString().take(8)
        val chapterId = "ch_" + UUID.randomUUID().toString().take(8)

        val words = initialChapterContent.trim().split(Regex("\\s+")).size
        val readTime = maxOf(1, words / 100)

        val newNovel = Novel(
            id = novelId,
            title = title.trim(),
            author = author.trim(),
            category = category,
            synopsis = synopsis.trim(),
            rating = 5.0f,
            viewsCount = 1,
            coverGradientIndex = (_novels.value.size % 5),
            totalChapters = 1,
            isFavorite = true,
            isDownloaded = true,
            isNew = true,
            isRecommended = true,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
            isCustomPublished = true
        )

        val newChapter = Chapter(
            id = chapterId,
            novelId = novelId,
            chapterNumber = 1,
            title = initialChapterTitle.trim().ifEmpty { "باب اول" },
            content = initialChapterContent.trim(),
            wordCount = words,
            readTimeMinutes = readTime
        )

        _customChapters.getOrPut(novelId) { mutableListOf() }.add(newChapter)
        _novels.value = listOf(newNovel) + _novels.value

        Pair(newNovel, newChapter)
    }

    suspend fun addChapterToNovel(
        novelId: String,
        chapterTitle: String,
        content: String
    ): Chapter = withContext(Dispatchers.IO) {
        val currentChapters = getChapters(novelId)
        val nextNumber = currentChapters.size + 1
        val chapterId = "ch_" + UUID.randomUUID().toString().take(8)
        val words = content.trim().split(Regex("\\s+")).size
        val readTime = maxOf(1, words / 100)

        val newChapter = Chapter(
            id = chapterId,
            novelId = novelId,
            chapterNumber = nextNumber,
            title = chapterTitle.trim().ifEmpty { "باب $nextNumber" },
            content = content.trim(),
            wordCount = words,
            readTimeMinutes = readTime
        )

        _customChapters.getOrPut(novelId) { mutableListOf() }.add(newChapter)

        _novels.value = _novels.value.map {
            if (it.id == novelId) it.copy(totalChapters = it.totalChapters + 1) else it
        }

        newChapter
    }

    // Supabase Cloud Push & Sync
    suspend fun testSupabaseConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val config = _supabaseConfig.value
        if (!config.isConfigured) {
            return@withContext Pair(false, "براہ کرم Supabase کا درست URL اور Anon Key درج کریں")
        }

        try {
            val client = SupabaseApiService.createClient(config.url)
                ?: return@withContext Pair(false, "غلط URL فارمیٹ")

            val response = client.getNovels(config.anonKey, "Bearer ${config.anonKey}")
            if (response.isSuccessful) {
                _supabaseConfig.value = _supabaseConfig.value.copy(
                    isConnected = true,
                    lastSyncMessage = "Supabase سے رابطہ کامیابی سے قائم ہو گیا!",
                    lastSyncTime = System.currentTimeMillis()
                )
                prefs.edit().putBoolean("sb_connected", true).apply()
                Pair(true, "کامیابی! Supabase کلاؤڈ کنکشن ایکٹیو ہے")
            } else {
                val errorMsg = "رابطہ ناکام رہا (کوڈ: ${response.code()})"
                _supabaseConfig.value = _supabaseConfig.value.copy(
                    isConnected = false,
                    lastSyncMessage = errorMsg
                )
                prefs.edit().putBoolean("sb_connected", false).apply()
                Pair(false, errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "کنکشن کی خرابی: ${e.localizedMessage ?: "نیٹ ورک کا مسئلہ"}"
            _supabaseConfig.value = _supabaseConfig.value.copy(
                isConnected = false,
                lastSyncMessage = errorMsg
            )
            prefs.edit().putBoolean("sb_connected", false).apply()
            Pair(false, errorMsg)
        }
    }

    suspend fun syncWithSupabase(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val config = _supabaseConfig.value
        if (!config.isConfigured) {
            return@withContext Pair(false, "Supabase کنفیگر نہیں ہے۔ براہ کرم ترتیبات میں جائیں۔")
        }

        _syncingState.value = true
        try {
            val client = SupabaseApiService.createClient(config.url)
                ?: return@withContext Pair(false, "Supabase کلائنٹ تیار نہیں ہو سکا")

            val response = client.getNovels(config.anonKey, "Bearer ${config.anonKey}")
            if (response.isSuccessful) {
                val remoteNovels = response.body() ?: emptyList()
                if (remoteNovels.isNotEmpty()) {
                    val merged = (remoteNovels + _novels.value).distinctBy { it.id }
                    _novels.value = merged
                }

                _supabaseConfig.value = _supabaseConfig.value.copy(
                    isConnected = true,
                    totalSyncedNovels = remoteNovels.size,
                    lastSyncMessage = "تازہ ترین ناولز سنک ہو گئے۔ کلاؤڈ ناولز: ${remoteNovels.size}",
                    lastSyncTime = System.currentTimeMillis()
                )
                Pair(true, "کامیابی! ${remoteNovels.size} ناولز کلاؤڈ سے سنک ہو گئے")
            } else {
                Pair(false, "کلاؤڈ سے ڈیٹا حاصل نہیں ہو سکا: کوڈ ${response.code()}")
            }
        } catch (e: Exception) {
            Pair(false, "سنک کے دوران خرابی: ${e.localizedMessage ?: "کنکشن ٹائم آؤٹ"}")
        } finally {
            _syncingState.value = false
        }
    }

    suspend fun pushNovelToSupabase(novelId: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val config = _supabaseConfig.value
        if (!config.isConfigured) {
            return@withContext Pair(false, "ڈیٹا پش کرنے کے لیے Supabase URL اور Key درکار ہے")
        }

        val novel = getNovelById(novelId) ?: return@withContext Pair(false, "ناول نہیں ملا")
        val chapters = getChapters(novelId)

        _syncingState.value = true
        try {
            val client = SupabaseApiService.createClient(config.url)
                ?: return@withContext Pair(false, "Supabase کلائنٹ کی خرابی")

            val novelRes = client.pushNovel(novel, config.anonKey, "Bearer ${config.anonKey}")
            if (!novelRes.isSuccessful && novelRes.code() != 409) {
                return@withContext Pair(false, "ناول پش نہ ہو سکا: کوڈ ${novelRes.code()}")
            }

            if (chapters.isNotEmpty()) {
                val chapRes = client.pushChapters(chapters, config.anonKey, "Bearer ${config.anonKey}")
                if (!chapRes.isSuccessful && chapRes.code() != 409) {
                    return@withContext Pair(false, "ابواب پش نہ ہو سکے: کوڈ ${chapRes.code()}")
                }
            }

            Pair(true, "ناول اور ${chapters.size} ابواب کامیابی سے Supabase کلاؤڈ پر پش ہو گئے!")
        } catch (e: Exception) {
            Pair(false, "پش کے دوران خرابی: ${e.localizedMessage ?: "نیٹ ورک کا مسئلہ"}")
        } finally {
            _syncingState.value = false
        }
    }
}
