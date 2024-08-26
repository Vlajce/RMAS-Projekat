package com.example.projekat_rmas.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projekat_rmas.components.BottomNavigationBar
import com.example.projekat_rmas.repository.FirebaseRepo
import com.example.projekat_rmas.viewmodel.ObjectState
import com.example.projekat_rmas.viewmodel.ObjectViewModel
import com.example.projekat_rmas.viewmodel.ObjectViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen(
    navController: NavHostController,
    objectViewModel: ObjectViewModel = viewModel(factory = ObjectViewModelFactory(FirebaseRepo()))
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    var showAddObjectDialog by remember { mutableStateOf(false) }

    // Pokretanje dohvaćanja objekata na početku
    LaunchedEffect(Unit) {
        objectViewModel.fetchAllObjects()
    }

    // Inicijalizacija ActivityResultLauncher-a za traženje dozvola
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    if (isMapReady) {
                        googleMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude), 15f
                            )
                        )
                        googleMap?.isMyLocationEnabled = true
                        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                    }
                } else {
                    locationErrorMessage = "Unable to get location."
                }
            }
        } else {
            locationErrorMessage = "Location permission denied."
        }
    }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    if (isMapReady) {
                        googleMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude), 15f
                            )
                        )
                        googleMap?.isMyLocationEnabled = true
                        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
            contentAlignment = Alignment.TopCenter

        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Dugmići za prikaz filtera i dodavanje objekta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp), // Odvojenost od ivica
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { /* Logika za prikaz filtera */ },
                        modifier = Modifier
                            .weight(1f) // Daje jednak prostor svakom dugmetu
                            .padding(end = 8.dp) // Padding između dugmića
                            .height(56.dp) // Visina dugmeta
                    ) {
                        Text("Show Filters")
                    }

                    Button(
                        onClick = { showAddObjectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(56.dp)
                    ) {
                        Text("Add Object")
                    }
                }

                // AndroidView za prikaz mape ispod dugmića
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { mapView.apply { onCreate(null); onResume() } },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            view.getMapAsync { map ->
                                googleMap = map
                                isMapReady = true

                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    googleMap?.isMyLocationEnabled = true
                                    googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                                }

                                currentLocation?.let { location ->
                                    val userLocation = LatLng(location.latitude, location.longitude)
                                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                                }

                                // Prikazivanje markera za sve objekte na mapi
                                if (objectViewModel.objectState is ObjectState.ObjectsFetched) {
                                    val objects = (objectViewModel.objectState as ObjectState.ObjectsFetched).objects
                                    objects.forEach { mapObject ->
                                        val objectLocation = LatLng(mapObject.latitude, mapObject.longitude)
                                        googleMap?.addMarker(MarkerOptions().position(objectLocation).title(mapObject.title))
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Prikazivanje dijaloga za dodavanje objekta
            if (showAddObjectDialog) {
                AddObjectDialog(
                    onDismiss = { showAddObjectDialog = false },
                    onSave = { title, subject, description, selectedImageUri ->
                        currentLocation?.let { location ->
                            objectViewModel.addObject(
                                title = title,
                                subject = subject,
                                description = description,
                                locationLat = location.latitude,
                                locationLng = location.longitude,
                                imageUri = selectedImageUri
                            )
                            showAddObjectDialog = false
                        }
                    }
                )
            }

            when (val state = objectViewModel.objectState) {
                is ObjectState.Loading -> {
                    // Prikaz loading indikatora
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ObjectState.Success -> {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { objectViewModel.resetState() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(text = "Object added successfully!")
                    }
                }
                is ObjectState.Error -> {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = {
                            TextButton(onClick = { objectViewModel.resetState() }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(text = state.message)
                    }
                }
                else -> {}
            }

            locationErrorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = message)
                }
            }
        }
    }
}



// Funkcija za dobijanje trenutne lokacije
private fun startLocationUpdates(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    // Proveravamo da li aplikacija ima dozvolu za pristup lokaciji
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (hasLocationPermission) {
        // Ako je dozvola data, zatraži poslednju poznatu lokaciju
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationReceived(location)
        }
    } else {
        // Ako nema dozvolu, prosledi `null`
        onLocationReceived(null)
    }
}
