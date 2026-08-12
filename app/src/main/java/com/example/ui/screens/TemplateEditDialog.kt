package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.TemplateEntity
import com.example.data.db.TriggerEntity
import com.example.ui.components.FormattedTextPreview
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditDialog(
    template: TemplateEntity?,
    triggers: List<TriggerEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var category by remember { mutableStateOf(template?.category ?: "عمومی") }
    var contentState by remember { mutableStateOf(TextFieldValue(template?.content ?: "")) }

    val categories = listOf("عمومی", "موسیقی", "پیامک", "کسب‌ و کار", "آموزشی")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (template == null) "ایجاد قالب جدید" else "ویرایش قالب",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان قالب") },
                        placeholder = { Text("مثلاً: آکورد گیتار یا پیامک تبریک") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category Selection Chips
                    Text(
                        text = "دسته‌بندی:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Variable Trigger Quick-Insert Chips
                    Text(
                        text = "افزودن متغیر به متن (تریگرها):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(triggers) { trigger ->
                            val hex = parseHexColor(trigger.colorHex)
                            ElevatedAssistChip(
                                onClick = {
                                    val tagToInsert = "[${trigger.tag}]"
                                    val currentText = contentState.text
                                    val cursor = contentState.selection.start.coerceIn(0, currentText.length)
                                    val newText = currentText.substring(0, cursor) + tagToInsert + currentText.substring(cursor)
                                    val newCursor = cursor + tagToInsert.length
                                    contentState = TextFieldValue(
                                        text = newText,
                                        selection = androidx.compose.ui.text.TextRange(newCursor)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "+ [${trigger.displayName}]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.elevatedAssistChipColors(
                                    containerColor = hex.copy(alpha = 0.15f),
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Content Input Field
                    OutlinedTextField(
                        value = contentState,
                        onValueChange = { contentState = it },
                        label = { Text("متن قالب (شامل [word] یا [chord] و ...)") },
                        placeholder = { Text("بلا بلا بلا [word] بلا بلا [word] بلابلابلا [chord] بلا بلا") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 280.dp)
                            .testTag("template_content_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Live Highlighted Syntax Preview
                    if (contentState.text.isNotBlank()) {
                        Text(
                            text = "پیش‌نمایش زنده قالب با تریگرها:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            FormattedTextPreview(
                                text = contentState.text,
                                triggers = triggers,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && contentState.text.isNotBlank()) {
                                onSave(title, contentState.text, category)
                            }
                        },
                        enabled = title.isNotBlank() && contentState.text.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_template_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره قالب")
                    }
                }
            }
        }
    }
}
