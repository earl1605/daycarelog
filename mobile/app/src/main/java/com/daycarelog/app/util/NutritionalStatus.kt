package com.daycarelog.app.util

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class NutritionalStatusResult(val label: String, val color: String)

/** Background/foreground pair for a status "color" key — mirrors the web app's NutritionalStatusBadge color map. */
fun nutritionalStatusColors(color: String): Pair<Color, Color> = when (color) {
    "green"  -> Color(0xFFDCFCE7) to Color(0xFF166534)
    "yellow" -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
    "orange" -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
    "red"    -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
    else     -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
}

/** Whole calendar months between dateOfBirth and today, ignoring day-of-month — mirrors the web app's getAgeInMonths() exactly. */
fun getAgeInMonths(dateOfBirth: String): Long {
    val birth = LocalDate.parse(dateOfBirth)
    val now = LocalDate.now()
    return (now.year - birth.year) * 12L + (now.monthValue - birth.monthValue)
}

fun formatAge(dateOfBirth: String): String {
    val months = getAgeInMonths(dateOfBirth)
    if (months < 24) return "$months month${if (months != 1L) "s" else ""}"
    val years = months / 12
    val rem = months % 12
    return if (rem == 0L) "$years yr${if (years != 1L) "s" else ""}"
    else "$years yr${if (years != 1L) "s" else ""} $rem mo"
}

// DOH weight-for-age classification (simplified, 0-5 years) — WHO median weight-for-age
// table, ported verbatim from web/src/utils/nutritionalStatus.js to keep both clients
// classifying the same record identically.
private val MEDIAN_MALE = doubleArrayOf(
    3.3, 4.5, 5.6, 6.4, 7.0, 7.5, 7.9, 8.3, 8.6, 8.9, 9.2, 9.4, 9.6, 10.0, 10.3, 10.6, 10.9,
    11.1, 11.4, 11.6, 11.8, 12.0, 12.2, 12.4, 12.6, 12.8, 13.0, 13.2, 13.4, 13.6, 13.8, 14.0,
    14.2, 14.4, 14.6, 14.8, 14.9, 15.1, 15.3, 15.5, 15.7, 15.9, 16.1, 16.2, 16.4, 16.6, 16.8,
    17.0, 17.2, 17.4, 17.5, 17.7, 17.9, 18.1, 18.3, 18.5, 18.7, 18.9, 19.1, 19.3, 19.5,
)
private val MEDIAN_FEMALE = doubleArrayOf(
    3.2, 4.2, 5.1, 5.8, 6.4, 6.9, 7.3, 7.6, 7.9, 8.2, 8.5, 8.7, 8.9, 9.2, 9.5, 9.8, 10.0,
    10.2, 10.5, 10.7, 10.9, 11.1, 11.3, 11.5, 11.7, 11.9, 12.1, 12.3, 12.5, 12.7, 12.9, 13.1,
    13.3, 13.5, 13.7, 13.9, 14.1, 14.2, 14.4, 14.6, 14.8, 15.0, 15.1, 15.3, 15.5, 15.7, 15.9,
    16.1, 16.2, 16.4, 16.6, 16.8, 17.0, 17.2, 17.4, 17.6, 17.8, 18.0, 18.2, 18.4, 18.6,
)

fun classifyNutritionalStatus(weightKg: Double?, dateOfBirth: String?, sex: String?): NutritionalStatusResult {
    if (weightKg == null || weightKg <= 0.0 || dateOfBirth.isNullOrBlank()) {
        return NutritionalStatusResult("Unknown", "gray")
    }
    val months = getAgeInMonths(dateOfBirth)
    if (months < 0 || months > 60) return NutritionalStatusResult("Out of Range", "gray")

    val idx = months.toInt().coerceAtMost(60)
    val table = if (sex?.lowercase() == "female") MEDIAN_FEMALE else MEDIAN_MALE
    val ratio = weightKg / table[idx]

    return when {
        ratio >= 1.20 -> NutritionalStatusResult("Overweight", "yellow")
        ratio >= 0.90 -> NutritionalStatusResult("Normal", "green")
        ratio >= 0.75 -> NutritionalStatusResult("Underweight", "orange")
        else -> NutritionalStatusResult("Severely Underweight", "red")
    }
}
