package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.TemplateEntity
import com.example.data.db.TriggerEntity
import com.example.ui.components.FormattedTextPreview
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillTemplateScreen(
    template: TemplateEntity,
    viewModel: MainViewModel,
    triggers: List<TriggerEntity>,
    onDismiss: () -> Unit,
    onSavedSuccessfully: () -> Unit
) {
    val context = LocalContext.current
    val fillValues by viewModel.fillValues.collectAsState()
    val liveText by viewModel.liveGeneratedText.collectAsState()
    val attachedImageUri by viewModel.attachedImageUri.collectAsState()
    val attachedNote by viewModel.attachedNote.collectAsState()

    val placeholders = remember(template) {
        viewModel.extractPlaceholders(template.content).distinct()
    }

    // Photo picker launcher for picking sample PNG or JPG images from gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setAttachedImageUri(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "جایگذاری متغیرها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.copyToClipboard(context, liveText, "متن جایگذاری‌شده کپی شد 📋")
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی سریع متن")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.copyToClipboard(context, liveText, "متن نهایی کپی شد 📋")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کپی متن", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.saveFilledResult(context) {
                                onSavedSuccessfully()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_filled_result_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره نتیجه", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Section 1: Live Generated Output Box with Quick Copy
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پیش‌نمایش خروجی زنده:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.copyToClipboard(context, liveText, "متن خروجی کپی شد 📋")
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کپی", fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(14.dp)
                    ) {
                        FormattedTextPreview(
                            text = template.content,
                            triggers = triggers,
                            fillValues = fillValues,
                            fontSize = 16
                        )
                    }
                }
            }

            // Section 2: Input Fields for Each Placeholder Tag detected
            Text(
                text = "مقادیر متغیرها را وارد کنید:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (placeholders.isEmpty()) {
                Text(
                    text = "هیچ متغیری مانند [word] یا [chord] در این قالب یافت نشد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    placeholders.forEach { tag ->
                        val matchedTrigger = triggers.find { it.tag == tag }
                        val labelName = matchedTrigger?.displayName ?: tag
                        val currentValue = fillValues[tag] ?: ""

                        OutlinedTextField(
                            value = currentValue,
                            onValueChange = { newValue ->
                                viewModel.updateFillValue(tag, newValue)
                            },
                            label = { Text("مقدار برای [$labelName]") },
                            placeholder = { Text("مثلاً برای $tag ...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("variable_input_$tag"),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (currentValue.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateFillValue(tag, "") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Divider()

            // Section 3: Attachment Sample Image (PNG/JPG)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پیوست تصویر نمونه (PNG / JPG):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (attachedImageUri == null) "انتخاب عکس" else "تغییر عکس", fontSize = 12.sp)
                    }
                }

                if (attachedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = attachedImageUri,
                            contentDescription = "تصویر نمونه پیوست‌شده",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { viewModel.setAttachedImageUri(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "حذف عکس", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Optional Note
            OutlinedTextField(
                value = attachedNote,
                onValueChange = { viewModel.setAttachedNote(it) },
                label = { Text("یادداشت یا توضیحات تکمیلی (اختیاری)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
