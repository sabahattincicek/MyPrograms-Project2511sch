package com.saboon.project_2511sch.util

import androidx.annotation.AttrRes
import com.saboon.project_2511sch.R

/**
 * A utility object to map semantic color names (stored in the database) to their
 * corresponding theme attributes. This allows the UI to be theme-aware (light/dark).
 */
object ModelColors {
    const val MODEL_COLOR_1 = "modelColor_1"
    const val MODEL_COLOR_2 = "modelColor_2"
    const val MODEL_COLOR_3 = "modelColor_3"
    const val MODEL_COLOR_4 = "modelColor_4"
    const val MODEL_COLOR_5 = "modelColor_5"
    const val MODEL_COLOR_6 = "modelColor_6"
    const val MODEL_COLOR_7 = "modelColor_7"
    const val MODEL_COLOR_8 = "modelColor_8"

    const val MODEL_COLOR_LESSON = "modelColor_lesson"
    const val MODEL_COLOR_EXAM = "modelColor_exam"
    const val MODEL_COLOR_HOMEWORK = "modelColor_homework"

    /**
     * Returns the theme attribute for the main color.
     * Use this for text, icons, borders, etc.
     */
    @AttrRes
    fun getThemeAttrForCustomColor(colorName: String?): Int {
        return when (colorName) {
            MODEL_COLOR_1 -> R.attr.colorCustomColor1
            MODEL_COLOR_2 -> R.attr.colorCustomColor2
            MODEL_COLOR_3 -> R.attr.colorCustomColor3
            MODEL_COLOR_4 -> R.attr.colorCustomColor4
            MODEL_COLOR_5 -> R.attr.colorCustomColor5
            MODEL_COLOR_6 -> R.attr.colorCustomColor6
            MODEL_COLOR_7 -> R.attr.colorCustomColor7
            MODEL_COLOR_8 -> R.attr.colorCustomColor8

            MODEL_COLOR_LESSON -> R.attr.colorCustomColorLesson
            MODEL_COLOR_EXAM -> R.attr.colorCustomColorExam
            MODEL_COLOR_HOMEWORK -> R.attr.colorCustomColorHomework
            else -> androidx.appcompat.R.attr.colorPrimary
        }
    }

    /**
     * Returns the theme attribute for the container color.
     * Use this for backgrounds.
     */
    @AttrRes
    fun getThemeAttrForOnCustomColor(colorName: String?): Int {
        return when (colorName) {
            MODEL_COLOR_1 -> R.attr.colorOnCustomColor1
            MODEL_COLOR_2 -> R.attr.colorOnCustomColor2
            MODEL_COLOR_3 -> R.attr.colorOnCustomColor3
            MODEL_COLOR_4 -> R.attr.colorOnCustomColor4
            MODEL_COLOR_5 -> R.attr.colorOnCustomColor5
            MODEL_COLOR_6 -> R.attr.colorOnCustomColor6
            MODEL_COLOR_7 -> R.attr.colorOnCustomColor7
            MODEL_COLOR_8 -> R.attr.colorOnCustomColor8

            MODEL_COLOR_LESSON -> R.attr.colorOnCustomColorLesson
            MODEL_COLOR_EXAM -> R.attr.colorOnCustomColorExam
            MODEL_COLOR_HOMEWORK -> R.attr.colorOnCustomColorHomework
            else -> com.google.android.material.R.attr.colorOnPrimary
        }
    }

    /**
     * Returns the theme attribute for content (e.g., text) that is placed ON a container color.
     * In most cases, this is the same as the main color.
     */
    @AttrRes
    fun getThemeAttrForCustomContainerColor(colorName: String?): Int {
        return when (colorName) {
            MODEL_COLOR_1 -> R.attr.colorCustomColor1Container
            MODEL_COLOR_2 -> R.attr.colorCustomColor2Container
            MODEL_COLOR_3 -> R.attr.colorCustomColor3Container
            MODEL_COLOR_4 -> R.attr.colorCustomColor4Container
            MODEL_COLOR_5 -> R.attr.colorCustomColor5Container
            MODEL_COLOR_6 -> R.attr.colorCustomColor6Container
            MODEL_COLOR_7 -> R.attr.colorCustomColor7Container
            MODEL_COLOR_8 -> R.attr.colorCustomColor8Container

            MODEL_COLOR_LESSON -> R.attr.colorCustomColorLessonContainer
            MODEL_COLOR_EXAM -> R.attr.colorCustomColorExamContainer
            MODEL_COLOR_HOMEWORK -> R.attr.colorCustomColorHomeworkContainer
            else -> com.google.android.material.R.attr.colorSurface
        }
    }

    /**
     * Returns the theme attribute for content (e.g., text) that is placed ON a container color.
     * In most cases, this is the same as the main color.
     */
    @AttrRes
    fun getThemeAttrForOnCustomContainerColor(colorName: String?): Int {
        return when (colorName) {
            MODEL_COLOR_1 -> R.attr.colorOnCustomColor1Container
            MODEL_COLOR_2 -> R.attr.colorOnCustomColor2Container
            MODEL_COLOR_3 -> R.attr.colorOnCustomColor3Container
            MODEL_COLOR_4 -> R.attr.colorOnCustomColor4Container
            MODEL_COLOR_5 -> R.attr.colorOnCustomColor5Container
            MODEL_COLOR_6 -> R.attr.colorOnCustomColor6Container
            MODEL_COLOR_7 -> R.attr.colorOnCustomColor7Container
            MODEL_COLOR_8 -> R.attr.colorOnCustomColor8Container

            MODEL_COLOR_LESSON -> R.attr.colorOnCustomColorLessonContainer
            MODEL_COLOR_EXAM -> R.attr.colorOnCustomColorExamContainer
            MODEL_COLOR_HOMEWORK -> R.attr.colorOnCustomColorHomeworkContainer
            else -> com.google.android.material.R.attr.colorOnSurface
        }
    }
}