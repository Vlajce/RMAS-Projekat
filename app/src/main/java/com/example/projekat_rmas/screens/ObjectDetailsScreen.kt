package com.example.projekat_rmas.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberImagePainter
import com.example.projekat_rmas.model.MapObject
import com.example.projekat_rmas.viewmodel.ObjectViewModel
import com.example.projekat_rmas.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetailsScreen(navController: NavHostController, objectViewModel: ObjectViewModel, userViewModel: UserViewModel, objectId: String) {
    var mapObject by remember { mutableStateOf<MapObject?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var rating by remember { mutableStateOf(0f) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingResult by remember { mutableStateOf<String?>(null) }
    var buttonText by remember { mutableStateOf("Leave a Rating") }
    val coroutineScope = rememberCoroutineScope()

    // Učitaj objekat na početku i svaki put kad se promeni nesto u vezi objekta
    LaunchedEffect(mapObject) {
        if (mapObject == null) {
            objectViewModel.getObjectById(objectId) { mapObjectResult ->
                mapObject = mapObjectResult
                mapObjectResult?.let {
                    userViewModel.getUserRatingForObject(it.id) { userRating ->
                        if (userRating != null) {
                            rating = userRating.toFloat()
                            buttonText = "Update your rate"
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = mapObject?.type ?: "Type",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 16.dp),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (mapObject != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                mapObject?.photoUrl?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = mapObject?.title ?: "Title",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Subject:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = mapObject?.subject ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = mapObject?.description ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Average Rating: ${mapObject?.rating ?: "No ratings yet"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Provera da li je trenutni korisnik vlasnik objekta
                if (mapObject?.ownerId != currentUser?.uid) {
                    Button(
                        onClick = {
                            showRatingDialog = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(buttonText)
                    }
                } else {
                    Text(
                        text = "You cannot rate your own object.",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                ratingResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = result,
                        color = if (result.contains("Success")) Color.Green else Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Automatsko sakrivanje poruke nakon 3 sekunde
                    coroutineScope.launch {
                        delay(3000)
                        ratingResult = null
                    }
                }

            }
        } else {
            Text("Object not found", color = Color.Red, modifier = Modifier.fillMaxSize())
        }
        if (showRatingDialog) {
            AlertDialog(
                onDismissRequest = { showRatingDialog = false },
                title = { Text("Rate ${mapObject?.title}") },
                text = {
                    Column {
                        Text("Rate from 1 to 10:")
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Current Rating: ${rating.toInt()}", style = MaterialTheme.typography.bodyLarge)
                        Slider(
                            value = rating,
                            onValueChange = { rating = it },
                            valueRange = 1f..10f,
                            steps = 9
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        mapObject?.let {
                            objectViewModel.rateObject(it.id, rating.toInt()) { success, ownerId, difference ->
                                if (success) {
                                    ratingResult = "Rating Submitted Successfully"
                                    objectViewModel.getObjectById(objectId) { updatedObject ->
                                        if (updatedObject != null) {
                                            mapObject = updatedObject // Ovde osvežavamo objekat sa novom prosecnom ocenom
                                        }
                                    }
                                    // Ako je uspešno ocenjivanje, ažuriraj poene vlasnika objekta
                                    ownerId?.let { id ->
                                        userViewModel.updateOwnerPoints(id, difference)
                                    }
                                } else {
                                    ratingResult = "Failed to Submit Rating"
                                }
                                showRatingDialog = false
                            }
                        }
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRatingDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}




