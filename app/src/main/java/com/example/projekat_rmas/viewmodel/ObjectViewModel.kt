    package com.example.projekat_rmas.viewmodel

    import android.net.Uri
    import android.util.Log
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.setValue
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.projekat_rmas.model.Filters
    import com.example.projekat_rmas.repository.FirebaseRepo
    import kotlinx.coroutines.launch
    import com.example.projekat_rmas.model.MapObject
    import com.google.android.gms.maps.model.LatLng
    import com.google.firebase.auth.FirebaseAuth


    class ObjectViewModel(private val firebaseRepo: FirebaseRepo) : ViewModel() {

        var objectState by mutableStateOf<ObjectState>(ObjectState.Idle)
            private set

        private var allObjects: List<MapObject> = listOf()

        var currentFilters by mutableStateOf(Filters())
            private set



        fun addObject(
            title: String,
            subject: String,
            description: String,
            locationLat: Double,
            locationLng: Double,
            imageUri: Uri?,
            type: String,
        ) {
            objectState = ObjectState.Loading
            viewModelScope.launch {
                firebaseRepo.addObject(title, subject, description, locationLat, locationLng, imageUri, type) { success, message ->
                    if (success) {
                        fetchAllObjects()
                        objectState = ObjectState.Success
                    } else {
                        objectState = ObjectState.Error(message ?: "Failed to add object")
                    }
                }
            }
        }

        /*fun resetState() {
            objectState = ObjectState.Idle
        }*/

        fun fetchAllObjects() {
            objectState = ObjectState.Loading
            firebaseRepo.getAllObjects { objects, error ->
                if (error == null) {
                    allObjects = objects
                    objectState = ObjectState.ObjectsFetched(objects)
                    Log.d("Svi objekti nakon fetcha", "${allObjects}")
                } else {
                    objectState = ObjectState.Error(error)
                }
            }
        }

        fun getObjectById(objectId: String, onResult: (MapObject?) -> Unit) {
            firebaseRepo.getObjectById(objectId) { mapObject ->
                onResult(mapObject)
            }
        }

        fun applyFilters(author: String, type: String, subject: String, rating: Float, startDate: Long?, endDate: Long?, radius: Float, userLocation: LatLng) {
            currentFilters = Filters(
                author = author,
                type = type,
                subject = subject,
                rating = rating.toInt(),
                startDate = startDate,
                endDate = endDate,
                radius = radius
            )

            val filteredObjects = allObjects.filter { obj ->
                        (author.isEmpty() || obj.author.equals(author, ignoreCase = true)) &&
                        (type.isEmpty() || obj.type == type) &&
                        (subject.isEmpty() || obj.subject == subject) &&
                        (rating == 0f || (obj.rating ?: 0f) >= rating) &&
                        (startDate == null || obj.timestamp >= startDate) &&
                        (endDate == null || obj.timestamp <= endDate) &&
                        (radius == 0f || calculateDistance(obj.latitude, obj.longitude, userLocation.latitude, userLocation.longitude) <= radius)
            }
            objectState = ObjectState.ObjectsFetched(filteredObjects)
        }


        fun clearFilters() {
            objectState = ObjectState.ObjectsFetched(allObjects)
        }

        fun resetFilterValues() {
            currentFilters = Filters() // Resetovanje filter vrednosti na podrazumevane vrednosti
        }


        private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
            return results[0] / 1000 // Vraća rezultat u kilometrima
        }

        fun rateObject(objectId: String, value: Int, onResult: (Boolean, String?, Int) -> Unit) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                firebaseRepo.getUserRatingForObject(objectId, userId) { oldRating ->
                    firebaseRepo.addOrUpdateRating(userId, objectId, value) { success ->
                        if (success) {
                            refreshObjectRating(objectId) { updatedRating ->
                                firebaseRepo.getObjectById(objectId) { mapObject ->
                                    mapObject?.let {
                                        val ownerId = it.ownerId
                                        val difference = value - (oldRating ?: 0)
                                        onResult(true, ownerId, difference) // Prosledimo ownerId i razliku u oceni
                                    } ?: onResult(false, null, 0)
                                }
                            }
                        } else {
                            onResult(false, null, 0)
                        }
                    }
                }
            } else {
                onResult(false, null, 0)
            }
        }



        private fun refreshObjectRating(objectId: String, onResult: (Float) -> Unit) {
            firebaseRepo.getRatingsForObject(objectId) { ratings ->
                val averageRating = ratings.map { it.value }.average().toFloat()
                firebaseRepo.updateObjectRating(objectId, averageRating)
                onResult(averageRating) // Vraca azuriranu prosecnu ocenu objekta
            }
        }

    }

    // Definisanje različitih stanja za dodavanje i pirbavljanje objekata
    sealed class ObjectState {
        object Idle : ObjectState()
        object Loading : ObjectState()
        object Success : ObjectState()
        data class ObjectsFetched(val objects: List<MapObject>) : ObjectState()
        data class Error(val message: String) : ObjectState()
    }



