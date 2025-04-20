package com.example.ladycure

import LadyCureTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore  

@Composable
fun LoginScreen(navController: NavController) {
    LadyCureTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),  // Add scrolling if content is long
        ) {
            Column(modifier = Modifier.fillMaxSize().background(
                color = MaterialTheme.colorScheme.background))  {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.80f)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    )
                    Image(
                        painter = painterResource(id = R.drawable.kapi2),
                        contentDescription = "Capybara background",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(bottom = 30.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background) // This ensures the rest of the screen has the correct color
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
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

                    Spacer(modifier = Modifier.height(30.dp))

                    val emailState = remember { mutableStateOf("") }
                    val passwordState = remember { mutableStateOf("") }
                    val focusManager = LocalFocusManager.current

                    TextField(
                        value = emailState.value,
                        onValueChange = { emailState.value = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(
                            FocusDirection.Down) }),
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
                            .padding(20.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (validInput(emailState.value, passwordState.value)) {
                                authenticate(emailState.value, passwordState.value, navController)
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                    Button(
                        onClick = {
                            if (validInput(emailState.value, passwordState.value)) {
                                authenticate(emailState.value, passwordState.value, navController)
                            }
                        },
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }


                    TextButton (
                        onClick = { navController.navigate("register") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Don't have an account?\nRegister",
                            fontSize = 14.sp,
                            style = TextStyle(
                                // fontFamily = FontFamily.Serif, // Change this to your desired font family
                                //fontWeight = FontWeight.Bold,  // Change this to your desired font weight
                                fontSize = 16.sp              // Change this to your desired font size
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary)
                    }



//                    Image(
//                        painter = painterResource(id = R.drawable.bottom_kapi2),
//                        contentDescription = "App Logo",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp)
//                            .padding(top = 30.dp)
//                            .padding(bottom = 32.dp),
//                        contentScale = ContentScale.Crop
//                    )
                }
            }
        }
    }
}

fun validInput(email: String, password: String): Boolean {
    return when {
        email.isEmpty() -> {
            // Show snackbar or toast: "Email is empty"
            false
        }
        password.isEmpty() -> {
            // Show snackbar or toast: "Password is empty"
            false
        }
        else -> true
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
                    firestore.collection("users").document(it.uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role")
                                if (role == "admin") {
                                    navController.navigate("admin")
                                } else {
                                    navController.navigate("home")
                                }
                            } else {
                                // Show error message: User data not found
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
    LadyCureTheme {
        LoginScreen(navController)
    }
}