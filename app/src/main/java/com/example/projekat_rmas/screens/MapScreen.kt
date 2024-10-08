package com.example.projekat_rmas.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projekat_rmas.components.BottomNavigationBar
import com.example.projekat_rmas.repository.FirebaseRepo
import com.example.projekat_rmas.ui.theme.BgColor
import com.example.projekat_rmas.ui.theme.Primary
import com.example.projekat_rmas.viewmodel.ObjectState
import com.example.projekat_rmas.viewmodel.ObjectViewModel
import com.example.projekat_rmas.viewmodel.ObjectViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MapScreen(
    navController: NavHostController,
    objectViewModel: ObjectViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    var showAddObjectDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var showMarkerDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    var isCameraMovedManually by remember { mutableStateOf(false) }

    // Pokretanje dohvaćanja objekata na početku
    LaunchedEffect(Unit) {
        objectViewModel.fetchAllObjects()
    }

    // Inicijalizacija ActivityResultLauncher-a za traženje dozvola
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {//true ako korisnik prihvati koriscenje lokacije
            startContinuousLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    if (isMapReady && !isCameraMovedManually) {
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

    //proverava se da li aplikacija vec ima dozvolu
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    //IZVRSAVA SE SVAKI PUT KADA SE hasLocPerm PROMENI!
    //proveravamo da li aplikacija ima dozvolu za pracenje lokacije
    //ako nema trazimo dozvolu preko locPermLauncher-a(gornja fja)
    //ako ima, pokrece se fukncija startLocUpd, koja trazi poslednu poznatu lok
    //ako se lokacij uspesno dobije, kamera se pomera na tr lok korisnika
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startContinuousLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    if (isMapReady && !isCameraMovedManually) {
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
                        onClick = { showFilters = !showFilters},
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(56.dp)
                    ) {
                        Text(if (showFilters) "Hide Filters" else "Show Filters")
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
                if (showFilters) {
                    FilterSection(
                        objectViewModel = objectViewModel,
                        onApplyFilters = { author, type, subject, rating, startDate, endDate, radius ->
                            currentLocation?.let { location ->
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                objectViewModel.applyFilters(author, type, subject, rating, startDate, endDate, radius, userLatLng)
                            }
                            showFilters = false
                        },
                        onClearFilters = {
                            objectViewModel.clearFilters()
                            showFilters = false
                        }
                    )
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

                                //Listener za ručno pomeranje kamere
                                googleMap?.setOnCameraMoveStartedListener { reason ->
                                    if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                                        isCameraMovedManually = true
                                    }
                                }

                                googleMap?.uiSettings?.isZoomControlsEnabled = true
                                googleMap?.uiSettings?.isScrollGesturesEnabled = true
                                googleMap?.uiSettings?.isZoomGesturesEnabled = true
                                googleMap?.uiSettings?.isTiltGesturesEnabled = true
                                googleMap?.uiSettings?.isRotateGesturesEnabled = true
                                googleMap?.uiSettings?.isMyLocationButtonEnabled = true

                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    googleMap?.isMyLocationEnabled = true
                                    googleMap?.uiSettings?.isMyLocationButtonEnabled = true
                                }

                                // Ovde je logika za postavljanje markera
                                if (objectViewModel.objectState is ObjectState.ObjectsFetched) {
                                    googleMap?.clear() // Očistimo prethodne markere pre dodavanja novih
                                    val objects = (objectViewModel.objectState as ObjectState.ObjectsFetched).objects
                                    objects.forEach { mapObject ->
                                        val objectLocation = LatLng(mapObject.latitude, mapObject.longitude)
                                        googleMap?.addMarker(MarkerOptions().position(objectLocation).title(mapObject.title))?.tag = mapObject.id

                                    }
                                }

                                googleMap?.setOnMarkerClickListener { marker ->
                                    val objectId = marker.tag as? String
                                    val title = marker.title
                                    if (objectId != null && title != null) {
                                        showMarkerDialog = title to objectId
                                    }
                                    true
                                }


                                //Ako bismo zeleli da se kamera vrati na korisnikovu lokaciju nakon neke promene na mapi,
                                //npr. odlutamo na mapi, uvedemo filtere i ovo ce omoguciti da se korisnikova lokacija vrati u fokus.
                                currentLocation?.let { location ->
                                    if (!isCameraMovedManually) {
                                        val userLocation = LatLng(location.latitude, location.longitude)
                                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                                    }
                                }
                            }
                        },
                        onReset = {
                            mapView.onPause()
                            mapView.onStop()
                            mapView.onDestroy()
                        }
                    )

                    when (val state = objectViewModel.objectState) {
                        is ObjectState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is ObjectState.Error -> {
                            Snackbar(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                Text(text = state.message)
                            }
                        }
                        else -> {}
                    }
                }
            }

            if (showMarkerDialog != null) {
                val (title, objectId) = showMarkerDialog!!
                AlertDialog(
                    onDismissRequest = { showMarkerDialog = null },
                    title = { Text(text = title) },
                    confirmButton = {
                        TextButton(onClick = {
                            navController.navigate("object_details_screen/$objectId")
                            showMarkerDialog = null
                        }) {
                            Text("View Details", style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMarkerDialog = null }) {
                            Text("Close", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                )
            }

            // Prikazivanje dijaloga za dodavanje objekta
            if (showAddObjectDialog) {
                AddObjectDialog(
                    onDismiss = { showAddObjectDialog = false },
                    onSave = { title, subject, description, selectedImageUri, type ->
                        currentLocation?.let { location ->
                            objectViewModel.addObject(
                                title = title,
                                subject = subject,
                                description = description,
                                locationLat = location.latitude,
                                locationLng = location.longitude,
                                imageUri = selectedImageUri,
                                type = type
                            )
                            showAddObjectDialog = false
                        }
                    }
                )
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

// Funkcija za dobijanje trenutne lokacije na po 10secs
fun startContinuousLocationUpdates(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 5000  // Interval za dobijanje lokacije u milisekundama (npr. svakih 10 sekundi)
        fastestInterval = 2000  // Najkraći interval između dva uzastopna ažuriranja
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                onLocationReceived(location)
            }
        }
    }

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    objectViewModel: ObjectViewModel,
    onApplyFilters: (String, String, String, Float, Long?, Long?, Float) -> Unit,
    onClearFilters: () -> Unit
) {
    var author by remember { mutableStateOf(objectViewModel.currentFilters.author) }
    var selectedType by remember { mutableStateOf(objectViewModel.currentFilters.type) }
    var subject by remember { mutableStateOf(objectViewModel.currentFilters.subject) }
    var rating by remember { mutableStateOf(objectViewModel.currentFilters.rating.toFloat()) }
    var startDate by remember { mutableStateOf(objectViewModel.currentFilters.startDate) }
    var endDate by remember { mutableStateOf(objectViewModel.currentFilters.endDate) }
    var radius by remember { mutableStateOf(objectViewModel.currentFilters.radius) }


    var expanded by remember { mutableStateOf(false)}
    val types = listOf("Private School", "Private Tutor", "Exam Prep Group")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author(username)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            maxLines = 1,
            )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            maxLines = 1,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { expanded = true }) {
                Text(text = if (selectedType.isNotEmpty()) selectedType else "Select Type")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(text = type) },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Text(text = "Rating: $rating")
        Slider(value = rating, onValueChange = { rating = it }, valueRange = 1f..10f, steps = 9)

        Spacer(modifier = Modifier.height(8.dp))
        DatePicker(
            label = "Start Date",
            selectedDate = startDate,
            onDateChange = { startDate = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatePicker(
            label = "End Date",
            selectedDate = endDate,
            onDateChange = { endDate = it }
        )

        Text(text = "Radius: $radius km")
        Slider(value = radius, onValueChange = { radius = it }, valueRange = 0f..50f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { objectViewModel.resetFilterValues() // Resetovanje filtera u ViewModel-u
                author = "" // Resetovanje lokalnih vrednosti
                selectedType = ""
                subject = ""
                rating = 0f
                startDate = null
                endDate = null
                radius = 0.0f

                onClearFilters() }) {
                Text("Clear Filters")
            }
            Button(onClick = { onApplyFilters(author, selectedType, subject, rating, startDate, endDate, radius) }) {
                Text("Apply Filters")
            }
        }
    }
}

@Composable
fun DatePicker(
    label: String,
    selectedDate: Long?,
    onDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateChange(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    OutlinedButton(onClick = { datePickerDialog.show() }) {
        Text(text = if (selectedDate != null) dateFormat.format(Date(selectedDate)) else label)
    }
}

