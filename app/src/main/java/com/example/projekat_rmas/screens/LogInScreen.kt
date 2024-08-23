package com.example.projekat_rmas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projekat_rmas.R
import com.example.projekat_rmas.components.ButtonComponent
import com.example.projekat_rmas.components.ClickableLoginTextComponent
import com.example.projekat_rmas.components.DividerTextComponent
import com.example.projekat_rmas.components.HeadingTextComponent
import com.example.projekat_rmas.components.NormalTextComponent
import com.example.projekat_rmas.components.PasswordTextFieldComponent
import com.example.projekat_rmas.components.TextFieldComponent

@Composable
fun LoginScreen(navController: NavHostController){
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)
    ) {
        Column {

            Spacer(modifier = Modifier.height(20.dp))
            NormalTextComponent(value = "Hey there")
            HeadingTextComponent(value = "Welcome Back")
            Spacer(modifier = Modifier.height(20.dp))


            TextFieldComponent(labelValue = "Email", painterResource(id = R.drawable.email))
            PasswordTextFieldComponent(labelValue = "Password", painterResource(id = R.drawable.password) )

            Spacer(modifier = Modifier.height(30.dp))
            
            ButtonComponent(value = "Login", onClick = {})

            Spacer(modifier = Modifier.height(10.dp))

            DividerTextComponent()

            ClickableLoginTextComponent (tryingToLogin = false, onTextSelected = {
                navController.navigate("signup")
            })

        }
    }
}

