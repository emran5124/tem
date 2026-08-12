package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.FilledResultEntity
import com.example.data.db.TemplateEntity
import com.example.data.db.TriggerEntity
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(private val repository: TemplateRepository) : ViewModel() {

    val triggers: StateFlow<List<TriggerEntity>> = repository.allTriggers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<TemplateEntity>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filledResults: StateFlow<List<FilledResultEntity>> = repository.allFilledResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently active template for filling variables
    private val _selectedTemplateForFill = MutableStateFlow<TemplateEntity?>(null)
    val selectedTemplateForFill: StateFlow<TemplateEntity?> = _selectedTemplateForFill.asStateFlow()

    // Map holding user inputs for each placeholder variable tag
    private val _fillValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val fillValues: StateFlow<Map<String, String>> = _fillValues.asStateFlow()

    // Attached image Uri for the filled text sample result
    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    // Optional user note for the filled result
    private val _attachedNote = MutableStateFlow("")
    val attachedNote: StateFlow<String> = _attachedNote.asStateFlow()

    // Filter/Search queries
    val templateSearchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("همه")
    val resultSearchQuery = MutableStateFlow("")

    // Computed live substituted text
    val liveGeneratedText: StateFlow<String> = combine(
        _selectedTemplateForFill,
        _fillValues
    ) { template, values ->
        if (template == null) ""
        else repository.substituteText(template.content, values)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun selectTemplateForFill(template: TemplateEntity) {
        _selectedTemplateForFill.value = template
        val placeholders = repository.extractPlaceholders(template.content)
        val initialValues = placeholders.distinct().associateWith { "" }
        _fillValues.value = initialValues
        _attachedImageUri.value = null
        _attachedNote.value = ""
    }

    fun updateFillValue(tag: String, value: String) {
        val current = _fillValues.value.toMutableMap()
        current[tag] = value
        _fillValues.value = current
    }

    fun setAttachedImageUri(uri: Uri?) {
        _attachedImageUri.value = uri
    }

    fun setAttachedNote(note: String) {
        _attachedNote.value = note
    }

    fun clearFillState() {
        _selectedTemplateForFill.value = null
        _fillValues.value = emptyMap()
        _attachedImageUri.value = null
        _attachedNote.value = ""
    }

    fun saveFilledResult(context: Context, onSuccess: (FilledResultEntity) -> Unit) {
        val template = _selectedTemplateForFill.value ?: return
        val finalResultText = liveGeneratedText.value
        val imageUri = _attachedImageUri.value
        val note = _attachedNote.value.trim()

        viewModelScope.launch {
            var localImagePath: String? = null
            if (imageUri != null) {
                localImagePath = repository.saveImageToInternalStorage(imageUri) ?: imageUri.toString()
            }

            val jsonMap = JSONObject(_fillValues.value as Map<*, *>).toString()

            val entity = FilledResultEntity(
                templateId = template.id,
                templateTitle = template.title,
                filledText = finalResultText,
                valuesJson = jsonMap,
                imageUri = localImagePath,
                note = if (note.isNotBlank()) note else null,
                createdAt = System.currentTimeMillis()
            )

            val newId = repository.insertFilledResult(entity)
            Toast.makeText(context, "نتیجه با موفقیت ذخیره شد ✨", Toast.LENGTH_SHORT).show()
            onSuccess(entity.copy(id = newId.toInt()))
        }
    }

    fun copyToClipboard(context: Context, text: String, message: String = "متن در حافظه کپی شد 📋") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Filled Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun addTrigger(tag: String, displayName: String, colorHex: String) {
        viewModelScope.launch {
            val cleanTag = tag.replace("[", "").replace("]", "").trim()
            if (cleanTag.isNotBlank() && displayName.isNotBlank()) {
                repository.insertTrigger(
                    TriggerEntity(
                        tag = cleanTag,
                        displayName = displayName.trim(),
                        colorHex = colorHex
                    )
                )
            }
        }
    }

    fun deleteTrigger(trigger: TriggerEntity) {
        viewModelScope.launch {
            repository.deleteTrigger(trigger)
        }
    }

    fun saveTemplate(id: Int = 0, title: String, content: String, category: String) {
        viewModelScope.launch {
            if (title.isNotBlank() && content.isNotBlank()) {
                val entity = TemplateEntity(
                    id = id,
                    title = title.trim(),
                    content = content.trim(),
                    category = if (category.isNotBlank()) category.trim() else "عمومی",
                    createdAt = if (id != 0) System.currentTimeMillis() else System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                if (id == 0) {
                    repository.insertTemplate(entity)
                } else {
                    repository.updateTemplate(entity)
                }
            }
        }
    }

    fun deleteTemplate(template: TemplateEntity) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun deleteFilledResult(result: FilledResultEntity) {
        viewModelScope.launch {
            repository.deleteFilledResult(result)
        }
    }

    fun updateResultImage(result: FilledResultEntity, newImageUri: Uri) {
        viewModelScope.launch {
            val localPath = repository.saveImageToInternalStorage(newImageUri) ?: newImageUri.toString()
            val updated = result.copy(imageUri = localPath)
            repository.updateFilledResult(updated)
        }
    }

    fun extractPlaceholders(text: String): List<String> = repository.extractPlaceholders(text)
}

class MainViewModelFactory(private val repository: TemplateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
