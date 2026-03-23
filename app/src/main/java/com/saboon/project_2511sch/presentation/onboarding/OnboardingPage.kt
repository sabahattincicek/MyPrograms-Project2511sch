package com.saboon.project_2511sch.presentation.onboarding

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val isPermission: Boolean = false
)