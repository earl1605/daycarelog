package com.daycarelog.app.util

import androidx.compose.ui.text.input.TextFieldValue

/**
 * Capitalizes the first letter of each word segment (space/hyphen/apostrophe-delimited)
 * without touching any other character — so deliberate casing like "McDonald" or "DeSilva"
 * survives untouched. Same-length output (a pure case remap, no insertions/deletions), which
 * is what makes cursor-position preservation trivial in [capitalizedNameFieldValue] below.
 *
 * Used both as a manual onValueChange transform (covering pasted text and IMEs that don't
 * respect KeyboardCapitalization.Words) and as a final pass before saving to the API.
 */
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

/**
 * onValueChange transform for name TextFields backed by [TextFieldValue] state: applies
 * [capitalizeWords] to the text while preserving the selection/cursor exactly as Compose
 * already computed it for the raw edit — valid because the transform never changes length.
 */
fun capitalizedNameFieldValue(new: TextFieldValue): TextFieldValue =
    new.copy(text = capitalizeWords(new.text))
