package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.TriggerEntity
import com.example.ui.components.TriggerBadge
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggersManageDialog(
    triggers: List<TriggerEntity>,
    onDismiss: () -> Unit,
    onAddTrigger: (tag: String, displayName: String, colorHex: String) -> Unit,
    onDeleteTrigger: (TriggerEntity) -> Unit
) {
    var newTag by remember { mutableStateOf("") }
    var newDisplayName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#8B5CF6") }

    val presetColors = listOf(
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#3B82F6", // Blue
        "#10B981", // Green
        "#F59E0B", // Amber
        "#EF4444", // Red
        "#06B6D4", // Cyan
        "#84CC16"  // Lime
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        text = "مدیریت تریگرها (متغیرها)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Text(
                    text = "تریگرها کلماتی مانند [word] یا [chord] هستند که در متن قالب جایگذاری می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Form to Add New Trigger
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "افزودن تریگر جدید:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                label = { Text("نام تگ (انگلیسی)") },
                                placeholder = { Text("chord") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_trigger_tag_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = newDisplayName,
                                onValueChange = { newDisplayName = it },
                                label = { Text("عنوان فارسی") },
                                placeholder = { Text("آکورد") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_trigger_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Color Picker
                        Text(
                            text = "انتخاب رنگ نشانگر:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetColors) { hex ->
                                val color = parseHexColor(hex)
                                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (newTag.isNotBlank() && newDisplayName.isNotBlank()) {
                                    onAddTrigger(newTag, newDisplayName, selectedColorHex)
                                    newTag = ""
                                    newDisplayName = ""
                                }
                            },
                            enabled = newTag.isNotBlank() && newDisplayName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_trigger_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن تریگر")
                        }
                    }
                }

                // Existing Triggers List
                Text(
                    text = "تریگرهای موجود:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(triggers) { trigger ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TriggerBadge(trigger = trigger)

                            if (!trigger.isDefault) {
                                IconButton(onClick = { onDeleteTrigger(trigger) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف تریگر",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                Text(
                                    text = "پیش‌فرض",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
