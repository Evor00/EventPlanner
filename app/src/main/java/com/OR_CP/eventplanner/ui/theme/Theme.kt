package com.OR_CP.eventplanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Turquesa,
    onPrimary = Color.White,

    secondary = Coral,
    onSecondary = Color.White,

    background = GrisClaro,
    surface = Color.White,
    onSurface = TextoOscuro
)

@Composable
fun EventPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
