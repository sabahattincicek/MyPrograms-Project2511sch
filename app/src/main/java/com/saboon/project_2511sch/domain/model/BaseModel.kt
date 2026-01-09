package com.saboon.project_2511sch.domain.model

interface BaseModel {
    val id: String
    val createdAt: Long
    val updatedAt: Long
    val version: Int
}