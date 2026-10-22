package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ReaderEmeraldBg
import com.example.ui.theme.ReaderEmeraldText
import com.example.ui.theme.ReaderNightBg
import com.example.ui.theme.ReaderNightText
import com.example.ui.theme.ReaderOledBg
import com.example.ui.theme.ReaderOledText
import com.example.ui.theme.ReaderPaperBg
import com.example.ui.theme.ReaderPaperText
import com.example.ui.theme.ReaderSepiaBg
import com.example.ui.theme.ReaderSepiaText

enum class ReaderTheme(
    val titleUrdu: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    PAPER("کاغذی (Paper)", ReaderPaperBg, ReaderPaperText),
    SEPIA("سیپیا (Sepia)", ReaderSepiaBg, ReaderSepiaText),
    NIGHT("رات کا موڈ (Night)", ReaderNightBg, ReaderNightText),
    OLED("گہرا سیاہ (OLED)", ReaderOledBg, ReaderOledText),
    EMERALD("زمردی (Emerald)", ReaderEmeraldBg, ReaderEmeraldText)
}

enum class FontStyleType(val urduLabel: String) {
    NASTALEEQ("نستعلیق انداز (Nastaliq)"),
    NASKH("نسخ انداز (Modern Naskh)")
}

data class ReaderSettings(
    val fontSizeSp: Float = 21f,
    val lineHeightMultiplier: Float = 1.8f,
    val theme: ReaderTheme = ReaderTheme.PAPER,
    val fontStyle: FontStyleType = FontStyleType.NASTALEEQ,
    val autoBookmark: Boolean = true
)
