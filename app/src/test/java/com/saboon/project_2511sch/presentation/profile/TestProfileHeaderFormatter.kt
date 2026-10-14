package com.saboon.project_2511sch.presentation.profile

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class TestProfileHeaderFormatter {

    private val turkish = Locale.forLanguageTag("tr-TR")

    @Test
    fun `initials takes the first letters of the first and last words`() {
        // WHEN
        val result = ProfileHeaderFormatter.initials("ada byron lovelace", Locale.ENGLISH)

        // THEN
        assertThat(result).isEqualTo("AL")
    }

    @Test
    fun `initials of a single word is one letter`() {
        assertThat(ProfileHeaderFormatter.initials("sabo", Locale.ENGLISH)).isEqualTo("S")
    }

    @Test
    fun `initials ignores extra spaces`() {
        assertThat(ProfileHeaderFormatter.initials("  ada    lovelace  ", Locale.ENGLISH)).isEqualTo("AL")
    }

    @Test
    fun `initials uses the given locale for upper case`() {
        // GIVEN a Turkish name starting with a dotted i
        val name = "ilker ışık"

        // WHEN
        val result = ProfileHeaderFormatter.initials(name, turkish)

        // THEN
        assertThat(result).isEqualTo("İI")
    }

    @Test
    fun `initials of a blank name is empty`() {
        assertThat(ProfileHeaderFormatter.initials("   ", Locale.ENGLISH)).isEmpty()
        assertThat(ProfileHeaderFormatter.initials("", Locale.ENGLISH)).isEmpty()
    }

    @Test
    fun `details joins all three values with the separator`() {
        // WHEN
        val result = ProfileHeaderFormatter.details("Student", "3rd year", "Ankara University")

        // THEN
        assertThat(result).isEqualTo("Student · 3rd year · Ankara University")
    }

    @Test
    fun `details skips blank values and trims the others`() {
        // WHEN
        val result = ProfileHeaderFormatter.details("  ", " 3rd year ", "Ankara University")

        // THEN
        assertThat(result).isEqualTo("3rd year · Ankara University")
    }

    @Test
    fun `details is empty when all values are blank`() {
        assertThat(ProfileHeaderFormatter.details("", " ", "")).isEmpty()
    }
}
