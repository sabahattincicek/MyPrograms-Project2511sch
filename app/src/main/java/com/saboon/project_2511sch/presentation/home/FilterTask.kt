package com.saboon.project_2511sch.presentation.home

data class FilterTask(
    var lesson: Boolean = true,
    var exam: Boolean = true,
    var homework: Boolean = true
)
