package com.example.projekat_rmas.model

data class Filters(
    val author: String = "",
    val type: String = "",
    val subject: String = "",
    val rating: Int = 0,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val radius: Float = 0f
)
