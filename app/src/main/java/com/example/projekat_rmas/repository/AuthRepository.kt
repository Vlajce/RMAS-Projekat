package com.example.projekat_rmas.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun registerUser(
        username : String,
        email: String,
        password: String,
        fullname: String,
        phoneNumber: String,
        imageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val profileData = hashMapOf(
                            "username" to username,
                            "fullname" to fullname,
                            "phoneNumber" to phoneNumber,
                            "email" to email
                        )

                        // Čuvanje podataka u Firestore
                        db.collection("users").document(userId).set(profileData)
                            .addOnSuccessListener {
                                // Upload slike ako postoji
                                imageUri?.let { uri ->
                                    val storageRef = storage.reference.child("profile_photos/$userId.jpg")
                                    storageRef.putFile(uri)
                                        .addOnSuccessListener {
                                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                db.collection("users").document(userId)
                                                    .update("photoUrl", downloadUrl.toString())
                                                    .addOnSuccessListener {
                                                        onResult(true, null)
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            onResult(false, exception.message)
                                        }
                                } ?: run {
                                    onResult(true, null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                onResult(false, exception.message)
                            }
                    } else {
                        onResult(false, "User ID is null")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}
