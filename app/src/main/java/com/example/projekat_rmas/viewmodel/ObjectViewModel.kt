package com.example.projekat_rmas.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projekat_rmas.repository.FirebaseRepo
import kotlinx.coroutines.launch
import com.example.projekat_rmas.model.MapObject


class ObjectViewModel(private val firebaseRepo: FirebaseRepo) : ViewModel() {

    var objectState by mutableStateOf<ObjectState>(ObjectState.Idle)
        private set

    // Funkcija za dodavanje objekta
    fun addObject(
        title: String,
        subject: String,
        description: String,
        locationLat: Double,
        locationLng: Double,
        imageUri: Uri?
    ) {
        objectState = ObjectState.Loading
        viewModelScope.launch {
            firebaseRepo.addObject(title, subject, description, locationLat, locationLng, imageUri) { success, message ->
                if (success) {
                    fetchAllObjects() // osvezavanje liste objekata nakon dodavanja novog
                    objectState = ObjectState.Success
                } else {
                    objectState = ObjectState.Error(message ?: "Failed to add object")
                }
            }
        }
    }

    // Funkcija za resetovanje stanja nakon uspešnog ili neuspešnog dodavanja objekta
    fun resetState() {
        objectState = ObjectState.Idle
    }

    fun fetchAllObjects() {
        objectState = ObjectState.Loading
        firebaseRepo.getAllObjects { objects, error ->
            if (error == null) {
                objectState = ObjectState.ObjectsFetched(objects)
            } else {
                objectState = ObjectState.Error(error)
            }
        }
    }
}

// Definisanje različitih stanja za dodavanje i pirbavljanje objekata
sealed class ObjectState {
    object Idle : ObjectState()
    object Loading : ObjectState()
    object Success : ObjectState()
    data class ObjectsFetched(val objects: List<MapObject>) : ObjectState() // Novo stanje
    data class Error(val message: String) : ObjectState()
}

