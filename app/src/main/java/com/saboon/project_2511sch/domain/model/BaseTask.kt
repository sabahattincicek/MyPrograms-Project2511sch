package com.saboon.project_2511sch.domain.model

interface BaseTask {
    val courseId: String
    val programTableId: String
    val title: String?
    val description: String?
}

enum class TaskType{
    LESSON, EXAM, HOMEWORK
}