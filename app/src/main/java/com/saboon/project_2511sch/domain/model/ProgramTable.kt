package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import com.saboon.project_2511sch.util.ModelColors
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProgramTable(
    override val id: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val createdBy: String,
    override val appVersionAtCreation: String,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val updatedBy: String,
    override val version: Int = 1,
    override val isActive: Boolean = true,
    override val isDeleted: Boolean = false,
    override val deletedAt: Long = 0L,
    override val syncStatus: Int = 0,
    override val contentHash: String = "",
    override val serverVersion: Int = 1,

    val title: String,
    val description: String,
    val color: String = ModelColors.MODEL_COLOR_1,
    ): Parcelable, BaseModel
