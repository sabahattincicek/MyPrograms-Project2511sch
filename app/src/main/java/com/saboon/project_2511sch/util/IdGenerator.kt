package com.saboon.project_2511sch.util

import java.util.UUID

object IdGenerator {

    fun generateUserId(userName: String = "userName"): String{
        val dateMillis = System.currentTimeMillis().toString()
        val date = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHmmss")
        val uuid = java.util.UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${userName}_${uuid}"
    }

    fun generateProgramTableId(programTableTitle: String): String{
        val dateMillis = System.currentTimeMillis().toString()
        val date = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHmmss")
        val uuid = java.util.UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${programTableTitle}_${uuid}"
    }

    fun generateCourseId(courseTitle: String): String{
        val dateMillis = System.currentTimeMillis().toString()
        val date = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHmmss")
        val uuid = java.util.UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${courseTitle}_${uuid}"
    }

    fun generateTaskId(taskTitle: String): String{
        val dateMillis = System.currentTimeMillis().toString()
        val date = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHmmss")
        val uuid = java.util.UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${taskTitle}_${uuid}"
    }

    fun generateFileId(fileTitle: String): String{
        val dateMillis = System.currentTimeMillis()
        val date = dateMillis.toFormattedString("yyyyMMdd_HHmmss")
        val uuid = UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${fileTitle}_${uuid}"
    }
}