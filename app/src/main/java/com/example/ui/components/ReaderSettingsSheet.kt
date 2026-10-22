package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FontStyleType
import com.example.data.model.ReaderSettings
import com.example.data.model.ReaderTheme
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onSettingsChanged: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("reader_settings_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "قراءت کی ترتیبات (Reading Settings)",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Live preview card with selected styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = settings.theme.backgroundColor,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "یہ اردو یونیکوڈ متن کی نمائش ہے، جمیل نوری نستعلیق انداز کے ساتھ۔",
                    fontFamily = if (settings.fontStyle == FontStyleType.NASTALEEQ) FontFamily.Serif else FontFamily.Default,
                    fontSize = settings.fontSizeSp.sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                    color = settings.theme.textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Reader Color Themes
            Text(
                text = "پس منظر اور رنگ (Theme)",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReaderTheme.entries.forEach { theme ->
                    val isSelected = settings.theme == theme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSettingsChanged(settings.copy(theme = theme)) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(theme.backgroundColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ا",
                                color = theme.textColor,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = theme.titleUrdu.substringBefore(" ("),
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Font Size Slider (16sp - 34sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فونٹ سائز: ${(settings.fontSizeSp).toInt()} pt",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (settings.fontSizeSp > 16f) {
                                onSettingsChanged(settings.copy(fontSizeSp = settings.fontSizeSp - 1f))
                            }
                        }
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    IconButton(
                        onClick = {
                            if (settings.fontSizeSp < 36f) {
                                onSettingsChanged(settings.copy(fontSizeSp = settings.fontSizeSp + 1f))
                            }
                        }
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Slider(
                value = settings.fontSizeSp,
                onValueChange = { onSettingsChanged(settings.copy(fontSizeSp = it)) },
                valueRange = 16f..36f,
                steps = 19,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("font_size_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Line Spacing Multiplier (1.4x - 2.4x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سطروں کا فاصلہ (Line Height): ${"%.1f".format(settings.lineHeightMultiplier)}x",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Slider(
                value = settings.lineHeightMultiplier,
                onValueChange = { onSettingsChanged(settings.copy(lineHeightMultiplier = it)) },
                valueRange = 1.4f..2.4f,
                steps = 9,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("line_height_slider")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Font Style (Nastaleeq vs Modern Naskh)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رسم الخط (Font Style)",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.fontStyle == FontStyleType.NASTALEEQ,
                        onClick = { onSettingsChanged(settings.copy(fontStyle = FontStyleType.NASTALEEQ)) },
                        label = { Text("نستعلیق", fontFamily = FontFamily.Serif) }
                    )
                    FilterChip(
                        selected = settings.fontStyle == FontStyleType.NASKH,
                        onClick = { onSettingsChanged(settings.copy(fontStyle = FontStyleType.NASKH)) },
                        label = { Text("نسخ", fontFamily = FontFamily.Default) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Screen Brightness Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اسکرین کی چمک (Brightness)",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Slider(
                value = 0.85f,
                onValueChange = {},
                valueRange = 0.2f..1.0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("brightness_slider")
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
