package com.saboon.project_2511sch.util

import kotlinx.serialization.Serializable


@Serializable
data class Character (
    val id: String,
    val name: String,
    val personality: Map<String, String>,
    val image: String,
    val activities: List<Activity>,
)

@Serializable
data class Activity(
    val id: String,
    val image: String,
    val content: Map<String, String>
)

//Refik → Mükemmeliyetçi
//Melisa → Pratik zekâ
//Selim → Adrenalin bağımlısı
//Zeynep → Akademik deha
//Arif → Sosyal ve karizmatik
//Ezgi → İyi niyetli ve saf