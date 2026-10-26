package com.github.bernatbc.petitapetit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Palette.Ink,
    onPrimary = Palette.Paper,
    primaryContainer = Palette.BlueTint,
    onPrimaryContainer = Palette.OnBlueTint,
    secondary = Palette.Blue,
    onSecondary = Palette.Paper,
    secondaryContainer = Palette.Sand,
    onSecondaryContainer = Palette.Ink,
    tertiary = Palette.Blue,
    background = Palette.Paper,
    onBackground = Palette.Ink,
    surface = Palette.Paper,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.PaperSunk,
    onSurfaceVariant = Palette.InkMuted,
    surfaceContainerLowest = Palette.Paper,
    surfaceContainerLow = Palette.Paper,
    surfaceContainer = Palette.PaperSunk,
    surfaceContainerHigh = Palette.PaperSunk,
    surfaceContainerHighest = Palette.PaperSunk,
    surfaceTint = Palette.Paper,
    outline = Palette.InkMuted,
    outlineVariant = Palette.Hairline,
    error = Palette.Brick,
    inverseSurface = Palette.Ink,
    inverseOnSurface = Palette.Paper,
    inversePrimary = Palette.NightInk,
)

private val DarkColors = darkColorScheme(
    primary = Palette.NightInk,
    onPrimary = Palette.NightPaper,
    primaryContainer = Palette.NightBlueTint,
    onPrimaryContainer = Palette.NightOnBlueTint,
    secondary = Palette.NightBlue,
    onSecondary = Palette.NightPaper,
    secondaryContainer = Palette.NightSand,
    onSecondaryContainer = Palette.NightInk,
    tertiary = Palette.NightBlue,
    background = Palette.NightPaper,
    onBackground = Palette.NightInk,
    surface = Palette.NightPaper,
    onSurface = Palette.NightInk,
    surfaceVariant = Palette.NightPaperSunk,
    onSurfaceVariant = Palette.NightInkMuted,
    surfaceContainerLowest = Palette.NightPaper,
    surfaceContainerLow = Palette.NightPaper,
    surfaceContainer = Palette.NightPaperSunk,
    surfaceContainerHigh = Palette.NightPaperSunk,
    surfaceContainerHighest = Palette.NightPaperSunk,
    surfaceTint = Palette.NightPaper,
    outline = Palette.NightInkMuted,
    outlineVariant = Palette.NightHairline,
    error = Palette.NightBrick,
    inverseSurface = Palette.NightInk,
    inverseOnSurface = Palette.NightPaper,
    inversePrimary = Palette.Ink,
)

// Nearly square corners: the app reads like a printed page rather than a set of pills.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(3.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

// Dynamic colour is deliberately not used so that the palette stays the same on every device.
@Composable
fun PetitÀPetitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
