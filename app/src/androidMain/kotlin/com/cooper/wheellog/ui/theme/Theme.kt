package com.cooper.wheellog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = com.wheellog.shared.ui.Color.MD_THEME_DARK_PRIMARY.toColor(),
        onPrimary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_PRIMARY
                .toColor(), // md_theme_light_onPrimary,
        primaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_PRIMARYCONTAINER
                .toColor(), // md_theme_light_primaryContainer,
        onPrimaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONPRIMARYCONTAINER
                .toColor(), // md_theme_light_onPrimaryContainer,
        secondary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SECONDARY
                .toColor(), // md_theme_light_secondary,
        onSecondary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONSECONDARY
                .toColor(), // md_theme_light_onSecondary,
        secondaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SECONDARYCONTAINER
                .toColor(), // md_theme_light_secondaryContainer,
        onSecondaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONSECONDARYCONTAINER
                .toColor(), // md_theme_light_onSecondaryContainer,
        tertiary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_TERTIARY
                .toColor(), // md_theme_light_tertiary,
        onTertiary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONTERTIARY
                .toColor(), // md_theme_light_onTertiary,
        tertiaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_TERTIARYCONTAINER
                .toColor(), // md_theme_light_tertiaryContainer,
        onTertiaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONTERTIARYCONTAINER
                .toColor(), // md_theme_light_onTertiaryContainer,
        error =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ERROR.toColor(), // md_theme_light_error,
        errorContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ERRORCONTAINER
                .toColor(), // md_theme_light_errorContainer,
        onError =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONERROR
                .toColor(), // md_theme_light_onError,
        onErrorContainer =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONERRORCONTAINER
                .toColor(), // md_theme_light_onErrorContainer,
        background =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_BACKGROUND
                .toColor(), // md_theme_light_background,
        onBackground =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONBACKGROUND
                .toColor(), // md_theme_light_onBackground,
        surface =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SURFACE
                .toColor(), // md_theme_light_surface,
        onSurface =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONSURFACE
                .toColor(), // md_theme_light_onSurface,
        surfaceVariant =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SURFACEVARIANT
                .toColor(), // md_theme_light_surfaceVariant,
        onSurfaceVariant =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_ONSURFACEVARIANT
                .toColor(), // md_theme_light_onSurfaceVariant,
        outline =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_OUTLINE
                .toColor(), // md_theme_light_outline,
        inverseOnSurface =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_INVERSEONSURFACE
                .toColor(), // md_theme_light_inverseOnSurface,
        inverseSurface =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_INVERSESURFACE
                .toColor(), // md_theme_light_inverseSurface,
        inversePrimary =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_INVERSEPRIMARY
                .toColor(), // md_theme_light_inversePrimary,
        surfaceTint =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SURFACETINT
                .toColor(), // md_theme_light_surfaceTint,
        outlineVariant =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_OUTLINEVARIANT
                .toColor(), // md_theme_light_outlineVariant,
        scrim =
            com.wheellog.shared.ui.Color.MD_THEME_LIGHT_SCRIM.toColor(), // md_theme_light_scrim,
    )

private val DarkColors =
    darkColorScheme(
        primary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_PRIMARY.toColor(), // md_theme_dark_primary,
        onPrimary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONPRIMARY
                .toColor(), // md_theme_dark_onPrimary,
        primaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_PRIMARYCONTAINER
                .toColor(), // md_theme_dark_primaryContainer,
        onPrimaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONPRIMARYCONTAINER
                .toColor(), // md_theme_dark_onPrimaryContainer,
        secondary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_SECONDARY
                .toColor(), // md_theme_dark_secondary,
        onSecondary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONSECONDARY
                .toColor(), // md_theme_dark_onSecondary,
        secondaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_SECONDARYCONTAINER
                .toColor(), // md_theme_dark_secondaryContainer,
        onSecondaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONSECONDARYCONTAINER
                .toColor(), // md_theme_dark_onSecondaryContainer,
        tertiary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_TERTIARY
                .toColor(), // md_theme_dark_tertiary,
        onTertiary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONTERTIARY
                .toColor(), // md_theme_dark_onTertiary,
        tertiaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_TERTIARYCONTAINER
                .toColor(), // md_theme_dark_tertiaryContainer,
        onTertiaryContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONTERTIARYCONTAINER
                .toColor(), // md_theme_dark_onTertiaryContainer,
        error = com.wheellog.shared.ui.Color.MD_THEME_DARK_ERROR.toColor(), // md_theme_dark_error,
        errorContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ERRORCONTAINER
                .toColor(), // md_theme_dark_errorContainer,
        onError =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONERROR.toColor(), // md_theme_dark_onError,
        onErrorContainer =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONERRORCONTAINER
                .toColor(), // md_theme_dark_onErrorContainer,
        background =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_BACKGROUND
                .toColor(), // md_theme_dark_background,
        onBackground =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONBACKGROUND
                .toColor(), // md_theme_dark_onBackground,
        surface =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_SURFACE.toColor(), // md_theme_dark_surface,
        onSurface =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONSURFACE
                .toColor(), // md_theme_dark_onSurface,
        surfaceVariant =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_SURFACEVARIANT
                .toColor(), // md_theme_dark_surfaceVariant,
        onSurfaceVariant =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_ONSURFACEVARIANT
                .toColor(), // md_theme_dark_onSurfaceVariant,
        outline =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_OUTLINE.toColor(), // md_theme_dark_outline,
        inverseOnSurface =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_INVERSEONSURFACE
                .toColor(), // md_theme_dark_inverseOnSurface,
        inverseSurface =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_INVERSESURFACE
                .toColor(), // md_theme_dark_inverseSurface,
        inversePrimary =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_INVERSEPRIMARY
                .toColor(), // md_theme_dark_inversePrimary,
        surfaceTint =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_SURFACETINT
                .toColor(), // md_theme_dark_surfaceTint,
        outlineVariant =
            com.wheellog.shared.ui.Color.MD_THEME_DARK_OUTLINEVARIANT
                .toColor(), // md_theme_dark_outlineVariant,
        scrim = com.wheellog.shared.ui.Color.MD_THEME_DARK_SCRIM.toColor(), // md_theme_dark_scrim,
    )

@Composable
fun AppTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    MaterialTheme(colorScheme = colors, content = content)
}
