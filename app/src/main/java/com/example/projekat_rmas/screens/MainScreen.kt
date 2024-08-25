package com.example.projekat_rmas.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.projekat_rmas.components.BottomNavigationBar


@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        content = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)  // Ovdje se padding primenjuje na osnovu sadržaja Scaffold-a
            ) {
                Text(
                    text = "Welcome to the App!",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    )
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Main") },
            label = { Text("Main") },
            selected = false,
            onClick = {
                navController.navigate("main_screen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
            label = { Text("Map") },
            selected = false,
            onClick = {
                navController.navigate("map_screen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "Leaderboard") },
            label = { Text("Leaderboard") },
            selected = false,
            onClick = {
                navController.navigate("leaderboard_screen") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
    }
}





