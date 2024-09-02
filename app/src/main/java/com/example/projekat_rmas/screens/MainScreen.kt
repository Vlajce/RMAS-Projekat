package com.example.projekat_rmas.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projekat_rmas.MainActivity
import com.example.projekat_rmas.R
import com.example.projekat_rmas.components.BottomNavigationBar
import com.example.projekat_rmas.service.LocationService
import com.example.projekat_rmas.viewmodel.AuthViewModel
import com.example.projekat_rmas.viewmodel.NotificationViewModel
import com.example.projekat_rmas.viewmodel.NotificationViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var permissionsRequested by remember { mutableStateOf(false) }

    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(context)
    )
    val serviceRunningState by notificationViewModel.serviceRunningState.observeAsState(false)

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Provera i zahtev za dozvole kada se uđe u MainScreen
    if (!permissionsRequested) {
        LaunchedEffect(Unit) {
            requestAllPermissions(context)
            permissionsRequested = true
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            notificationViewModel.setNotificationEnabled(false)
            LocationService.stopLocationService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Logout", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            authViewModel.logout(navController)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.logout),
                                contentDescription = "Logout",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.homescreenimage),
                    contentDescription = "School Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(290.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to the app for finding private tutors and schools!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This app helps you find the best private tutors and schools in your area, rate them, and help others make informed decisions.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You can use this button to turn notifications on or off.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Button(
                    onClick = {
                        if (serviceRunningState) {
                            LocationService.stopLocationService(context)
                            notificationViewModel.setNotificationEnabled(enabled = false)
                        } else {
                            // Provera dozvola pre pokretanja servisa
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                LocationService.startLocationService(context)
                                notificationViewModel.setNotificationEnabled(enabled = true)
                            } else {
                                Toast.makeText(context, "Location permission is required to start the service.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (serviceRunningState) Color.Red else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = if (serviceRunningState) "Stop Notification Service" else "Start Notification Service")
                }
            }
        }
    )
}

// Funkcija za proveru i zahtev za sve potrebne dozvole
private fun requestAllPermissions(context: Context) {
    val permissionsNeeded = mutableListOf<String>()

    // Provera i dodavanje dozvole za notifikacije
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Provera i dodavanje dozvole za lokaciju
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Zahtevaj sve potrebne dozvole odjednom
    if (permissionsNeeded.isNotEmpty()) {
        ActivityCompat.requestPermissions(context as MainActivity, permissionsNeeded.toTypedArray(), REQUEST_CODE_PERMISSIONS)
    }
}

private const val REQUEST_CODE_PERMISSIONS = 1001



