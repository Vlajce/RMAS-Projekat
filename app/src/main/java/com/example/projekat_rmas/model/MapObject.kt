package com.example.projekat_rmas.model

data class MapObject(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrl: String? = null,
    val author: String = "",
    val ownerId: String = "",
    val type: String = "",
    val rating: Float? = 0f,
    val timestamp: Long = 0L
)
