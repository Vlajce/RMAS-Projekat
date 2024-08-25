package com.example.projekat_rmas.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.projekat_rmas.components.BottomNavigationBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    // Inicijalizacija ActivityResultLauncher-a za traženje dozvola
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Kada je dozvola dodeljena, započni dobijanje lokacije
            startLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    // Ako je mapa spremna, centriraj kameru na trenutnu lokaciju
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

    // Provera da li je dozvola već dodeljena
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Pokrećemo zahtev za dozvolu ako nije dodeljena
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Ako je dozvola već dodeljena, odmah započni dobijanje lokacije
            startLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    // Ako je mapa spremna, centriraj kameru na trenutnu lokaciju
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
        Box(modifier = Modifier.fillMaxSize()) {
            // AndroidView za prikaz mape
            AndroidView(
                factory = { mapView.apply { onCreate(null); onResume() } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                update = { view ->
                    // Inicijalizacija mape
                    view.getMapAsync { map ->
                        googleMap = map
                        isMapReady = true

                        // Omogući dugme za centriranje lokacije
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            googleMap?.isMyLocationEnabled = true
                            googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                        }

                        // Ako je lokacija već dostupna, centriraj kameru na nju
                        currentLocation?.let { location ->
                            val userLocation = LatLng(location.latitude, location.longitude)
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        }
                    }
                },
                // Upravljanje životnim ciklusom mape
                onReset = {
                    mapView.onPause()
                    mapView.onStop()
                    mapView.onDestroy()
                }
            )

            // Prikazivanje greške ako postoji
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
