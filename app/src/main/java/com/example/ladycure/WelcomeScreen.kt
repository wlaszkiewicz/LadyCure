package com.example.ladycure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to LadyCure",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}