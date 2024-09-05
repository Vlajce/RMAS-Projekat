package com.example.projekat_rmas.repository

import android.net.Uri
import android.util.Log
import com.example.projekat_rmas.model.MapObject
import com.example.projekat_rmas.model.Rate
import com.example.projekat_rmas.model.User
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
                                        "email" to email,
                                        "points" to 0
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

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        val userDocRef = db.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
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
                "ownerId" to userId,
                "latitude" to locationLat,
                "longitude" to locationLng,
                "type" to type,
                "rating" to 0.0,
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

    fun getObjectById(objectId: String, onResult: (MapObject?) -> Unit) {
        db.collection("objects").document(objectId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val mapObject = document.toObject(MapObject::class.java)
                    onResult(mapObject)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addOrUpdateRating(userId: String, objectId: String, value: Int, onResult: (Boolean) -> Unit) {
        val ratingRef = db.collection("ratings")
            .whereEqualTo("userId", userId)
            .whereEqualTo("objectId", objectId)

        ratingRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // Dodaj novu ocenu ako ne postoji
                val newRating = Rate(userId, objectId, value)
                db.collection("ratings").add(newRating)
                    .addOnSuccessListener {
                        onResult(true)
                    }
                    .addOnFailureListener {
                        onResult(false)
                    }
            } else {
                // Ažuriraj postojeću ocenu
                for (document in documents) {
                    db.collection("ratings").document(document.id)
                        .update("value", value)
                        .addOnSuccessListener {
                            onResult(true)
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
            }
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun getRatingsForObject(objectId: String, onResult: (List<Rate>) -> Unit) {
        db.collection("ratings")
            .whereEqualTo("objectId", objectId)
            .get()
            .addOnSuccessListener { result ->
                val ratings = result.documents.mapNotNull { document ->
                    document.toObject(Rate::class.java)
                }
                onResult(ratings)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getUserRatingForObject(objectId: String, userId: String, onResult: (Int?) -> Unit) {
        db.collection("ratings")
            .whereEqualTo("objectId", objectId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val rating = documents.documents[0].getLong("value")?.toInt()
                    onResult(rating)
                } else {
                    onResult(null) // Korisnik još uvek nije ocenio ovaj objekat
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    fun updateObjectRating(objectId: String, newRating: Float) {
        db.collection("objects").document(objectId)
            .update("rating", newRating)
            .addOnSuccessListener {
                Log.d("FirebaseRepo", "Object rating updated successfully")
            }
            .addOnFailureListener {
                Log.e("FirebaseRepo", "Failed to update object rating")
            }
    }

    fun updateOwnerPoints(ownerId: String, pointsToAdd: Int) {
        val userRef = db.collection("users").document(ownerId)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentPoints = document.getLong("points") ?: 0L
                val newPoints = currentPoints + pointsToAdd
                userRef.update("points", newPoints)
            }
        }
    }

    fun getAllUsers(onResult: (List<User>, String?) -> Unit) {
        db.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { document ->
                    document.toObject(User::class.java)?.copy(id = document.id)
                }
                onResult(users, null)
            }
            .addOnFailureListener { exception ->
                onResult(emptyList(), exception.message)
            }
    }


}
