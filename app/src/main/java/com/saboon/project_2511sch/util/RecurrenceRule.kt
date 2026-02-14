package com.saboon.project_2511sch.util

import android.icu.util.Calendar
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecurrenceRule(
    var freq: Frequency = Frequency.ONCE,
    var dtStart: Long = 0L,
    var until: Long = Long.MAX_VALUE,
): Parcelable {
    enum class Frequency {
        ONCE, DAILY, WEEKLY, MONTHLY, YEARLY
    }
    /**
     * Converts the object back to a string like:
     * "DTSTART=1735489600000;FREQ=WEEKLY;UNTIL=1767025600000"
     */
    fun toRuleString(): String {
        if (freq == Frequency.ONCE) return ""
        return "DTSTART=$dtStart;FREQ=$freq;UNTIL=$until"
    }

    /**
     * Calculates the next date based on the frequency.
     */
    fun getNextOccurrence(currentDate: Long): Long? {
        if (freq == Frequency.ONCE) return null

        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        return when (freq) {
            Frequency.DAILY -> calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            Frequency.WEEKLY -> calendar.apply { add(Calendar.WEEK_OF_YEAR, 1) }.timeInMillis
            Frequency.MONTHLY -> calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            Frequency.YEARLY -> calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis
            else -> null
        }
    }

    companion object {
        /**
         * Parses a string rule into a RecurrenceRule object safely.
         */
        fun fromRuleString(rule: String?): RecurrenceRule {
            if (rule.isNullOrBlank()) return RecurrenceRule(Frequency.ONCE)

            val parts = rule.split(";")
            val dtStart = parts.find { it.startsWith("DTSTART=") }?.substringAfter("DTSTART=")?.toLongOrNull() ?: 0L
            val freqStr = parts.find { it.startsWith("FREQ=") }?.substringAfter("FREQ=") ?: "ONCE"
            val freq = try { Frequency.valueOf(freqStr) } catch (e: Exception) { Frequency.ONCE }
            val until = parts.find { it.startsWith("UNTIL=") }?.substringAfter("UNTIL=")?.toLongOrNull()

            return RecurrenceRule(freq, dtStart, until!!)
        }
    }
}
