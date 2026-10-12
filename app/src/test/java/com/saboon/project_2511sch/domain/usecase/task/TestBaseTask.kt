package com.saboon.project_2511sch.domain.usecase.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.ModelColor
import com.saboon.project_2511sch.util.RecurrenceRule

/**
 * Shared sample tags, courses, files and tasks for unit tests. Copy and override only the fields a test cares about.
 */
abstract class TestBaseTask {
    val baseTag = Tag(
        id = "test-tag",
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        title = "",
        description = "",
        color = ModelColor()
    )
    val baseCourse = Course(
        id = "test-course",
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        tagId = null,
        title = "",
        description = "",
        people = "",
        color = ModelColor()
    )
    val baseSFile = SFile(
        id = "test-sfile",
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        courseId = "test-course",
        title = "",
        description = "",
        filePath = ""
    )
    val baseLesson = Task.Lesson(
        id = "test-lesson",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        courseId = "test-course",
        remindBefore = 0,
        title = "",
        description = "",
        date = 0L,
        recurrenceRule = RecurrenceRule(),
        timeStart = 0L,
        timeEnd = 0L,
        absence = emptyList(),
        place = ""
    )
    val baseExam = Task.Exam(
        id = "test-exam",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        courseId = "test-course",
        remindBefore = 0,
        title = "",
        description = "",
        date = 0L,
        timeStart = 0L,
        timeEnd = 0L,
        place = ""
    )
    val baseHomework = Task.Homework(
        id = "test-homework",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        courseId = "test-course",
        remindBefore = 0,
        title = "",
        description = "",
        dueDate = 0L,
        dueTime = 0L
    )
}
