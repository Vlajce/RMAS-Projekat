package com.example.projekat_rmas.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun AddObjectDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Uri?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedType by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val types = listOf("Private School", "Private Tutor", "Exam Prep Group")

    val context = LocalContext.current

    // Launcher za biranje slike iz galerije
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(
            text = "Add new private school/professor",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        ) },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    maxLines = 1,
                )
                TextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    maxLines = 1,
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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

                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text("Choose Photo")
                }

                // Prikaz slike ako je izabrana
                selectedImageUri?.let {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ){
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(top = 16.dp)
                    )}
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotEmpty() && subject.isNotEmpty() && description.isNotEmpty() && selectedType.isNotEmpty() && selectedImageUri != null) {
                    onSave(title, subject, description, selectedImageUri, selectedType)
                    onDismiss()
                } else {
                    showError = true
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showError) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        ) {
            Text(text = "All fields and photo must be filled!")
        }
    }
}
