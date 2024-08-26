package com.example.projekat_rmas.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun TableScreen(navController: NavHostController){
    Scaffold(
        bottomBar = { com.example.projekat_rmas.components.BottomNavigationBar(navController) }
    ) { paddingValues ->  // Prosleđujemo padding values
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)  // Koristimo padding da izbegnemo preklapanje sa BottomNavigationBar
        ) {
            Text(
                text = "This is the Table Screen",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}