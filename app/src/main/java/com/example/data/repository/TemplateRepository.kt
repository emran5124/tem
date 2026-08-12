package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

class TemplateRepository(
    private val context: Context,
    private val triggerDao: TriggerDao,
    private val templateDao: TemplateDao,
    private val filledResultDao: FilledResultDao
) {
    val allTriggers: Flow<List<TriggerEntity>> = triggerDao.getAllTriggers()
    val allTemplates: Flow<List<TemplateEntity>> = templateDao.getAllTemplates()
    val allFilledResults: Flow<List<FilledResultEntity>> = filledResultDao.getAllFilledResults()

    suspend fun insertTrigger(trigger: TriggerEntity) = triggerDao.insertTrigger(trigger)
    suspend fun deleteTrigger(trigger: TriggerEntity) = triggerDao.deleteTrigger(trigger)

    suspend fun insertTemplate(template: TemplateEntity) = templateDao.insertTemplate(template)
    suspend fun updateTemplate(template: TemplateEntity) = templateDao.updateTemplate(template)
    suspend fun deleteTemplate(template: TemplateEntity) = templateDao.deleteTemplate(template)

    suspend fun insertFilledResult(result: FilledResultEntity) = filledResultDao.insertFilledResult(result)
    suspend fun updateFilledResult(result: FilledResultEntity) = filledResultDao.updateFilledResult(result)
    suspend fun deleteFilledResult(result: FilledResultEntity) = filledResultDao.deleteFilledResult(result)

    /**
     * Extracts all bracketed placeholder tags like [word], [chord] in exact sequence without brackets.
     */
    fun extractPlaceholders(text: String): List<String> {
        val pattern = Pattern.compile("\\[([^\\]]+)\\]")
        val matcher = pattern.matcher(text)
        val placeholders = mutableListOf<String>()
        while (matcher.find()) {
            val tag = matcher.group(1)?.trim() ?: ""
            if (tag.isNotEmpty()) {
                placeholders.add(tag)
            }
        }
        return placeholders
    }

    /**
     * Substitutes [tag] in templateText with values provided in valuesMap.
     */
    fun substituteText(templateText: String, valuesMap: Map<String, String>): String {
        var result = templateText
        valuesMap.forEach { (tag, value) ->
            if (value.isNotBlank()) {
                result = result.replace("[$tag]", value)
            }
        }
        return result
    }

    /**
     * Saves image URI locally to internal storage so the image sample is preserved offline permanently.
     */
    suspend fun saveImageToInternalStorage(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val imagesDir = File(context.filesDir, "saved_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val fileName = "sample_${System.currentTimeMillis()}.jpg"
            val destFile = File(imagesDir, fileName)
            val outputStream = FileOutputStream(destFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
