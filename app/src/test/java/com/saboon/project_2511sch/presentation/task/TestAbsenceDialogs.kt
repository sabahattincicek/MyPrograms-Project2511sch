package com.saboon.project_2511sch.presentation.task

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TestAbsenceDialogs {

    private val utc = TimeZone.getTimeZone("UTC")
    private val istanbul = TimeZone.getTimeZone("Europe/Istanbul")
    private val newYork = TimeZone.getTimeZone("America/New_York")

    private fun millis(zone: TimeZone, year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance(zone).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis

    @Test
    fun `local day maps to midnight UTC of the same calendar day`() {
        // GIVEN: 14 September 2026 01:30 in Istanbul is still 13 September in UTC
        val local = millis(istanbul, 2026, 9, 14, 1, 30)

        // WHEN
        val selection = AbsenceDialogs.localDayToPickerSelection(local, istanbul)

        // THEN
        assertThat(selection).isEqualTo(millis(utc, 2026, 9, 14))
    }

    @Test
    fun `picker selection maps to noon of the chosen day east of Greenwich`() {
        // WHEN
        val result = AbsenceDialogs.pickerSelectionToLocalNoon(millis(utc, 2026, 9, 14), istanbul)

        // THEN
        assertThat(result).isEqualTo(millis(istanbul, 2026, 9, 14, 12))
    }

    @Test
    fun `picker selection stays on the chosen day west of Greenwich`() {
        // GIVEN: midnight UTC is the previous evening in New York
        val selection = millis(utc, 2026, 9, 14)

        // WHEN
        val result = AbsenceDialogs.pickerSelectionToLocalNoon(selection, newYork)

        // THEN
        assertThat(result).isEqualTo(millis(newYork, 2026, 9, 14, 12))
    }

    @Test
    fun `picking the preselected day returns the same calendar day`() {
        // GIVEN
        val stored = millis(newYork, 2026, 9, 14, 23, 45)

        // WHEN
        val selection = AbsenceDialogs.localDayToPickerSelection(stored, newYork)
        val result = AbsenceDialogs.pickerSelectionToLocalNoon(selection, newYork)

        // THEN
        assertThat(result).isEqualTo(millis(newYork, 2026, 9, 14, 12))
    }
}
