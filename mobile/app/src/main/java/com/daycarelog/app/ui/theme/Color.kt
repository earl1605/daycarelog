package com.daycarelog.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand accent (teal/green) — fixed Material tonal ramp, no dynamic color.
val Green10  = Color(0xFF002204)
val Green20  = Color(0xFF00390B)
val Green30  = Color(0xFF005315)
val Green40  = Color(0xFF16a34a)
val Green80  = Color(0xFF86efac)
val Green90  = Color(0xFFbbf7d0)
val Green95  = Color(0xFFdcfce7)

// Neutral surfaces — mirrors the web app's off-white sidebar/card palette.
val OffWhite    = Color(0xFFF7F7F5)
val CardSurface = Color(0xFFFAFAFA)
val Charcoal    = Color(0xFF1F2937)
val MutedGray   = Color(0xFF6B7280)
val BorderGray  = Color(0xFFE5E7EB)
val White       = Color(0xFFFFFFFF)
val Red40       = Color(0xFFEF4444)

// Stat-card pastel accents (icon badge background + icon tint), one hue per card.
val StatGreenBg  = Color(0xFFDCFCE7)
val StatGreenFg  = Color(0xFF16A34A)
val StatBlueBg   = Color(0xFFDBEAFE)
val StatBlueFg   = Color(0xFF2563EB)
val StatVioletBg = Color(0xFFEDE9FE)
val StatVioletFg = Color(0xFF7C3AED)
val StatAmberBg  = Color(0xFFFEF3C7)
val StatAmberFg  = Color(0xFFD97706)

// Dark theme surfaces — same two-tier relationship as the light palette above
// (OffWhite/CardSurface vs White), just inverted: in a dark UI, "elevated" surfaces
// (drawer, nested cards) read best a shade LIGHTER than the base page, not darker.
val DarkBackground   = Color(0xFF111827) // darkest tier — page canvas
val DarkSurface      = Color(0xFF1F2937) // not-so-dark tier — drawer, cards, chips
val DarkOnBackground = Color(0xFFF9FAFB)
val DarkMutedGray    = Color(0xFF9CA3AF)
val DarkBorderGray   = Color(0xFF374151)
