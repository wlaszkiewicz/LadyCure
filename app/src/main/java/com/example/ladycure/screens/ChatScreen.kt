package com.example.ladycure.screens

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.presentation.home.components.Screen
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController

@Composable
fun ChatScreen(navController: NavHostController, snackbarController: SnackbarController?) {
    var role by remember { mutableStateOf("") }
    val authRepo = AuthRepository()
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val result = try {
            authRepo.getUserRole()
        } catch (e: Exception) {
            Result.failure(e)
        }
        when {
            result.isSuccess -> role = result.getOrNull() ?: ""
            result.isFailure -> error = result.exceptionOrNull()?.message ?: "Unknown error"
        }
    }

    if (error.isNotEmpty()) {
        snackbarController?.showMessage(error)
    }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
        ) {
            // Header
            Surface(
                color = DefaultBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Chat",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )
                    Text(
                        text = if (role == "user") {"Connect with medical professionals"} else {"Contact your patients"},
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated chat illustration
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = DefaultPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat illustration",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "No active conversations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DefaultOnPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if(role == "user") {"Start a secure chat with a doctor or specialist to discuss your health concerns"} else {""},
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                if (role == "user") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { navController.navigate(Screen.Doctors.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DefaultPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find doctors",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find a Doctor")
                        }

                        Button(
                            onClick = { /* Handle emergency chat */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = DefaultOnPrimary
                            ),
                            border = BorderStroke(1.dp, DefaultPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Urgent Help")
                        }
                    }
                }
            }
        }
    }

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(navController = rememberNavController(), snackbarController = null)
}