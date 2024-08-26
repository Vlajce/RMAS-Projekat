package com.example.projekat_rmas.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekat_rmas.repository.FirebaseRepo
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: FirebaseRepo) : ViewModel() {

    var registrationState by mutableStateOf<RegistrationState>(RegistrationState.Idle)
        private set

    fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> "Username cannot be empty."
            !username.matches(Regex("^[a-zA-Z0-9]+$")) -> "Username can only contain letters and numbers."
            else -> null
        }
    }

    fun validateFullName(fullname: String): String? {
        return when {
            fullname.isEmpty() -> "Full name cannot be empty."
            !fullname.matches(Regex("^[a-zA-Z\\s]+$")) -> "Full name can only contain letters and spaces."
            else -> null
        }
    }

    fun validatePhoneNumber(phoneNumber: String): String? {
        return when {
            phoneNumber.isEmpty() -> "Phone number cannot be empty."
            !phoneNumber.matches(Regex("^[0-9]+$")) -> "Phone number can only contain numbers."
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email cannot be empty."
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) -> "Invalid email format."
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty."
            password.length < 6 -> "Password must be at least 6 characters long."
            password.contains(" ") -> "Password cannot contain spaces."
            !password.matches(Regex(".*[A-Z].*")) -> "Password must contain at least one uppercase letter."
            !password.matches(Regex(".*[a-z].*")) -> "Password must contain at least one lowercase letter."
            !password.matches(Regex(".*[@$!%*?&#].*")) -> "Password must contain at least one special character."
            !password.matches(Regex(".*\\d.*")) -> "Password must contain at least one digit."
            else -> null
        }
    }

    fun validateImage(imageUri: Uri?): String? {
        return if (imageUri == null) {
            "You must select a profile image."
        } else {
            null
        }
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        fullname: String,
        phoneNumber: String,
        imageUri: Uri?
    ) {
        registrationState = RegistrationState.Loading
        viewModelScope.launch {
            authRepository.registerUser(username, email, password, fullname, phoneNumber, imageUri) { success, message ->
                registrationState = if (success) {
                    RegistrationState.Success
                } else {
                    RegistrationState.Error(message ?: "Registration failed")
                }
            }
        }
    }

    fun loginUser(email: String, password: String) {
        registrationState = RegistrationState.Loading
        viewModelScope.launch {
            authRepository.loginUser(email, password) { success, message ->
                registrationState = if (success) {
                    RegistrationState.Success
                } else {
                    RegistrationState.Error("Invalid email or password. Please try again.")
                }
            }
        }
    }

}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}
