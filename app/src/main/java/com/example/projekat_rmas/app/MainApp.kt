import android.util.Log
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
import com.example.projekat_rmas.viewmodel.UserViewModel
import com.example.projekat_rmas.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainApp() {

    val navController = rememberNavController()
    val firebaseRepo = FirebaseRepo()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(firebaseRepo)
    )
    val objectViewModel: ObjectViewModel = viewModel(
        factory = ObjectViewModelFactory(firebaseRepo))

    val userViewModel: UserViewModel = viewModel(
        factory =  UserViewModelFactory(firebaseRepo)
    )


    val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

    NavHost(navController = navController, if(isUserLoggedIn) "main_screen" else "login") {
        composable("signup") {
            SignUpScreen(navController, viewModel = authViewModel)
        }
        composable("login") {
            LoginScreen(navController, viewModel = authViewModel)
        }
        composable("main_screen") {
            MainScreen(navController, authViewModel)
        }
        composable("map_screen") {
            MapScreen(navController, objectViewModel)
        }
        composable("leaderboard_screen") {
            LeaderboardScreen(navController, userViewModel)
        }
        composable("table_screen"){
            TableScreen(navController, objectViewModel)
        }
        composable("object_details_screen/{objectId}") { backStackEntry -> //stanje navigacije koje sadrži informacije o trenutnoj ruti, uključujući argumente proslijeđene toj ruti.
            val objectId = backStackEntry.arguments?.getString("objectId") ?: return@composable //kao provera, ako se ne dobiej objectID kod se prekida ali ne dolazi do greske
            ObjectDetailsScreen(navController, objectViewModel,userViewModel, objectId)
        }

    }
}


