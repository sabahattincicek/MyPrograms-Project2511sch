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
            MODEL_COLOR_1 -> R.attr.modelColor1
            MODEL_COLOR_2 -> R.attr.modelColor2
            MODEL_COLOR_3 -> R.attr.modelColor3
            MODEL_COLOR_4 -> R.attr.modelColor4
            MODEL_COLOR_5 -> R.attr.modelColor5
            MODEL_COLOR_6 -> R.attr.modelColor6
            MODEL_COLOR_7 -> R.attr.modelColor7
            MODEL_COLOR_8 -> R.attr.modelColor8

            MODEL_COLOR_LESSON -> R.attr.modelColorLesson
            MODEL_COLOR_EXAM -> R.attr.modelColorExam
            MODEL_COLOR_HOMEWORK -> R.attr.modelColorHomework
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
            MODEL_COLOR_1 -> R.attr.modelColorContainer1
            MODEL_COLOR_2 -> R.attr.modelColorContainer2
            MODEL_COLOR_3 -> R.attr.modelColorContainer3
            MODEL_COLOR_4 -> R.attr.modelColorContainer4
            MODEL_COLOR_5 -> R.attr.modelColorContainer5
            MODEL_COLOR_6 -> R.attr.modelColorContainer6
            MODEL_COLOR_7 -> R.attr.modelColorContainer7
            MODEL_COLOR_8 -> R.attr.modelColorContainer8

            MODEL_COLOR_LESSON -> R.attr.modelColorLessonContainer
            MODEL_COLOR_EXAM -> R.attr.modelColorExamContainer
            MODEL_COLOR_HOMEWORK -> R.attr.modelColorHomeworkContainer
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