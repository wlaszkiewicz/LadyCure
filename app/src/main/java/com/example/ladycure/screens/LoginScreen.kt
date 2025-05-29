package com.example.ladycure.screens

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.R
import com.example.ladycure.presentation.login.LoginViewModel
import com.example.ladycure.presentation.login.components.NonWomanWelcomeDialog
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

@Composable
fun LoginScreen(navController: NavController, snackbarHostState: SnackbarController) {
    val authRepo = AuthRepository()
    val viewModel = viewModel { LoginViewModel(authRepo) }
    var infoClicked by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel.error) {
        if (viewModel.error.isNotEmpty()) {
            snackbarHostState.showMessage(
                message = viewModel.error
            )
            viewModel.error = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = DefaultBackground
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_kapi),
                contentDescription = "Capybara background",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(bottom = 30.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(1f),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 220.dp)
                    .padding(vertical = 16.dp)
                    .zIndex(0f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = DefaultOnPrimary
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 30.dp)
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Welcome to LadyCure",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "An app designed for women",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    infoClicked = true
                                },
                            tint = DefaultPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                    )

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Enter Password"
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus() // This will hide the keyboard
                            if (viewModel.isValidInput()) {
                                viewModel.login(navController, snackbarHostState)
                            }
                        })
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.login(navController, snackbarHostState)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = viewModel.isValidInput() && !viewModel.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Don't have an account? \n Register",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary,
                textAlign = TextAlign.Center,
            )
        }
    }

    if(infoClicked) {
        NonWomanWelcomeDialog(
            onContinue = {
                infoClicked = false
                navController.navigate("login")
            },
            onUninstall = {
                val packageName = context.packageName
                val intent = Intent(
                    Intent.ACTION_DELETE,
                    Uri.fromParts("package", packageName, null)
                )
                context.startActivity(intent)
            },
            onDismiss = { infoClicked = false }
        )
    }
}

