package com.saboon.project_2511sch.domain.usecase.task

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.AbsenceRecord
import org.junit.Test

class TestGetCourseAbsenceRecordsUseCase : TestBaseTask() {

    private val useCase = GetCourseAbsenceRecordsUseCase()

    @Test
    fun `empty task list returns no records`() {
        // WHEN
        val result = useCase(emptyList())

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun `lesson without absence returns no records`() {
        // GIVEN
        val lesson = baseLesson.copy(absence = emptyList())

        // WHEN
        val result = useCase(listOf(lesson))

        // THEN
        assertThat(result).isEmpty()
    }

    @Test
    fun `exams and homeworks are ignored`() {
        // GIVEN
        val lesson = baseLesson.copy(id = "l1", title = "Teori", absence = listOf(100L))

        // WHEN
        val result = useCase(listOf(baseExam, lesson, baseHomework))

        // THEN
        assertThat(result).containsExactly(AbsenceRecord("l1", "Teori", 100L))
    }

    @Test
    fun `absences of several lessons are merged and sorted newest first`() {
        // GIVEN
        val theory = baseLesson.copy(id = "l1", title = "Teori", absence = listOf(100L, 300L))
        val lab = baseLesson.copy(id = "l2", title = "Lab", absence = listOf(200L))

        // WHEN
        val result = useCase(listOf(theory, lab))

        // THEN
        assertThat(result).containsExactly(
            AbsenceRecord("l1", "Teori", 300L),
            AbsenceRecord("l2", "Lab", 200L),
            AbsenceRecord("l1", "Teori", 100L)
        ).inOrder()
    }

    @Test
    fun `same date in two lessons is ordered by lesson title`() {
        // GIVEN
        val theory = baseLesson.copy(id = "l1", title = "Teori", absence = listOf(100L))
        val lab = baseLesson.copy(id = "l2", title = "Lab", absence = listOf(100L))

        // WHEN
        val result = useCase(listOf(theory, lab))

        // THEN
        assertThat(result.map { it.lessonTitle }).containsExactly("Lab", "Teori").inOrder()
    }

    @Test
    fun `duplicate dates in one lesson are kept as separate records`() {
        // GIVEN: the same date stored twice still counts as two missed sessions
        val lesson = baseLesson.copy(id = "l1", title = "Ders", absence = listOf(100L, 100L))

        // WHEN
        val result = useCase(listOf(lesson))

        // THEN
        assertThat(result).hasSize(2)
    }
}
