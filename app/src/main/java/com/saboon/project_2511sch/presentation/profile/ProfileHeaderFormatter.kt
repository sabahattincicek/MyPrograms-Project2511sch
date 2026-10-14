package com.saboon.project_2511sch.presentation.profile

import java.util.Locale

/**
 * Builds the texts shown on the profile header card in [ProfileFragment].
 *
 * Presentation layer helper with no Android dependency, so the rules can be unit tested:
 * - the avatar shows the initials of the user's name (first letter of the first and the last word),
 * - the line under the name joins the non-empty title, class and school with " · ".
 *
 * Empty results are returned as empty strings; the fragment decides what to show instead
 * (a person icon for the avatar, a hint text for the details line).
 */
object ProfileHeaderFormatter {

    /** Separator placed between the title, class and school on the details line. */
    const val SEPARATOR = " · "

    /**
     * Returns up to two upper-case initials for [fullName], e.g. "Ada Lovelace" -> "AL", "Ada" -> "A".
     *
     * @param locale Locale used for upper-casing, so Turkish "i" becomes "İ".
     * @return The initials, or an empty string when the name is blank.
     */
    fun initials(fullName: String, locale: Locale = Locale.getDefault()): String {
        val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return ""
        val letters = if (words.size == 1) {
            words.first().take(1)
        } else {
            words.first().take(1) + words.last().take(1)
        }
        return letters.uppercase(locale)
    }

    /**
     * Joins the non-blank, trimmed [role], [academicLevel] and [institution] with [SEPARATOR].
     *
     * @return The details line, or an empty string when all three are blank.
     */
    fun details(role: String, academicLevel: String, institution: String): String =
        listOf(role, academicLevel, institution)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(SEPARATOR)
}
