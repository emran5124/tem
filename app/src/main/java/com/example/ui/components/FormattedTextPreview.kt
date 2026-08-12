package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TriggerEntity
import java.util.regex.Pattern

@Composable
fun FormattedTextPreview(
    text: String,
    triggers: List<TriggerEntity> = emptyList(),
    modifier: Modifier = Modifier,
    fillValues: Map<String, String> = emptyMap(),
    fontSize: Int = 16
) {
    val triggerColorMap = triggers.associate { it.tag to parseHexColor(it.colorHex) }
    val defaultHighlightColor = MaterialTheme.colorScheme.primaryContainer
    val defaultOnHighlightColor = MaterialTheme.colorScheme.onPrimaryContainer

    val annotatedString = buildAnnotatedString {
        val pattern = Pattern.compile("\\[([^\\]]+)\\]")
        val matcher = pattern.matcher(text)
        var lastIndex = 0

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val tag = matcher.group(1) ?: ""

            // Append normal text before match
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val filledValue = fillValues[tag]
            val hasFilledValue = !filledValue.isNullOrBlank()

            val badgeColor = triggerColorMap[tag] ?: defaultHighlightColor
            val isChord = tag.lowercase() == "chord" || tag.contains("آکورد")

            if (hasFilledValue) {
                // If value is filled in
                withStyle(
                    SpanStyle(
                        color = if (isChord) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        background = badgeColor.copy(alpha = 0.2f)
                    )
                ) {
                    append(filledValue)
                }
            } else {
                // Bracketed placeholder styling e.g. [word] or [chord]
                withStyle(
                    SpanStyle(
                        color = if (isChord) Color(0xFFEC4899) else defaultOnHighlightColor,
                        fontWeight = FontWeight.Bold,
                        background = badgeColor.copy(alpha = 0.25f)
                    )
                ) {
                    append("[$tag]")
                }
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.5).sp,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge.copy(
            textDirection = TextDirection.ContentOrRtl
        ),
        modifier = modifier
    )
}

fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0xFF000000)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6366F1)
    }
}
