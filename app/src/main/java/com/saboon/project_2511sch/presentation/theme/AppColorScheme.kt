package com.saboon.project_2511sch.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The Material 3 color roles that the app's XML themes define (`themes*.xml`, `colors*.xml`).
 *
 * Presentation layer, used by [AppTheme]: during the XML -> Compose migration the XML themes stay
 * the single source of truth for colors. [readThemeColors] reads each role from the current
 * Activity theme and [appColorScheme] turns the result into a Compose [ColorScheme], so a Compose
 * screen always looks like the XML screens around it, in every color theme and in light / dark mode.
 */
enum class AppColorRole {
    PRIMARY, ON_PRIMARY, PRIMARY_CONTAINER, ON_PRIMARY_CONTAINER, INVERSE_PRIMARY,
    SECONDARY, ON_SECONDARY, SECONDARY_CONTAINER, ON_SECONDARY_CONTAINER,
    TERTIARY, ON_TERTIARY, TERTIARY_CONTAINER, ON_TERTIARY_CONTAINER,
    ERROR, ON_ERROR, ERROR_CONTAINER, ON_ERROR_CONTAINER,
    BACKGROUND, ON_BACKGROUND,
    SURFACE, ON_SURFACE, SURFACE_VARIANT, ON_SURFACE_VARIANT,
    INVERSE_SURFACE, INVERSE_ON_SURFACE,
    OUTLINE, OUTLINE_VARIANT,
    SURFACE_DIM, SURFACE_BRIGHT,
    SURFACE_CONTAINER_LOWEST, SURFACE_CONTAINER_LOW, SURFACE_CONTAINER,
    SURFACE_CONTAINER_HIGH, SURFACE_CONTAINER_HIGHEST,
}

/**
 * Builds a Compose [ColorScheme] from the ARGB colors read from an XML theme.
 *
 * Every role missing from [colors] keeps the Material 3 default of [lightColorScheme] or
 * [darkColorScheme], so a theme that sets only a few colors still gives a complete scheme.
 * `surfaceTint` follows `primary`, as in the XML Material 3 themes.
 *
 * @param isDark Whether the device is in dark mode; picks the default scheme for missing roles.
 * @param colors ARGB color for each role the theme defines.
 * @return The color scheme to pass to `MaterialTheme`.
 */
fun appColorScheme(isDark: Boolean, colors: Map<AppColorRole, Int>): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    fun color(role: AppColorRole, default: Color): Color = colors[role]?.let { Color(it) } ?: default

    val primary = color(AppColorRole.PRIMARY, base.primary)
    return base.copy(
        primary = primary,
        onPrimary = color(AppColorRole.ON_PRIMARY, base.onPrimary),
        primaryContainer = color(AppColorRole.PRIMARY_CONTAINER, base.primaryContainer),
        onPrimaryContainer = color(AppColorRole.ON_PRIMARY_CONTAINER, base.onPrimaryContainer),
        inversePrimary = color(AppColorRole.INVERSE_PRIMARY, base.inversePrimary),
        secondary = color(AppColorRole.SECONDARY, base.secondary),
        onSecondary = color(AppColorRole.ON_SECONDARY, base.onSecondary),
        secondaryContainer = color(AppColorRole.SECONDARY_CONTAINER, base.secondaryContainer),
        onSecondaryContainer = color(AppColorRole.ON_SECONDARY_CONTAINER, base.onSecondaryContainer),
        tertiary = color(AppColorRole.TERTIARY, base.tertiary),
        onTertiary = color(AppColorRole.ON_TERTIARY, base.onTertiary),
        tertiaryContainer = color(AppColorRole.TERTIARY_CONTAINER, base.tertiaryContainer),
        onTertiaryContainer = color(AppColorRole.ON_TERTIARY_CONTAINER, base.onTertiaryContainer),
        error = color(AppColorRole.ERROR, base.error),
        onError = color(AppColorRole.ON_ERROR, base.onError),
        errorContainer = color(AppColorRole.ERROR_CONTAINER, base.errorContainer),
        onErrorContainer = color(AppColorRole.ON_ERROR_CONTAINER, base.onErrorContainer),
        background = color(AppColorRole.BACKGROUND, base.background),
        onBackground = color(AppColorRole.ON_BACKGROUND, base.onBackground),
        surface = color(AppColorRole.SURFACE, base.surface),
        onSurface = color(AppColorRole.ON_SURFACE, base.onSurface),
        surfaceVariant = color(AppColorRole.SURFACE_VARIANT, base.surfaceVariant),
        onSurfaceVariant = color(AppColorRole.ON_SURFACE_VARIANT, base.onSurfaceVariant),
        surfaceTint = primary,
        inverseSurface = color(AppColorRole.INVERSE_SURFACE, base.inverseSurface),
        inverseOnSurface = color(AppColorRole.INVERSE_ON_SURFACE, base.inverseOnSurface),
        outline = color(AppColorRole.OUTLINE, base.outline),
        outlineVariant = color(AppColorRole.OUTLINE_VARIANT, base.outlineVariant),
        surfaceDim = color(AppColorRole.SURFACE_DIM, base.surfaceDim),
        surfaceBright = color(AppColorRole.SURFACE_BRIGHT, base.surfaceBright),
        surfaceContainerLowest = color(AppColorRole.SURFACE_CONTAINER_LOWEST, base.surfaceContainerLowest),
        surfaceContainerLow = color(AppColorRole.SURFACE_CONTAINER_LOW, base.surfaceContainerLow),
        surfaceContainer = color(AppColorRole.SURFACE_CONTAINER, base.surfaceContainer),
        surfaceContainerHigh = color(AppColorRole.SURFACE_CONTAINER_HIGH, base.surfaceContainerHigh),
        surfaceContainerHighest = color(AppColorRole.SURFACE_CONTAINER_HIGHEST, base.surfaceContainerHighest),
    )
}
