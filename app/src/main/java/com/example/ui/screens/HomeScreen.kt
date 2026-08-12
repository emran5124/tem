package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FilledResultEntity
import com.example.data.db.TemplateEntity
import com.example.ui.components.FilledResultCard
import com.example.ui.components.TemplateCard
import com.example.ui.components.TriggerBadge
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val triggers by viewModel.triggers.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val filledResults by viewModel.filledResults.collectAsState()

    val selectedTemplateForFill by viewModel.selectedTemplateForFill.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Templates, 1: Saved Results
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("همه") }

    var showEditDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<TemplateEntity?>(null) }
    var showTriggersDialog by remember { mutableStateOf(false) }
    var selectedResultForDetail by remember { mutableStateOf<FilledResultEntity?>(null) }

    val categories = listOf("همه", "عمومی", "موسیقی", "پیامک", "کسب‌ و کار", "آموزشی")

    // Filter templates based on category & search
    val filteredTemplates = remember(templates, searchQuery, selectedCategory) {
        templates.filter { t ->
            val matchesCategory = selectedCategory == "همه" || t.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    t.title.contains(searchQuery, ignoreCase = true) ||
                    t.content.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    // Filter results based on search
    val filteredResults = remember(filledResults, searchQuery) {
        filledResults.filter { r ->
            searchQuery.isBlank() ||
                    r.templateTitle.contains(searchQuery, ignoreCase = true) ||
                    r.filledText.contains(searchQuery, ignoreCase = true) ||
                    (r.note ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedTemplateForFill != null) {
        FillTemplateScreen(
            template = selectedTemplateForFill!!,
            viewModel = viewModel,
            triggers = triggers,
            onDismiss = { viewModel.clearFillState() },
            onSavedSuccessfully = {
                viewModel.clearFillState()
                selectedTabIndex = 1 // Switch to saved results tab to see saved result!
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(22.dp)
                            )
                        }
                        Text(
                            text = "قالب متن",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Trigger Manager Button
                    IconButton(
                        onClick = { showTriggersDialog = true },
                        modifier = Modifier.testTag("open_triggers_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = "مدیریت تریگرها",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        templateToEdit = null
                        showEditDialog = true
                    },
                    modifier = Modifier.testTag("add_template_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("قالب جدید", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row (قالب‌ها / نتایج ذخیره‌شده)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "قالب‌ها (${templates.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "نتایج جایگذاری شده (${filledResults.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null) }
                )
            }

            // Search Bar & Filter Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (selectedTabIndex == 0) "جستجو در عنوان و متن قالب..." else "جستجو در نتایج ذخیره‌شده...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                if (selectedTabIndex == 0) {
                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Available Triggers Horizontal Ribbon
                    if (triggers.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "تریگرهای فعال:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(triggers) { trigger ->
                                    TriggerBadge(trigger = trigger)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Main Content Area according to selected tab
            Crossfade(targetState = selectedTabIndex, label = "TabCrossfade") { tabIndex ->
                if (tabIndex == 0) {
                    // Tab 0: Templates List
                    if (filteredTemplates.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FindInPage,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "هیچ قالبی با این مشخصات پیدا نشد." else "هنوز قالبی ثبت نکرده‌اید.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        templateToEdit = null
                                        showEditDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ایجاد اولین قالب")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredTemplates, key = { it.id }) { template ->
                                val placeholders = remember(template.content) {
                                    viewModel.extractPlaceholders(template.content)
                                }

                                TemplateCard(
                                    template = template,
                                    triggers = triggers,
                                    placeholders = placeholders,
                                    onFillClick = {
                                        viewModel.selectTemplateForFill(template)
                                    },
                                    onEditClick = {
                                        templateToEdit = template
                                        showEditDialog = true
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteTemplate(template)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Tab 1: Saved Filled Results List
                    if (filteredResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "نتیجه‌ای یافت نشد." else "هنوز متن جایگذاری‌شده‌ای ذخیره نکرده‌اید.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredResults, key = { it.id }) { result ->
                                FilledResultCard(
                                    result = result,
                                    onCopyClick = {
                                        viewModel.copyToClipboard(context, result.filledText)
                                    },
                                    onShareClick = {
                                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, result.filledText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(android.content.Intent.createChooser(sendIntent, "اشتراک‌گذاری"))
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteFilledResult(result)
                                    },
                                    onAddImageClick = {
                                        selectedResultForDetail = result
                                    },
                                    onCardClick = {
                                        selectedResultForDetail = result
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showEditDialog) {
        TemplateEditDialog(
            template = templateToEdit,
            triggers = triggers,
            onDismiss = { showEditDialog = false },
            onSave = { title, content, category ->
                viewModel.saveTemplate(
                    id = templateToEdit?.id ?: 0,
                    title = title,
                    content = content,
                    category = category
                )
                showEditDialog = false
            }
        )
    }

    if (showTriggersDialog) {
        TriggersManageDialog(
            triggers = triggers,
            onDismiss = { showTriggersDialog = false },
            onAddTrigger = { tag, name, hex ->
                viewModel.addTrigger(tag, name, hex)
            },
            onDeleteTrigger = { trigger ->
                viewModel.deleteTrigger(trigger)
            }
        )
    }

    if (selectedResultForDetail != null) {
        FilledResultDetailDialog(
            result = selectedResultForDetail!!,
            onDismiss = { selectedResultForDetail = null },
            onCopyClick = {
                viewModel.copyToClipboard(context, selectedResultForDetail!!.filledText)
            },
            onDeleteClick = {
                viewModel.deleteFilledResult(selectedResultForDetail!!)
                selectedResultForDetail = null
            },
            onUpdateImageUri = { newUri ->
                viewModel.updateResultImage(selectedResultForDetail!!, newUri)
                selectedResultForDetail = selectedResultForDetail!!.copy(imageUri = newUri.toString())
            }
        )
    }
}
