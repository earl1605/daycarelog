package com.daycarelog.app.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Inserts '/' at positions 2 and 4 so raw digits MMDDYYYY display as MM/DD/YYYY. */
private object DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('/')
                append(c)
            }
        }
        val outLen = out.length
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = when {
                offset <= 2 -> offset
                offset <= 4 -> offset + 1
                else        -> offset + 2
            }.coerceAtMost(outLen)

            override fun transformedToOriginal(offset: Int) = when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                else        -> offset - 2
            }.coerceAtLeast(0)
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

/** "MMDDYYYY" → "YYYY-MM-DD" for backend submission. */
fun digitsToIso(digits: String): String {
    if (digits.length < 8) return digits
    return "${digits.substring(4, 8)}-${digits.substring(0, 2)}-${digits.substring(2, 4)}"
}

/** "YYYY-MM-DD" → "MMDDYYYY" raw digits for the field. */
fun isoToDigits(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[1]}${parts[2]}${parts[0]}" else ""
}

@Composable
fun DateField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors,
) {
    OutlinedTextField(
        value = digits,
        onValueChange = { new ->
            onDigitsChange(new.filter { it.isDigit() }.take(8))
        },
        label = { Text(label) },
        placeholder = { Text("MM/DD/YYYY", color = Color.LightGray) },
        singleLine = true,
        visualTransformation = DateTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(12.dp),
    )
}
