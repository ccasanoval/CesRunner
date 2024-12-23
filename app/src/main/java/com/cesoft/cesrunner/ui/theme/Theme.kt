package com.cesoft.cesrunner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Green, //Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    onSurface = Green,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

//https://stackoverflow.com/questions/78526697/how-to-change-the-default-color-of-button-in-jetpack-compose
//    background = Green,
//    onBackground = Green,
//    surface = Green,
//    onSurface = Green,

//
//    primaryContainer = Green,
//    onPrimaryContainer = Green,
//    onPrimary = Green,
//
//    secondaryContainer = Green,
//    onSecondaryContainer = Green,
//    onSecondary = Green,
//
//    tertiaryContainer = Green,
//    onTertiaryContainer = Green,
//    onTertiary = Green,
//
//    onSurface = Green,
//    background = Green,
//    onBackground = Green

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CesRunnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}