package com.saboon.project_2511sch.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestAppColorScheme {

    // Coffee theme, light mode (values/colors_coffee.xml)
    private val coffeePrimary = 0xFF571300.toInt()
    private val coffeeOnPrimary = 0xFFFFFFFF.toInt()
    private val coffeeTertiaryContainer = 0xFFBEAA61.toInt()
    private val coffeeBackground = 0xFFFFF8F6.toInt()

    @Test
    fun `colors read from the theme are used for their roles`() {
        // GIVEN
        val colors = mapOf(
            AppColorRole.PRIMARY to coffeePrimary,
            AppColorRole.ON_PRIMARY to coffeeOnPrimary,
            AppColorRole.TERTIARY_CONTAINER to coffeeTertiaryContainer,
            AppColorRole.BACKGROUND to coffeeBackground,
        )

        // WHEN
        val scheme = appColorScheme(isDark = false, colors = colors)

        // THEN
        assertThat(scheme.primary).isEqualTo(Color(coffeePrimary))
        assertThat(scheme.onPrimary).isEqualTo(Color(coffeeOnPrimary))
        assertThat(scheme.tertiaryContainer).isEqualTo(Color(coffeeTertiaryContainer))
        assertThat(scheme.background).isEqualTo(Color(coffeeBackground))
    }

    @Test
    fun `every role is mapped to its own color`() {
        // GIVEN a distinct color for every role
        val colors = AppColorRole.entries.associateWith { 0xFF000000.toInt() or it.ordinal }

        // WHEN
        val scheme = appColorScheme(isDark = false, colors = colors)

        // THEN
        assertThat(scheme.primary).isEqualTo(Color(colors.getValue(AppColorRole.PRIMARY)))
        assertThat(scheme.inversePrimary).isEqualTo(Color(colors.getValue(AppColorRole.INVERSE_PRIMARY)))
        assertThat(scheme.onSecondaryContainer).isEqualTo(Color(colors.getValue(AppColorRole.ON_SECONDARY_CONTAINER)))
        assertThat(scheme.onErrorContainer).isEqualTo(Color(colors.getValue(AppColorRole.ON_ERROR_CONTAINER)))
        assertThat(scheme.onSurfaceVariant).isEqualTo(Color(colors.getValue(AppColorRole.ON_SURFACE_VARIANT)))
        assertThat(scheme.inverseOnSurface).isEqualTo(Color(colors.getValue(AppColorRole.INVERSE_ON_SURFACE)))
        assertThat(scheme.outlineVariant).isEqualTo(Color(colors.getValue(AppColorRole.OUTLINE_VARIANT)))
        assertThat(scheme.surfaceDim).isEqualTo(Color(colors.getValue(AppColorRole.SURFACE_DIM)))
        assertThat(scheme.surfaceContainerLowest).isEqualTo(Color(colors.getValue(AppColorRole.SURFACE_CONTAINER_LOWEST)))
        assertThat(scheme.surfaceContainerHighest).isEqualTo(Color(colors.getValue(AppColorRole.SURFACE_CONTAINER_HIGHEST)))
    }

    @Test
    fun `surface tint follows primary`() {
        // WHEN
        val scheme = appColorScheme(isDark = true, colors = mapOf(AppColorRole.PRIMARY to coffeePrimary))

        // THEN
        assertThat(scheme.surfaceTint).isEqualTo(Color(coffeePrimary))
    }

    @Test
    fun `missing roles keep the light Material defaults in light mode`() {
        // WHEN
        val scheme = appColorScheme(isDark = false, colors = mapOf(AppColorRole.PRIMARY to coffeePrimary))

        // THEN
        val defaults = lightColorScheme()
        assertThat(scheme.secondary).isEqualTo(defaults.secondary)
        assertThat(scheme.surface).isEqualTo(defaults.surface)
        assertThat(scheme.surfaceContainer).isEqualTo(defaults.surfaceContainer)
    }

    @Test
    fun `missing roles keep the dark Material defaults in dark mode`() {
        // WHEN
        val scheme = appColorScheme(isDark = true, colors = emptyMap())

        // THEN
        val defaults = darkColorScheme()
        assertThat(scheme.primary).isEqualTo(defaults.primary)
        assertThat(scheme.background).isEqualTo(defaults.background)
        assertThat(scheme.onSurface).isEqualTo(defaults.onSurface)
    }
}
