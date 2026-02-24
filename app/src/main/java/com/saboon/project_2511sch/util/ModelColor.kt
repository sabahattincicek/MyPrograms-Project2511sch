package com.saboon.project_2511sch.util

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.math.min

@Parcelize
@Serializable
data class ModelColor(
    val colorHex: String = ModelColorConstats.COLOR_1
): Parcelable {
    // String olan Hex kodunu Android'in anladığı Int rengine çevirir
    @ColorInt
    fun getInt(): Int = colorHex.toColorInt()

    /**
     * Ana renkten yola çıkarak arka plan (Container) rengini üretir.
     */
    @ColorInt
    fun getContainerColor(context: Context): Int {
        val mainColor = getInt()
        val isDark = (context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val hsv = FloatArray(3)
        Color.colorToHSV(mainColor, hsv)
        // hsv[0] -> Hue (Renk tonu 0-360)
        // hsv[1] -> Saturation (Doygunluk 0-1)
        // hsv[2] -> Value (Parlaklık 0-1)

        if (isDark) {
            // KARANLIK MOD: Koyu ve oturaklı bir arka plan
            hsv[0] = hsv[0]
            hsv[1] = hsv[1] * 0.6f
            hsv[2] = hsv[2] * 0.6f
        } else {
            // AYDINLIK MOD: Pastel ve ferah bir arka plan
            hsv[0] = hsv[0]
            hsv[1] = hsv[1] * 0.60f
            hsv[2] = hsv[2] * 0.85f
        }
        return Color.HSVToColor(hsv)
    }

    /**
     * Arka plan rengine göre okunabilir (Siyah veya Beyaz) yazı rengini döner.
     */
    @ColorInt
    fun getOnContainerTextColor(context: Context): Int {
        val bgColor = getContainerColor(context)
        val luminance = ColorUtils.calculateLuminance(bgColor)
        return if (luminance > 0.5) "#1A1C1E".toColorInt() else "#F0F0F3".toColorInt()
    }

    /**
     * Ana rengin üzerine gelecek olan yazı rengini (Örn: Divider üstü) döner.
     */
    @ColorInt
    fun getOnMainTextColor(): Int {
        val luminance = ColorUtils.calculateLuminance(getInt())
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }
}