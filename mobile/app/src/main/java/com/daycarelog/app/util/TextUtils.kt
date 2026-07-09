package com.daycarelog.app.util

import androidx.compose.ui.text.input.TextFieldValue

fun capitalizeWords(value: String): String {
    if (value.isEmpty()) return value
    val builder = StringBuilder(value.length)
    var atSegmentStart = true
    for (char in value) {
        builder.append(if (atSegmentStart) char.uppercaseChar() else char)
        atSegmentStart = char == ' ' || char == '-' || char == '\''
    }
    return builder.toString()
}

fun capitalizedNameFieldValue(new: TextFieldValue): TextFieldValue =
    new.copy(text = capitalizeWords(new.text))
