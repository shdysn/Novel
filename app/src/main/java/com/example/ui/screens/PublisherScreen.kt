package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Novel
import com.example.data.model.NovelCategory
import com.example.data.supabase.TypeScriptGuide
import com.example.ui.components.NovelCoverCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.viewmodel.NovelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherScreen(
    viewModel: NovelViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val supabaseConfig by viewModel.supabaseConfig.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val allNovels by viewModel.filteredNovels.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateNovelDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showTypeScriptDialog by remember { mutableStateOf(false) }
    var selectedNovelForChapter by remember { mutableStateOf<Novel?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("publisher_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Publisher Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ناول پبلشر اسٹوڈیو (Supabase)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "فرنٹ اینڈ پر ڈیٹا ڈسپلے کریں اور کلاؤڈ پر پش کریں",
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Supabase Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Supabase Connection Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("supabase_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (supabaseConfig.isConnected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (supabaseConfig.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Status",
                                tint = if (supabaseConfig.isConnected) EmeraldPrimary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (supabaseConfig.isConnected) "Supabase کلاؤڈ کنکشن فعال ہے" else "Supabase غیر منسلک ہے",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Text(
                        text = if (supabaseConfig.url.isNotBlank()) "پروجیکٹ URL: ${supabaseConfig.url}" else "Supabase URL کنفیگر کریں تاکہ آپ کا ڈیٹا کلاؤڈ پر محفوظ ہو سکے",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "حیثیت: ${supabaseConfig.lastSyncMessage}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = if (supabaseConfig.isConnected) EmeraldPrimary else MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testSupabaseConnection() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کنکشن ٹیسٹ", fontFamily = FontFamily.Serif, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.syncFromSupabase() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کلاؤڈ سے سنک", fontFamily = FontFamily.Serif, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Action Buttons Row (Create Novel, Add Chapter, TypeScript Guide)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "اشاعت اور ڈویلپر ٹولز",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCreateNovelDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("publish_new_novel_button")
                    ) {
                        Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نیا ناول لکھیں", fontFamily = FontFamily.Serif, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            selectedNovelForChapter = allNovels.firstOrNull()
                            showAddChapterDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_chapter_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نیا باب شامل کریں", fontFamily = FontFamily.Serif, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TypeScript scripts guide button
                OutlinedButton(
                    onClick = { showTypeScriptDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_typescript_code_button")
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ٹائپ اسکرپٹ (TypeScript) کوڈ و پش اسکرپٹ دیکھیں",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Section: Novels Ready for Push or Sync
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دستیاب ناولز و کلاؤڈ پش (${allNovels.size})",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        items(allNovels, key = { it.id }) { novel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = novel.title,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "مصنف: ${novel.author}  •  ${novel.totalChapters} ابواب",
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { viewModel.pushNovelToCloud(novel.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("push_novel_${novel.id}")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کلاؤڈ پش", fontFamily = FontFamily.Serif, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Supabase Settings Dialog
    if (showConfigDialog) {
        var tempUrl by remember { mutableStateOf(supabaseConfig.url) }
        var tempKey by remember { mutableStateOf(supabaseConfig.anonKey) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    text = "Supabase کی ترتیبات درج کریں",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "اپنے Supabase پراجیکٹ کا URL اور anon/public یا service_role key درج کریں۔",
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Supabase URL (e.g. https://xyz.supabase.co)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("Supabase Anon Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSupabaseSettings(tempUrl, tempKey)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("محفوظ کریں", fontFamily = FontFamily.Serif)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("منسوخ", fontFamily = FontFamily.Serif)
                }
            }
        )
    }

    // Create New Novel Dialog
    if (showCreateNovelDialog) {
        CreateNovelDialog(
            onDismiss = { showCreateNovelDialog = false },
            onPublish = { title, author, category, synopsis, chTitle, chContent ->
                viewModel.publishNewNovel(
                    title = title,
                    author = author,
                    category = category,
                    synopsis = synopsis,
                    initialChapterTitle = chTitle,
                    initialChapterContent = chContent,
                    onSuccess = { showCreateNovelDialog = false }
                )
            }
        )
    }

    // Add Chapter Dialog
    if (showAddChapterDialog && allNovels.isNotEmpty()) {
        AddChapterDialog(
            novels = allNovels,
            initialSelectedNovel = selectedNovelForChapter ?: allNovels.first(),
            onDismiss = { showAddChapterDialog = false },
            onAdd = { novelId, chTitle, content ->
                viewModel.addNewChapter(
                    novelId = novelId,
                    chapterTitle = chTitle,
                    content = content,
                    onSuccess = { showAddChapterDialog = false }
                )
            }
        )
    }

    // TypeScript Scripts & SQL Guide Dialog
    if (showTypeScriptDialog) {
        TypeScriptGuideDialog(
            onDismiss = { showTypeScriptDialog = false },
            onCopyCode = { code, title ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(title, code)
                clipboard.setPrimaryClip(clip)
                viewModel.showMessage("$title کلپ بورڈ پر کاپی ہو گیا!")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNovelDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("سماجی و اصلاحی") }
    var synopsis by remember { mutableStateOf("") }
    var chTitle by remember { mutableStateOf("باب اول") }
    var chContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "نیا اردو ناول تحریر و شائع کریں",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ناول کا اردو عنوان *", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("مصنف کا نام *", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("صنف (رومانوی، سماجی، روحانی، تاریخی)", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("ناول کا مختصر خلاصہ", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "پہلا باب (First Chapter):",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = chTitle,
                    onValueChange = { chTitle = it },
                    label = { Text("باب کا عنوان", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chContent,
                    onValueChange = { chContent = it },
                    label = { Text("باب کا یونیکوڈ اردو متن *", fontFamily = FontFamily.Serif) },
                    placeholder = { Text("یہاں مکمل اردو متن پیسٹ یا ٹائپ کریں...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 7
                )
                Text(
                    text = "الفاظ کی تعداد: ${if (chContent.isBlank()) 0 else chContent.trim().split(Regex("\\s+")).size}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublish(title, author, category, synopsis, chTitle, chContent) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("شائع کریں", fontFamily = FontFamily.Serif)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ", fontFamily = FontFamily.Serif)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChapterDialog(
    novels: List<Novel>,
    initialSelectedNovel: Novel,
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var selectedNovel by remember { mutableStateOf(initialSelectedNovel) }
    var chapterTitle by remember { mutableStateOf("") }
    var chapterContent by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ناول میں نیا باب شامل کریں",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "منتخب ناول:",
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedNovel.title,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        novels.forEach { novel ->
                            DropdownMenuItem(
                                text = { Text(novel.title, fontFamily = FontFamily.Serif) },
                                onClick = {
                                    selectedNovel = novel
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("باب کا عنوان (مثلاً باب دوم)", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = chapterContent,
                    onValueChange = { chapterContent = it },
                    label = { Text("باب کا مکمل اردو متن *", fontFamily = FontFamily.Serif) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 8
                )
                Text(
                    text = "الفاظ: ${if (chapterContent.isBlank()) 0 else chapterContent.trim().split(Regex("\\s+")).size}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedNovel.id, chapterTitle, chapterContent) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("شامل کریں", fontFamily = FontFamily.Serif)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ", fontFamily = FontFamily.Serif)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeScriptGuideDialog(
    onDismiss: () -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    var selectedCodeTab by remember { mutableStateOf(0) } // 0: Push Script, 1: Fetch Script, 2: SQL Schema

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ٹائپ اسکرپٹ (TypeScript) گائیڈ",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedCodeTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedCodeTab == 0,
                        onClick = { selectedCodeTab = 0 },
                        text = { Text("Push Script", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedCodeTab == 1,
                        onClick = { selectedCodeTab = 1 },
                        text = { Text("Fetch Script", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedCodeTab == 2,
                        onClick = { selectedCodeTab = 2 },
                        text = { Text("SQL Schema", fontSize = 11.sp) }
                    )
                }

                val currentCode = when (selectedCodeTab) {
                    0 -> TypeScriptGuide.TYPESCRIPT_PUBLISH_SCRIPT
                    1 -> TypeScriptGuide.TYPESCRIPT_FETCH_SCRIPT
                    else -> TypeScriptGuide.SUPABASE_SQL_SCHEMA
                }

                val codeTitle = when (selectedCodeTab) {
                    0 -> "TypeScript Push Script"
                    1 -> "TypeScript Fetch Script"
                    else -> "Supabase SQL Schema"
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onCopyCode(currentCode, codeTitle) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کاپی کریں", fontFamily = FontFamily.Serif, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = currentCode,
                        color = Color(0xFFD4D4D4),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("بند کریں", fontFamily = FontFamily.Serif)
            }
        }
    )
}
