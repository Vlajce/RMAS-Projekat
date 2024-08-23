import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekat_rmas.screens.LoginScreen
import com.example.projekat_rmas.screens.SignUpScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "signup") {
        composable("signup") {
            SignUpScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
    }
}
