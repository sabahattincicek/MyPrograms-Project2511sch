package com.saboon.project_2511sch.domain.model

/**
 * The files attached to one course, as shown together in the file list.
 *
 * @property course The course the files belong to.
 * @property files The attached files, in repository order.
 */
data class CourseFiles(
    val course: Course,
    val files: List<SFile>
)
