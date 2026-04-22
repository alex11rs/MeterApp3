package ru.pepega.meterapp3.theme

import android.content.SharedPreferences
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.pepega.meterapp3.R

private const val PREF_THEME_PRESET = "setting_theme_preset"

enum class ThemePreset {
    LIGHT,
    ROSE,
    MINT,
    DARK,
    SAND,
    FOREST,
    TOXIC
}

fun SharedPreferences.getThemePreset(): ThemePreset {
    val saved = getString(PREF_THEME_PRESET, null)
    if (!saved.isNullOrBlank()) {
        return ThemePreset.entries.firstOrNull { it.name == saved } ?: ThemePreset.LIGHT
    }

    return if (getBoolean("setting_dark_theme", false)) {
        ThemePreset.DARK
    } else {
        ThemePreset.LIGHT
    }
}

fun ThemePreset.labelResId(): Int = when (this) {
    ThemePreset.LIGHT -> R.string.theme_light
    ThemePreset.ROSE -> R.string.theme_rose
    ThemePreset.MINT -> R.string.theme_mint
    ThemePreset.DARK -> R.string.theme_dark
    ThemePreset.SAND -> R.string.theme_sand
    ThemePreset.FOREST -> R.string.theme_forest
    ThemePreset.TOXIC -> R.string.theme_toxic
}

@Composable
fun MeterAppTheme(
    themePreset: ThemePreset,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themePreset.toColorScheme(),
        content = content
    )
}

