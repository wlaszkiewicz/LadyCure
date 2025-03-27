package com.example.ladycure

import LadyCureTheme
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    // Use the default theme colors consistently
    LadyCureTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background

        ) {
            Image(
                painter = painterResource(id = R.drawable.kapibara),
                contentDescription = "Capybara background",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f), // Adjust opacity (0.1f - 0.3f works well for backgrounds)
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = " ",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " ",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " ",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Welcome to LadyCure",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Please login to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val emailState = remember { mutableStateOf("") }
                val passwordState = remember { mutableStateOf("") }

                TextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                        colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                TextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                Button(
                    onClick = {
                        authenticate(emailState.value, passwordState.value, navController)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "Don't have an account? Register",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { navController.navigate("register") }
                )

            }
        }
    }
}

fun authenticate(
    email: String,
    password: String,
    navController: NavController
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance("telecure")

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    firestore.collection("user").document(it.uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role")
                                if (role == "admin") {
                                    navController.navigate("admin")
                                } else {
                                    navController.navigate("home")
                                }
                            } else {
                            }
                        }
                        .addOnFailureListener {
                            // Show error message: Failed to retrieve user data
                        }
                }
            } else {
                // Show error message: Authentication failed
            }
        }
}

@Preview
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LadyCureTheme {  // Removed dynamicColor parameter
        LoginScreen(navController)
    }
}