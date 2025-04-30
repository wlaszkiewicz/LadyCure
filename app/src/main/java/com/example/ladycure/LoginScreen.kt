package com.example.ladycure

import DefaultPrimary
import LadyCureTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

@Composable
fun LoginScreen(navController: NavController, snackbarHostState: SnackbarController) {
    val authRepo = AuthRepository()
    LadyCureTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.background
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.80f)
                        .background(color = DefaultPrimary.copy(alpha = 0.5f))
                )
                Image(
                    painter = painterResource(id = R.drawable.login_kapi),
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
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(
                            FocusDirection.Down
                        )
                    }),
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
                        val message = validInput(emailState.value, passwordState.value)
                        if (message == "") {
                            authRepo.authenticate(
                                email = emailState.value,
                                password = passwordState.value,
                                navController = navController,
                                onSuccess = {
                                    snackbarHostState.showMessage(
                                        message = "Login Successful"
                                    )
                                },
                                onFailure = { exception ->
                                    snackbarHostState.showMessage(
                                        message = exception.message ?: "Authentication failed"
                                    )
                                }
                            )
                        } else {
                            snackbarHostState?.showMessage(
                                message = message
                            )
                        }
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                Button(
                    onClick = {
                        val message = validInput(emailState.value, passwordState.value)
                        if (message == "") {
                            authRepo.authenticate(
                                email = emailState.value,
                                password = passwordState.value,
                                navController = navController,
                                onSuccess = {
                                    snackbarHostState.showMessage(
                                        message = "Login Successful"
                                    )
                                },
                                onFailure = { exception ->
                                    snackbarHostState.showMessage(
                                        message = exception.message ?: "Authentication failed"
                                    )
                                }
                            )
                        } else {
                            snackbarHostState.showMessage(
                                message = message
                            )
                        }
                        message == ""
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleMedium
                    )
                }


                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        "Don't have an account?\nRegister",
                        fontSize = 14.sp,
                        style = TextStyle(
                            fontSize = 16.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

            }
        }
    }
}

fun validInput(email: String, password: String): String {
    return when {
        email.isEmpty() -> {
            "Please enter a valid email"
        }

        password.isEmpty() -> {
            "Please enter a valid password"
        }

        else -> ""
    }
}