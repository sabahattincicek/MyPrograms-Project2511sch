package com.saboon.project_2511sch.presentation.task

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.toFormattedString
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone

/**
 * Dialogs for changing the absences of a lesson by hand (presentation layer).
 *
 * Shared by the home and course details screens so that the "+" and "-" absence buttons
 * behave the same everywhere:
 * - "+" asks on which date the lesson was missed,
 * - "-" asks which recorded absence should be removed.
 *
 * The dialogs only collect the user's choice and hand it back through a callback; saving is
 * done by the caller through ViewModelTask.
 */
object AbsenceDialogs {

    private const val DATE_PICKER_TAG = "AbsenceDialogs_datePicker"
    private const val DATE_FORMAT = "dd.MM.yyyy EEEE"

    /**
     * Shows a date picker preselecting [defaultDate] and returns the chosen day through [onDatePicked].
     */
    fun showAddAbsenceDatePicker(
        context: Context,
        fragmentManager: FragmentManager,
        defaultDate: Long,
        onDatePicked: (Long) -> Unit
    ) {
        Timber.d("Showing absence date picker: defaultDate=%d", defaultDate)
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(context.getString(R.string.absence_date))
            .setSelection(localDayToPickerSelection(defaultDate))
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val date = pickerSelectionToLocalNoon(selection)
            Timber.d("Absence date picked: date=%d", date)
            onDatePicked(date)
        }
        picker.show(fragmentManager, DATE_PICKER_TAG)
    }

    /**
     * Lists the recorded absences of [lesson], newest first, and returns the tapped one through
     * [onDatePicked]. Shows a short message instead when the lesson has no absence.
     */
    fun showRemoveAbsenceDialog(
        context: Context,
        lesson: Task.Lesson,
        onDatePicked: (Long) -> Unit
    ) {
        Timber.d("Showing remove absence dialog: lessonId=%s, records=%d", lesson.id, lesson.absence.size)
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.remove_absence)
            .setNegativeButton(R.string.cancel, null)
        if (lesson.absence.isEmpty()) {
            builder.setMessage(R.string.no_absence_recorded)
        } else {
            val dates = lesson.absence.sortedDescending()
            val items = dates.map { it.toFormattedString(DATE_FORMAT) }.toTypedArray()
            builder.setItems(items) { _, index ->
                Timber.d("Absence chosen for removal: lessonId=%s, date=%d", lesson.id, dates[index])
                onDatePicked(dates[index])
            }
        }
        builder.show()
    }

    /**
     * Converts a timestamp to what MaterialDatePicker expects as selection: midnight UTC of the
     * same calendar day the timestamp falls on in [zone].
     */
    internal fun localDayToPickerSelection(millis: Long, zone: TimeZone = TimeZone.getDefault()): Long {
        val local = Calendar.getInstance(zone).apply { timeInMillis = millis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    /**
     * Converts a MaterialDatePicker selection (midnight UTC of the chosen day) to noon of that
     * same calendar day in [zone]. Noon keeps the stored date on the chosen day in every time
     * zone, which midnight UTC does not (it is the previous evening west of Greenwich).
     */
    internal fun pickerSelectionToLocalNoon(selection: Long, zone: TimeZone = TimeZone.getDefault()): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = selection }
        return Calendar.getInstance(zone).apply {
            clear()
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
        }.timeInMillis
    }
}
