import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekat_rmas.repository.AuthRepository
import com.example.projekat_rmas.screens.LoginScreen
import com.example.projekat_rmas.screens.MainScreen
import com.example.projekat_rmas.screens.SignUpScreen
import com.example.projekat_rmas.viewmodel.AuthViewModel
import com.example.projekat_rmas.viewmodel.AuthViewModelFactory

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    NavHost(navController = navController, startDestination = "signup") {
        composable("signup") {
            SignUpScreen(navController, viewModel = viewModel)
        }
        composable("login") {
            LoginScreen(navController, viewModel = viewModel)
        }
        composable("mainScreen") {
            MainScreen()
        }
    }
}

