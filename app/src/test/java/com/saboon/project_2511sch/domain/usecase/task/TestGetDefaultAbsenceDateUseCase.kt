package com.saboon.project_2511sch.domain.usecase.task

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.util.RecurrenceRule
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class TestGetDefaultAbsenceDateUseCase : TestBaseTask() {

    private val useCase = GetDefaultAbsenceDateUseCase()
    private lateinit var originalZone: TimeZone

    private val day = 24 * 60 * 60 * 1000L
    private val week = 7 * day
    // Monday 2026-09-07 09:00 UTC
    private val start = 1_788_771_600_000L

    @Before
    fun setup() {
        // UTC has no daylight saving, so a week is always exactly 7 days in these tests
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    private fun weeklyLesson(until: Long = Long.MAX_VALUE) = baseLesson.copy(
        date = start,
        recurrenceRule = RecurrenceRule(RecurrenceRule.Frequency.WEEKLY, start, until)
    )

    @Test
    fun `lesson that does not repeat returns its own date`() {
        // GIVEN
        val lesson = baseLesson.copy(date = start, recurrenceRule = RecurrenceRule())

        // WHEN
        val result = useCase(lesson, start + 10 * week)

        // THEN
        assertThat(result).isEqualTo(start)
    }

    @Test
    fun `weekly lesson returns the latest session before now`() {
        // GIVEN: now is two days after the 4th session
        val now = start + 3 * week + 2 * day

        // WHEN
        val result = useCase(weeklyLesson(), now)

        // THEN
        assertThat(result).isEqualTo(start + 3 * week)
    }

    @Test
    fun `session starting exactly now counts as started`() {
        // WHEN
        val result = useCase(weeklyLesson(), start + 2 * week)

        // THEN
        assertThat(result).isEqualTo(start + 2 * week)
    }

    @Test
    fun `lesson that has not started yet returns its first date`() {
        // WHEN
        val result = useCase(weeklyLesson(), start - day)

        // THEN
        assertThat(result).isEqualTo(start)
    }

    @Test
    fun `sessions after the end of the recurrence are ignored`() {
        // GIVEN: the lesson ended after its 2nd session, now is much later
        val lesson = weeklyLesson(until = start + week + day)

        // WHEN
        val result = useCase(lesson, start + 20 * week)

        // THEN
        assertThat(result).isEqualTo(start + week)
    }

    @Test
    fun `daily lesson steps one day at a time`() {
        // GIVEN
        val lesson = baseLesson.copy(
            date = start,
            recurrenceRule = RecurrenceRule(RecurrenceRule.Frequency.DAILY, start, Long.MAX_VALUE)
        )

        // WHEN
        val result = useCase(lesson, start + 5 * day + 1)

        // THEN
        assertThat(result).isEqualTo(start + 5 * day)
    }
}