private fun ThemePreset.toColorScheme(): ColorScheme = when (this) {
    ThemePreset.LIGHT -> lightColorScheme(
        primary = Color(0xFF1565C0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6E8FF),
        onPrimaryContainer = Color(0xFF001C3A),
        secondary = Color(0xFF526070),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD6E4F7),
        onSecondaryContainer = Color(0xFF0F1D2A),
        tertiary = Color(0xFF00695C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFA9F2E2),
        onTertiaryContainer = Color(0xFF00201B),
        background = Color(0xFFF7F9FC),
        onBackground = Color(0xFF181C20),
        surface = Color(0xFFF7F9FC),
        onSurface = Color(0xFF181C20),
        surfaceVariant = Color(0xFFDEE3EB),
        onSurfaceVariant = Color(0xFF42474E),
        outline = Color(0xFF72777F)
    )

    ThemePreset.ROSE -> lightColorScheme(
        primary = Color(0xFFB24A6B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E3),
        onPrimaryContainer = Color(0xFF3F001D),
        secondary = Color(0xFF8A5A68),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E3),
        onSecondaryContainer = Color(0xFF381722),
        tertiary = Color(0xFF9A6040),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDCC8),
        onTertiaryContainer = Color(0xFF341100),
        background = Color(0xFFFFF7F9),
        onBackground = Color(0xFF23191D),
        surface = Color(0xFFFFF7F9),
        onSurface = Color(0xFF23191D),
        surfaceVariant = Color(0xFFF4DDE3),
        onSurfaceVariant = Color(0xFF534349),
        outline = Color(0xFF85737A)
    )

    ThemePreset.MINT -> lightColorScheme(
        primary = Color(0xFF2B7A68),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB8F2E1),
        onPrimaryContainer = Color(0xFF002019),
        secondary = Color(0xFF4C635C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCFE9DF),
        onSecondaryContainer = Color(0xFF082019),
        tertiary = Color(0xFF4D6477),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD4E8FE),
        onTertiaryContainer = Color(0xFF071E2E),
        background = Color(0xFFF4FFFB),
        onBackground = Color(0xFF16201D),
        surface = Color(0xFFF4FFFB),
        onSurface = Color(0xFF16201D),
        surfaceVariant = Color(0xFFD8E5DF),
        onSurfaceVariant = Color(0xFF3D4944),
        outline = Color(0xFF6C7872)
    )

    ThemePreset.DARK -> darkColorScheme(
        primary = Color(0xFF9FCAFF),
        onPrimary = Color(0xFF00325A),
        primaryContainer = Color(0xFF00497F),
        onPrimaryContainer = Color(0xFFD6E8FF),
        secondary = Color(0xFFBAC8DB),
        onSecondary = Color(0xFF243140),
        secondaryContainer = Color(0xFF3B4857),
        onSecondaryContainer = Color(0xFFD6E4F7),
        tertiary = Color(0xFF8DD5C6),
        onTertiary = Color(0xFF003730),
        tertiaryContainer = Color(0xFF005047),
        onTertiaryContainer = Color(0xFFA9F2E2),
        background = Color(0xFF101417),
        onBackground = Color(0xFFE0E3E7),
        surface = Color(0xFF101417),
        onSurface = Color(0xFFE0E3E7),
        surfaceVariant = Color(0xFF42474E),
        onSurfaceVariant = Color(0xFFC2C7CF),
        outline = Color(0xFF8C9198)
    )

    ThemePreset.SAND -> lightColorScheme(
        primary = Color(0xFF9C5A1A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDCC0),
        onPrimaryContainer = Color(0xFF331200),
        secondary = Color(0xFF775846),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDCC8),
        onSecondaryContainer = Color(0xFF2C1608),
        tertiary = Color(0xFF69602F),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF1E5A7),
        onTertiaryContainer = Color(0xFF211B00),
        background = Color(0xFFFFF8F2),
        onBackground = Color(0xFF231A14),
        surface = Color(0xFFFFF8F2),
        onSurface = Color(0xFF231A14),
        surfaceVariant = Color(0xFFF2DFD2),
        onSurfaceVariant = Color(0xFF54443A),
        outline = Color(0xFF867469)
    )

    ThemePreset.FOREST -> darkColorScheme(
        primary = Color(0xFF7FD08B),
        onPrimary = Color(0xFF003910),
        primaryContainer = Color(0xFF1A5125),
        onPrimaryContainer = Color(0xFF9AECA4),
        secondary = Color(0xFFB7CCB2),
        onSecondary = Color(0xFF223423),
        secondaryContainer = Color(0xFF384B39),
        onSecondaryContainer = Color(0xFFD3E8CD),
        tertiary = Color(0xFFA5D0D6),
        onTertiary = Color(0xFF04363B),
        tertiaryContainer = Color(0xFF234D52),
        onTertiaryContainer = Color(0xFFC1ECF2),
        background = Color(0xFF0E1510),
        onBackground = Color(0xFFDDE5DB),
        surface = Color(0xFF0E1510),
        onSurface = Color(0xFFDDE5DB),
        surfaceVariant = Color(0xFF404941),
        onSurfaceVariant = Color(0xFFC0C9BF),
        outline = Color(0xFF8A938A)
    )

    ThemePreset.TOXIC -> darkColorScheme(
        primary = Color(0xFFB7FF00),
        onPrimary = Color(0xFF0F1600),
        primaryContainer = Color(0xFF426B00),
        onPrimaryContainer = Color(0xFFF8FFD6),
        secondary = Color(0xFF00FFD5),
        onSecondary = Color(0xFF001A15),
        secondaryContainer = Color(0xFF005647),
        onSecondaryContainer = Color(0xFFA8FFF1),
        tertiary = Color(0xFFFF2BD6),
        onTertiary = Color(0xFF2A0024),
        tertiaryContainer = Color(0xFF7A0065),
        onTertiaryContainer = Color(0xFFFFD9F7),
        background = Color(0xFF050800),
        onBackground = Color(0xFFEAF7C7),
        surface = Color(0xFF080C00),
        onSurface = Color(0xFFEAF7C7),
        surfaceVariant = Color(0xFF446000),
        onSurfaceVariant = Color(0xFFE2F5B9),
        outline = Color(0xFF96B74B)
    )
}
