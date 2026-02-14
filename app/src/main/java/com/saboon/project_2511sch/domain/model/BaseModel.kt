package com.saboon.project_2511sch.domain.model

interface BaseModel {
    val id: String
    val createdBy: String
    val updatedBy: String
    val createdAt: Long
    val updatedAt: Long
    val version: Int
    val isActive: Boolean
    val isDeleted: Boolean
    val deletedAt: Long
    val appVersionAtCreation: String
    val title: String
    val description: String
}