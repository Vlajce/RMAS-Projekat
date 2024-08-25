package com.example.projekat_rmas.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.projekat_rmas.R
import com.example.projekat_rmas.components.ButtonComponent
import com.example.projekat_rmas.components.ClickableLoginTextComponent
import com.example.projekat_rmas.components.DividerTextComponent
import com.example.projekat_rmas.components.HeadingTextComponent
import com.example.projekat_rmas.components.TextFieldComponent
import com.example.projekat_rmas.components.NormalTextComponent
import com.example.projekat_rmas.components.PasswordTextFieldComponent
import com.example.projekat_rmas.viewmodel.AuthViewModel
import com.example.projekat_rmas.viewmodel.RegistrationState

@Composable
fun SignUpScreen(navController: NavHostController, viewModel: AuthViewModel) {

    var username by remember { mutableStateOf("")}
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val registrationState = viewModel.registrationState
    val context = LocalContext.current

    // Kreiramo launcher za biranje slike iz galerije
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Dobijamo URI odabrane slike
            selectedImageUri = result.data?.data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(20.dp))
            NormalTextComponent(value = "Hey there")
            HeadingTextComponent(value = "Create an account")
            Spacer(modifier = Modifier.height(20.dp))

            TextFieldComponent(
                labelValue = "Username",
                painterResource(id = R.drawable.profile),
                value = username,
                onValueChange = {username = it}
            )
            TextFieldComponent(
                labelValue = "Fullname",
                painterResource = painterResource(id = R.drawable.profile),
                value = fullname,
                onValueChange = {fullname = it}
            )
            TextFieldComponent(
                labelValue = "Email",
                painterResource = painterResource(id = R.drawable.email),
                value = email,
                onValueChange = { email = it }
            )

            TextFieldComponent(
                labelValue = "Phone",
                painterResource = painterResource(id = R.drawable.phone),
                value = phoneNumber,
                onValueChange = {phoneNumber = it}
            )
            PasswordTextFieldComponent(
                labelValue = "Password",
                painterResource = painterResource(id = R.drawable.password) ,
                value = password,
                onValueChange = {password = it}
            )

            Spacer(modifier = Modifier.height(20.dp))

            ButtonComponent(value = "Choose Photo") {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intent)
            }
            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ButtonComponent(value = "Register", onClick = {
                val usernameError = viewModel.validateUsername(username)
                val fullnameError = viewModel.validateFullName(fullname)
                val phoneNumberError = viewModel.validatePhoneNumber(phoneNumber)
                val emailError = viewModel.validateEmail(email)
                val passwordError = viewModel.validatePassword(password)
                val imageError = viewModel.validateImage(selectedImageUri)

                when {
                    usernameError != null -> {
                        Toast.makeText(context, usernameError, Toast.LENGTH_SHORT).show()
                    }
                    fullnameError != null -> {
                        Toast.makeText(context, fullnameError, Toast.LENGTH_SHORT).show()
                    }
                    phoneNumberError != null -> {
                        Toast.makeText(context, phoneNumberError, Toast.LENGTH_SHORT).show()
                    }
                    emailError != null -> {
                        Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
                    }
                    passwordError != null -> {
                        Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
                    }
                    imageError != null -> {
                        Toast.makeText(context, imageError, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Sve validacije su prošle, pokreće se registracija
                        viewModel.registerUser(username, email, password,fullname, phoneNumber, selectedImageUri)
                    }
                }
            })

            when (registrationState) {
                is RegistrationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CircularProgressIndicator()
                    }
                }
                is RegistrationState.Success -> {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("main_screen") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                }
                is RegistrationState.Error -> {
                    val errorMessage = (registrationState as RegistrationState.Error).message
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> Unit
            }

            Spacer(modifier = Modifier.height(20.dp))
            DividerTextComponent()

            ClickableLoginTextComponent (tryingToLogin = true, onTextSelected = {
                navController.navigate("login")
            })


        }
    }
}
