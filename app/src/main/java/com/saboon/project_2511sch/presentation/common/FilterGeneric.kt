package com.saboon.project_2511sch.presentation.common

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task

data class FilterGeneric (
    val tag: Tag? = null,
    val programTableIncludeSubItems: Boolean = true,

    val course: Course? = null,
    val courseIncludeSubItems: Boolean = true,

    val task: Task? = null,
)