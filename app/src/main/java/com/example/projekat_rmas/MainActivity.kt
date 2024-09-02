package com.example.projekat_rmas

import MainApp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projekat_rmas.service.LocationService
import com.example.projekat_rmas.ui.theme.Projekat_RMASTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Projekat_RMASTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainApp()
                }
            }
        }
    }

    // Rezultat zahteva za dozvole
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var allPermissionsGranted = true
            var deniedPermission: String? = null

            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    deniedPermission = permissions[i]
                    break
                }
            }

            if (allPermissionsGranted) {
                // Sve dozvole su dodeljene
                Log.d("Permissions", "All permissions granted.")
            } else {
                Log.d("Permissions", "Permission denied for: $deniedPermission")
                // Korisnik je odbio neku dozvolu, prikaži Toast
                Toast.makeText(this, "Permissions are required for the app to function correctly.", Toast.LENGTH_SHORT).show()

                // Provera ako korisnik nije označio "Don't ask again"
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermission!!)) {
                    // Nakon 5 sekundi, ponovo prikaži dijalog za dozvolu
                    Handler(Looper.getMainLooper()).postDelayed({
                        ActivityCompat.requestPermissions(this, arrayOf(deniedPermission), REQUEST_CODE_PERMISSIONS)
                    }, 5000)
                } else {
                    // Korisnik je označio "Don't ask again", mora ručno omogućiti dozvolu
                    Toast.makeText(this, "Permission denied permanently, please enable it in app settings.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
}
