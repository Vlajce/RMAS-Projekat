import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekat_rmas.repository.FirebaseRepo
import com.example.projekat_rmas.screens.LeaderboardScreen
import com.example.projekat_rmas.screens.LoginScreen
import com.example.projekat_rmas.screens.MainScreen
import com.example.projekat_rmas.screens.MapScreen
import com.example.projekat_rmas.screens.ObjectDetailsScreen
import com.example.projekat_rmas.screens.SignUpScreen
import com.example.projekat_rmas.screens.TableScreen
import com.example.projekat_rmas.viewmodel.AuthViewModel
import com.example.projekat_rmas.viewmodel.AuthViewModelFactory
import com.example.projekat_rmas.viewmodel.ObjectViewModel
import com.example.projekat_rmas.viewmodel.ObjectViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val authRepository = FirebaseRepo()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )
    val objectViewModel: ObjectViewModel = viewModel(
        factory = ObjectViewModelFactory(FirebaseRepo()))


    val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(navController = navController, if(isUserLoggedIn) "main_screen" else "login") {
        composable("signup") {
            SignUpScreen(navController, viewModel = authViewModel)
        }
        composable("login") {
            LoginScreen(navController, viewModel = authViewModel)
        }
        composable("main_screen") {
            MainScreen(navController, viewModel = authViewModel)
        }
        composable("map_screen") {
            MapScreen(navController, objectViewModel)
        }
        composable("leaderboard_screen") {
            LeaderboardScreen(navController)
        }
        composable("table_screen"){
            TableScreen(navController)
        }
        composable("object_details_screen/{objectId}") { backStackEntry ->
            val objectId = backStackEntry.arguments?.getString("objectId") ?: return@composable
            ObjectDetailsScreen(navController, objectViewModel, objectId)
        }

    }
}


