package com.saboon.project_2511sch.util

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class TestRecurrenceRule {

    private lateinit var originalZone: TimeZone
    private val day = 24 * 60 * 60 * 1000L
    // Monday 2026-09-07 09:00 UTC
    private val start = 1_788_771_600_000L

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
    fun `rule string round trip keeps every field`() {
        // GIVEN
        val rule = RecurrenceRule(RecurrenceRule.Frequency.WEEKLY, 1_000L, 9_000L)

        // WHEN
        val text = rule.toRuleString()
        val parsed = RecurrenceRule.fromRuleString(text)

        // THEN
        assertThat(text).isEqualTo("DTSTART=1000;FREQ=WEEKLY;UNTIL=9000")
        assertThat(parsed).isEqualTo(rule)
    }

    @Test
    fun `rule that does not repeat is stored as an empty string`() {
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.ONCE).toRuleString()).isEmpty()
    }

    @Test
    fun `empty or missing rule string means no repetition`() {
        assertThat(RecurrenceRule.fromRuleString("").freq).isEqualTo(RecurrenceRule.Frequency.ONCE)
        assertThat(RecurrenceRule.fromRuleString(null).freq).isEqualTo(RecurrenceRule.Frequency.ONCE)
    }

    @Test
    fun `unknown frequency falls back to no repetition`() {
        // WHEN
        val parsed = RecurrenceRule.fromRuleString("DTSTART=1;FREQ=HOURLY;UNTIL=2")

        // THEN
        assertThat(parsed.freq).isEqualTo(RecurrenceRule.Frequency.ONCE)
    }

    @Test
    fun `next occurrence steps by the frequency`() {
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.DAILY).getNextOccurrence(start)).isEqualTo(start + day)
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.WEEKLY).getNextOccurrence(start)).isEqualTo(start + 7 * day)
        // September has 30 days
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.MONTHLY).getNextOccurrence(start)).isEqualTo(start + 30 * day)
        // 2027 is not a leap year and the step does not cross 29 February
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.YEARLY).getNextOccurrence(start)).isEqualTo(start + 365 * day)
    }

    @Test
    fun `rule that does not repeat has no next occurrence`() {
        assertThat(RecurrenceRule(RecurrenceRule.Frequency.ONCE).getNextOccurrence(start)).isNull()
    }
}
