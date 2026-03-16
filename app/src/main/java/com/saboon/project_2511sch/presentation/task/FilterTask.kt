package com.saboon.project_2511sch.presentation.task

data class FilterTask(
    val lesson: Boolean = true,
    val exam: Boolean = true,
    val homework: Boolean = true
)