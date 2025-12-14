package com.saboon.project_2511sch.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String, //when first start the user created and set the userId
    val authProviderId: String?, //when user set the remote and sync this field creates

    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val rowVersion: Int = 1,
    val isActive: Boolean = false,

    val email: String?,
    val userName: String?,
    val firstName: String?,
    val secondName: String?,
    val photoUrl: String?,
    val userRole: String?, //student, teacher, ...
    val academicLevel: String?, //bachelor 2. level, lisans 1. sinif
    val organization: String?,

    val lastLoginAt: Long?,
    val lastLoginIp: String?,
    val isVerified: Boolean = false,
) : Parcelable
