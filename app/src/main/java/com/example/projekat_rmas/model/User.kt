package com.example.projekat_rmas.model

data class User(
    val id: String = "",
    val username: String = "",
    val fullname: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val points: Int = 0
)
