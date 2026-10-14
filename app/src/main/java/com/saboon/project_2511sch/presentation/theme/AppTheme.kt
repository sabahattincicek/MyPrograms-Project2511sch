package com.saboon.project_2511sch.presentation.theme

import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import timber.log.Timber
import com.google.android.material.R as MaterialR

/**
 * Compose theme of the app (presentation layer), wrapped around every Compose screen.
 *
 * The colors are not written again in Kotlin: they are read from the XML theme of the hosting
 * Activity (the one `MainActivity` picks with `setTheme` from the user's color theme setting).
 * Light / dark mode comes for free, because the theme already resolves `values-night` colors.
 * Typography and shapes are the Material 3 defaults, which is also what the XML theme uses.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    val colorScheme = remember(context, isDark) {
        appColorScheme(isDark, context.readThemeColors())
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/** Theme attribute that holds each [AppColorRole] in the XML Material 3 themes. */
private val roleAttributes: Map<AppColorRole, Int> = mapOf(
    AppColorRole.PRIMARY to androidx.appcompat.R.attr.colorPrimary,
    AppColorRole.ON_PRIMARY to MaterialR.attr.colorOnPrimary,
    AppColorRole.PRIMARY_CONTAINER to MaterialR.attr.colorPrimaryContainer,
    AppColorRole.ON_PRIMARY_CONTAINER to MaterialR.attr.colorOnPrimaryContainer,
    AppColorRole.INVERSE_PRIMARY to MaterialR.attr.colorPrimaryInverse,
    AppColorRole.SECONDARY to MaterialR.attr.colorSecondary,
    AppColorRole.ON_SECONDARY to MaterialR.attr.colorOnSecondary,
    AppColorRole.SECONDARY_CONTAINER to MaterialR.attr.colorSecondaryContainer,
    AppColorRole.ON_SECONDARY_CONTAINER to MaterialR.attr.colorOnSecondaryContainer,
    AppColorRole.TERTIARY to MaterialR.attr.colorTertiary,
    AppColorRole.ON_TERTIARY to MaterialR.attr.colorOnTertiary,
    AppColorRole.TERTIARY_CONTAINER to MaterialR.attr.colorTertiaryContainer,
    AppColorRole.ON_TERTIARY_CONTAINER to MaterialR.attr.colorOnTertiaryContainer,
    AppColorRole.ERROR to androidx.appcompat.R.attr.colorError,
    AppColorRole.ON_ERROR to MaterialR.attr.colorOnError,
    AppColorRole.ERROR_CONTAINER to MaterialR.attr.colorErrorContainer,
    AppColorRole.ON_ERROR_CONTAINER to MaterialR.attr.colorOnErrorContainer,
    AppColorRole.BACKGROUND to android.R.attr.colorBackground,
    AppColorRole.ON_BACKGROUND to MaterialR.attr.colorOnBackground,
    AppColorRole.SURFACE to MaterialR.attr.colorSurface,
    AppColorRole.ON_SURFACE to MaterialR.attr.colorOnSurface,
    AppColorRole.SURFACE_VARIANT to MaterialR.attr.colorSurfaceVariant,
    AppColorRole.ON_SURFACE_VARIANT to MaterialR.attr.colorOnSurfaceVariant,
    AppColorRole.INVERSE_SURFACE to MaterialR.attr.colorSurfaceInverse,
    AppColorRole.INVERSE_ON_SURFACE to MaterialR.attr.colorOnSurfaceInverse,
    AppColorRole.OUTLINE to MaterialR.attr.colorOutline,
    AppColorRole.OUTLINE_VARIANT to MaterialR.attr.colorOutlineVariant,
    AppColorRole.SURFACE_DIM to MaterialR.attr.colorSurfaceDim,
    AppColorRole.SURFACE_BRIGHT to MaterialR.attr.colorSurfaceBright,
    AppColorRole.SURFACE_CONTAINER_LOWEST to MaterialR.attr.colorSurfaceContainerLowest,
    AppColorRole.SURFACE_CONTAINER_LOW to MaterialR.attr.colorSurfaceContainerLow,
    AppColorRole.SURFACE_CONTAINER to MaterialR.attr.colorSurfaceContainer,
    AppColorRole.SURFACE_CONTAINER_HIGH to MaterialR.attr.colorSurfaceContainerHigh,
    AppColorRole.SURFACE_CONTAINER_HIGHEST to MaterialR.attr.colorSurfaceContainerHighest,
)

/**
 * Reads the color of every [AppColorRole] from this context's theme.
 *
 * @return ARGB color per role; roles the theme does not define are left out (and logged).
 */
fun Context.readThemeColors(): Map<AppColorRole, Int> {
    val roles = roleAttributes.keys.toList()
    val attrs = roles.map { roleAttributes.getValue(it) }.toIntArray()
    val typedArray = obtainStyledAttributes(attrs)
    try {
        val colors = buildMap {
            roles.forEachIndexed { index, role ->
                if (typedArray.hasValue(index)) {
                    put(role, typedArray.getColor(index, 0))
                }
            }
        }
        val missing = roles - colors.keys
        if (missing.isEmpty()) {
            Timber.d("Read all %d theme colors", colors.size)
        } else {
            Timber.w("Theme does not define %s; using Material 3 defaults", missing)
        }
        return colors
    } finally {
        typedArray.recycle()
    }
}

/**
 * Creates a [ComposeView] that shows [content] inside [AppTheme], for use as a Fragment's view.
 *
 * The composition is disposed with the Fragment's view (not the Fragment), which is the right
 * lifecycle for Fragments kept on the back stack.
 */
fun Fragment.appComposeView(content: @Composable () -> Unit): ComposeView =
    ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { AppTheme(content) }
    }
