package com.saboon.project_2511sch.presentation

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.CourseFiles
import com.saboon.project_2511sch.domain.model.CourseGroup
import com.saboon.project_2511sch.domain.model.CourseTasks
import com.saboon.project_2511sch.domain.model.HomeDay
import com.saboon.project_2511sch.domain.model.HomeTimeline
import com.saboon.project_2511sch.domain.model.HomeTimelineItem
import com.saboon.project_2511sch.domain.usecase.task.TestBaseTask
import com.saboon.project_2511sch.presentation.course.DisplayItemCourse
import com.saboon.project_2511sch.presentation.course.toCourseDisplayItems
import com.saboon.project_2511sch.presentation.home.DisplayItemHome
import com.saboon.project_2511sch.presentation.home.toHomeDisplayItems
import com.saboon.project_2511sch.presentation.sfile.DisplayItemSFile
import com.saboon.project_2511sch.presentation.sfile.toSFileDisplayItems
import com.saboon.project_2511sch.presentation.tag.DisplayItemTag
import com.saboon.project_2511sch.presentation.tag.toTagDisplayItems
import com.saboon.project_2511sch.presentation.task.DisplayItemTask
import com.saboon.project_2511sch.presentation.task.toTaskDisplayItems
import org.junit.Test

/**
 * Tests of the mappers that turn domain results into list rows (headers, content, footers).
 */
class TestDisplayItemMappers : TestBaseTask() {

    // TAG

    @Test
    fun `tags become content rows followed by a footer with the count`() {
        // GIVEN
        val tags = listOf(baseTag.copy(id = "t1"), baseTag.copy(id = "t2"))

        // WHEN
        val items = tags.toTagDisplayItems()

        // THEN
        assertThat(items).containsExactly(
            DisplayItemTag.ContentTag(tags[0]),
            DisplayItemTag.ContentTag(tags[1]),
            DisplayItemTag.FooterTag(2)
        ).inOrder()
    }

    @Test
    fun `no tag gives no rows`() {
        assertThat(emptyList<com.saboon.project_2511sch.domain.model.Tag>().toTagDisplayItems()).isEmpty()
    }

    // COURSE

    @Test
    fun `course groups become header and content rows followed by a footer with the total`() {
        // GIVEN
        val tag = baseTag.copy(id = "t1")
        val math = baseCourse.copy(id = "c1", tagId = "t1")
        val sport = baseCourse.copy(id = "c2", tagId = null)

        // WHEN
        val items = listOf(CourseGroup(tag, listOf(math)), CourseGroup(null, listOf(sport))).toCourseDisplayItems()

        // THEN
        assertThat(items).containsExactly(
            DisplayItemCourse.HeaderCourse(tag),
            DisplayItemCourse.ContentCourse(math, tag),
            DisplayItemCourse.HeaderCourse(null),
            DisplayItemCourse.ContentCourse(sport, null),
            DisplayItemCourse.FooterCourse(2)
        ).inOrder()
    }

    @Test
    fun `no course gives no rows`() {
        assertThat(emptyList<CourseGroup>().toCourseDisplayItems()).isEmpty()
    }

    // FILE

    @Test
    fun `file groups become header and content rows followed by a footer with the total`() {
        // GIVEN
        val math = baseCourse.copy(id = "c1", title = "Matematik")
        val notes = baseSFile.copy(id = "f1", courseId = "c1")
        val slides = baseSFile.copy(id = "f2", courseId = "c1")

        // WHEN
        val items = listOf(CourseFiles(math, listOf(notes, slides))).toSFileDisplayItems()

        // THEN
        assertThat(items).containsExactly(
            DisplayItemSFile.HeaderSFile("Matematik"),
            DisplayItemSFile.ContentSFile(math, notes),
            DisplayItemSFile.ContentSFile(math, slides),
            DisplayItemSFile.FooterSFile(2)
        ).inOrder()
    }

    @Test
    fun `no file gives no rows in the global list`() {
        assertThat(emptyList<CourseFiles>().toSFileDisplayItems()).isEmpty()
    }

    @Test
    fun `files of one course become content rows and always end with a footer`() {
        // GIVEN
        val math = baseCourse.copy(id = "c1")
        val notes = baseSFile.copy(id = "f1", courseId = "c1")

        // WHEN
        val withFile = listOf(notes).toSFileDisplayItems(math)
        val withoutFile = emptyList<com.saboon.project_2511sch.domain.model.SFile>().toSFileDisplayItems(math)

        // THEN
        assertThat(withFile).containsExactly(
            DisplayItemSFile.ContentSFile(math, notes),
            DisplayItemSFile.FooterSFile(1)
        ).inOrder()
        assertThat(withoutFile).containsExactly(DisplayItemSFile.FooterSFile(0))
    }

    // TASK

    @Test
    fun `tasks are grouped by type under headers and end with a footer with the total`() {
        // GIVEN
        val course = baseCourse.copy(id = "c1")
        val theory = baseLesson.copy(id = "l1")
        val lab = baseLesson.copy(id = "l2")
        val exam = baseExam.copy(id = "e1")

        // WHEN
        val items = CourseTasks(course, listOf(theory, exam, lab)).toTaskDisplayItems()

        // THEN
        assertThat(items).containsExactly(
            DisplayItemTask.HeaderTask("Lesson"),
            DisplayItemTask.ContentTask(course, theory),
            DisplayItemTask.ContentTask(course, lab),
            DisplayItemTask.HeaderTask("Exam"),
            DisplayItemTask.ContentTask(course, exam),
            DisplayItemTask.FooterTask(3)
        ).inOrder()
    }

    @Test
    fun `course without task gives no rows`() {
        assertThat(CourseTasks(baseCourse, emptyList()).toTaskDisplayItems()).isEmpty()
    }

    // HOME

    @Test
    fun `timeline days become header and content rows followed by a summary footer`() {
        // GIVEN
        val course = baseCourse.copy(id = "c1")
        val item = HomeTimelineItem(null, course, baseLesson.copy(id = "l1"), "l1_100")
        val timeline = HomeTimeline(
            startDate = 0L,
            endDate = 999L,
            days = listOf(HomeDay(100L, listOf(item)), HomeDay(200L, emptyList())),
            hasTasks = true
        )

        // WHEN
        val items = timeline.toHomeDisplayItems()

        // THEN
        assertThat(items).containsExactly(
            DisplayItemHome.HeaderItemHome(100L),
            DisplayItemHome.ContentItemHome(null, course, item.task, "l1_100"),
            DisplayItemHome.HeaderItemHome(200L),
            DisplayItemHome.FooterItemHome(0L, 999L, 1)
        ).inOrder()
    }

    @Test
    fun `timeline with tasks but no day in range still shows the footer`() {
        // WHEN
        val items = HomeTimeline(0L, 999L, emptyList(), hasTasks = true).toHomeDisplayItems()

        // THEN
        assertThat(items).containsExactly(DisplayItemHome.FooterItemHome(0L, 999L, 0))
    }

    @Test
    fun `timeline with nothing to show gives no rows`() {
        assertThat(HomeTimeline(0L, 999L, emptyList(), hasTasks = false).toHomeDisplayItems()).isEmpty()
    }
}
