package com.saboon.project_2511sch.domain.model

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.util.toFormattedString
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class TestDataTransferPackage {

    private lateinit var originalZone: TimeZone

    @Before
    fun setup() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun `export date format writes hour minute and second`() {
        // GIVEN: Monday 2026-09-07 09:05:07 UTC
        val timestamp = 1_788_771_600_000L + 5 * 60_000L + 7_000L

        // WHEN
        val text = timestamp.toFormattedString(DataTransferPackage.EXPORT_DATE_FORMAT)

        // THEN: minutes (05) must not be replaced by the day of month (07)
        assertThat(text).isEqualTo("20260907_090507")
    }

    @Test
    fun `new package gets an export date in the export format`() {
        // WHEN
        val dataPackage = DataTransferPackage(emptyList(), emptyList(), emptyList(), emptyList())

        // THEN
        assertThat(dataPackage.exportDate).matches("[0-9]{8}_[0-9]{6}")
    }
}
