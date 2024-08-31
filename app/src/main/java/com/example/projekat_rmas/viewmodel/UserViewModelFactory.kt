package com.example.projekat_rmas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projekat_rmas.repository.FirebaseRepo

class UserViewModelFactory (private val firebaseRepo: FirebaseRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(firebaseRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
