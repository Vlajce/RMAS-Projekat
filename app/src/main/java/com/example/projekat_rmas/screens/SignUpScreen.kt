package com.example.projekat_rmas.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun SignUpScreen(navController: NavHostController) {

    // Stanje za odabranu sliku
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Kreiramo launcher za biranje slike iz galerije
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Dobijamo URI odabrane slike
            selectedImageUri = result.data?.data
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)
    ){
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(20.dp))
            NormalTextComponent(value = "Hey there")
            HeadingTextComponent(value = "Create an account")
            Spacer(modifier = Modifier.height(20.dp))
            TextFieldComponent(
                labelValue = "Username",
                painterResource(id = R.drawable.profile)

            )
            TextFieldComponent(
                labelValue = "Fullname",
                painterResource = painterResource(id = R.drawable.profile)
            )
            TextFieldComponent(
                labelValue = "Email",
                painterResource = painterResource(id = R.drawable.email)
            )
            TextFieldComponent(
                labelValue = "Phone",
                painterResource = painterResource(id = R.drawable.phone)
            )
            PasswordTextFieldComponent(
                labelValue = "Password",
                painterResource = painterResource(id = R.drawable.password)
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
                // Ovde možeš dodati akciju za registraciju
                // Ako trenutno nema akcije, ostavi praznu lambdu
            })

            Spacer(modifier = Modifier.height(20.dp))

            DividerTextComponent()

            ClickableLoginTextComponent (tryingToLogin = true, onTextSelected = {
                navController.navigate("login")
            })


        }
    }
}
