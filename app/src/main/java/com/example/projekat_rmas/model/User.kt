package com.example.projekat_rmas.model

data class User(
    val userId: String,
    val username: String,
    val fullname: String,
    val email: String,
    val phoneNumber: String,
    val photoUrl: String? = null
)