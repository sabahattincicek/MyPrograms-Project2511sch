package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    override val id: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val version: Int = 0,
    override val isActive: Boolean = true,
    override val isDeleted: Boolean = false,
    override val deletedAt: Long = 0L,
    override val appVersionAtCreation: String,

    val programTableId: String,

    val title: String,
    val description: String,
    val people: String,
    val color: String,
    val absence: Int = 0,
) : Parcelable, BaseModel
