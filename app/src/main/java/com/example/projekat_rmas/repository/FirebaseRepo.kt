package com.example.projekat_rmas.repository

import android.net.Uri
import android.util.Log
import com.example.projekat_rmas.model.MapObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun registerUser(
        username: String,
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
                    val user = auth.currentUser
                    if (user != null) {
                        // Ažuriraj profil korisnika i postavi displayName na username
                        val profileUpdates = userProfileChangeRequest {
                            displayName = username
                        }

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    val userId = user.uid
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
                                    onResult(false, profileUpdateTask.exception?.message)
                                }
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

    fun addObject(
        title: String,
        subject: String,
        description: String,
        locationLat: Double,
        locationLng: Double,
        imageUri: Uri?,
        type: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        val username = auth.currentUser?.displayName
        if (userId != null && username != null) {
            // Kreiraj početni objekt bez ID-ja
            val objectData = hashMapOf(
                "title" to title,
                "subject" to subject,
                "description" to description,
                "author" to username,
                "latitude" to locationLat,
                "longitude" to locationLng,
                "type" to type,
                "rating" to null,
                "timestamp" to System.currentTimeMillis()
            )

            // Sačuvaj podatke u Firestore i dobij ID
            db.collection("objects").add(objectData)
                .addOnSuccessListener { documentReference ->
                    val objectId = documentReference.id

                    // Ažuriraj dokument sa ID-jem
                    db.collection("objects").document(objectId)
                        .update("id", objectId)
                        .addOnSuccessListener {
                            // Ako postoji slika, upload slike u Firebase Storage
                            imageUri?.let { uri ->
                                val storageRef = storage.reference.child("object_photos/$objectId.jpg")
                                storageRef.putFile(uri)
                                    .addOnSuccessListener {
                                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                            db.collection("objects").document(objectId)
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
                                // Ako nema slike, završi uspešno
                                onResult(true, null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            onResult(false, exception.message)
                        }
                }
                .addOnFailureListener { exception ->
                    onResult(false, exception.message)
                }
        } else {
            onResult(false, "User not authenticated")
        }
    }

    fun getAllObjects(onResult: (List<MapObject>, String?) -> Unit) {
        db.collection("objects")
            .get()
            .addOnSuccessListener { result ->
                val objects = result.documents.mapNotNull { document ->
                    document.toObject(MapObject::class.java)?.copy(id = document.id)
                }
                onResult(objects, null)
            }
            .addOnFailureListener { exception ->
                onResult(emptyList(), exception.message)
            }
    }

    /*fun getAllObjects(onResult: (List<MapObject>, String?) -> Unit) {
        db.collection("objects")
            .get()
            .addOnSuccessListener { result ->
                val objects = mutableListOf<MapObject>()
                for (document in result) {
                    val mapObject = document.toObject(MapObject::class.java)
                    mapObject.id = document.id
                    objects.add(mapObject)
                }
                onResult(objects, null)
            }
            .addOnFailureListener { exception ->
                onResult(emptyList(), exception.message)
            }
    }*/


}
