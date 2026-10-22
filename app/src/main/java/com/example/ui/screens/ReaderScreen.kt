package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FontStyleType
import com.example.ui.components.ReaderSettingsSheet
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.NovelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    novelId: String,
    chapterId: String,
    viewModel: NovelViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val novel = viewModel.getNovel(novelId)
    val chapter = viewModel.getChapter(novelId, chapterId)
    val allChapters = viewModel.getChapters(novelId)
    val readerSettings by viewModel.readerSettings.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isBookmarked by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Calculate reading progress percentage
    val readingProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    // Auto-save bookmark on exit or when user taps bookmark icon
    LaunchedEffect(chapterId) {
        scrollState.scrollTo(0)
    }

    if (novel == null || chapter == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("باب دستیاب نہیں ہے", fontFamily = FontFamily.Serif)
        }
        return
    }

    val fontFam = if (readerSettings.fontStyle == FontStyleType.NASTALEEQ) FontFamily.Serif else FontFamily.Default
    val activeTheme = readerSettings.theme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activeTheme.backgroundColor)
            .testTag("reader_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                color = activeTheme.backgroundColor,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.navigateBack() },
                                modifier = Modifier.testTag("reader_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = activeTheme.textColor
                                )
                            }

                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = novel.title,
                                    fontFamily = fontFam,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = activeTheme.textColor,
                                    maxLines = 1
                                )
                                Text(
                                    text = chapter.title,
                                    fontFamily = fontFam,
                                    fontSize = 12.sp,
                                    color = activeTheme.textColor.copy(alpha = 0.75f),
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Search in text button
                            IconButton(onClick = { showSearchBar = !showSearchBar }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search in chapter",
                                    tint = activeTheme.textColor
                                )
                            }

                            // Reader Settings (Font size, Theme, Line Height)
                            IconButton(
                                onClick = { showSettingsSheet = true },
                                modifier = Modifier.testTag("reader_appearance_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Font settings",
                                    tint = activeTheme.textColor
                                )
                            }

                            // Bookmark Button
                            IconButton(
                                onClick = {
                                    isBookmarked = !isBookmarked
                                    viewModel.saveBookmark(novel, chapter, readingProgress)
                                },
                                modifier = Modifier.testTag("save_bookmark_button")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) GoldAccent else activeTheme.textColor
                                )
                            }

                            // Chapters List Drawer
                            IconButton(onClick = { showChaptersSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Chapters list",
                                    tint = activeTheme.textColor
                                )
                            }
                        }
                    }

                    // Optional In-Chapter Search Bar
                    AnimatedVisibility(visible = showSearchBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "اس باب میں لفظ تلاش کریں...",
                                        fontFamily = fontFam,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = activeTheme.backgroundColor,
                                    unfocusedContainerColor = activeTheme.backgroundColor,
                                    focusedTextColor = activeTheme.textColor,
                                    unfocusedTextColor = activeTheme.textColor
                                )
                            )
                        }
                    }
                }
            }

            // Novel Reader Scrollable Text Body (Pure Unicode!)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
                    .testTag("novel_text_content")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Chapter Heading Decoration
                    Text(
                        text = "﷽",
                        fontFamily = fontFam,
                        fontSize = (readerSettings.fontSizeSp + 4).sp,
                        color = activeTheme.textColor.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "❦ ${chapter.title} ❦",
                        fontFamily = fontFam,
                        fontWeight = FontWeight.Bold,
                        fontSize = (readerSettings.fontSizeSp + 3).sp,
                        color = activeTheme.textColor,
                        textAlign = TextAlign.Center,
                        lineHeight = ((readerSettings.fontSizeSp + 3) * 1.6f).sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Render Unicode Text with search highlight if applicable
                    val annotatedContent = remember(chapter.content, searchQuery, activeTheme) {
                        buildAnnotatedString {
                            val text = chapter.content
                            if (searchQuery.isBlank()) {
                                append(text)
                            } else {
                                var startIndex = 0
                                while (startIndex < text.length) {
                                    val index = text.indexOf(searchQuery, startIndex, ignoreCase = true)
                                    if (index == -1) {
                                        append(text.substring(startIndex))
                                        break
                                    }
                                    append(text.substring(startIndex, index))
                                    withStyle(
                                        style = SpanStyle(
                                            background = GoldAccent.copy(alpha = 0.5f),
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append(text.substring(index, index + searchQuery.length))
                                    }
                                    startIndex = index + searchQuery.length
                                }
                            }
                        }
                    }

                    Text(
                        text = annotatedContent,
                        fontFamily = fontFam,
                        fontSize = readerSettings.fontSizeSp.sp,
                        lineHeight = (readerSettings.fontSizeSp * readerSettings.lineHeightMultiplier).sp,
                        color = activeTheme.textColor,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // End of chapter flourish
                    Text(
                        text = "✦ ✦ ✦",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Text(
                        text = "— ختم شد —",
                        fontFamily = fontFam,
                        fontSize = 14.sp,
                        color = activeTheme.textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Reader Bottom Control Bar
            Surface(
                color = activeTheme.backgroundColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Progress Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(readingProgress * 100).toInt()}%",
                            fontFamily = fontFam,
                            fontSize = 12.sp,
                            color = activeTheme.textColor.copy(alpha = 0.8f),
                            modifier = Modifier.width(36.dp)
                        )

                        Slider(
                            value = readingProgress,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldPrimary,
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = activeTheme.textColor.copy(alpha = 0.15f)
                            )
                        )

                        Text(
                            text = "باب ${chapter.chapterNumber} از ${allChapters.size}",
                            fontFamily = fontFam,
                            fontSize = 12.sp,
                            color = activeTheme.textColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Navigation buttons: Previous & Next Chapter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isFirstChapter = chapter.chapterNumber <= 1
                        val isLastChapter = chapter.chapterNumber >= allChapters.size

                        Button(
                            onClick = { viewModel.navigateToPreviousChapter(novel.id, chapter.id) },
                            enabled = !isFirstChapter,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("prev_chapter_button")
                        ) {
                            Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پچھلا باب", fontFamily = fontFam, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { showChaptersSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Select Chapter",
                                tint = activeTheme.textColor
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateToNextChapter(novel.id, chapter.id) },
                            enabled = !isLastChapter,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("next_chapter_button")
                        ) {
                            Text("اگلا باب", fontFamily = fontFam, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    // Reader Appearance Settings Bottom Sheet
    if (showSettingsSheet) {
        ReaderSettingsSheet(
            settings = readerSettings,
            onSettingsChanged = { viewModel.updateReaderSettings(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }

    // Chapters Selector Bottom Sheet
    if (showChaptersSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showChaptersSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "فہرستِ ابواب — ${novel.title}",
                    fontFamily = fontFam,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(allChapters, key = { it.id }) { ch ->
                        val isCurrent = ch.id == chapter.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showChaptersSheet = false
                                    viewModel.navigateToReader(novel.id, ch.id)
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${ch.chapterNumber}. ${ch.title}",
                                    fontFamily = fontFam,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )

                                if (isCurrent) {
                                    Text(
                                        text = "پڑھا جا رہا ہے",
                                        fontFamily = fontFam,
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
