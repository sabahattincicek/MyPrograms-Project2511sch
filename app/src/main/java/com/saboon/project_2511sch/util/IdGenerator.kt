package com.saboon.project_2511sch.util

import java.util.UUID

object IdGenerator {

    fun generateId(text: String = "text"): String{
        val formattedText = text.trim().replace("\\s+".toRegex(), "-")
        val dateMillis = System.currentTimeMillis().toString()
        val date = System.currentTimeMillis().toFormattedString("yyyyMMdd_HHmmss")
        val uuid = UUID.randomUUID().toString()
        return "${dateMillis}_${date}_${formattedText}_${uuid}"
    }
}