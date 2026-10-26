package com.github.bernatbc.petitapetit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.bernatbc.petitapetit.R

// Serif for everything that is French content, sans for the interface around it.
val SourceSerif = FontFamily(
    Font(R.font.source_serif_light, FontWeight.Light),
    Font(R.font.source_serif_regular, FontWeight.Normal),
    Font(R.font.source_serif_semibold, FontWeight.SemiBold),
    Font(R.font.source_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

private val Sans = FontFamily.SansSerif

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Light, fontSize = 48.sp, lineHeight = 54.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Light, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.4).sp),
    displaySmall = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Light, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.3).sp),
    headlineLarge = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Light, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 25.sp),
    titleSmall = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontFamily = SourceSerif, fontWeight = FontWeight.Normal, fontSize = 19.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.15.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.6.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.4.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp),
)
