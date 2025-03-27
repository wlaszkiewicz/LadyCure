package com.example.ladycure

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.wear.compose.material3.Text

@Composable
fun AdminScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color(0xFF000000)) {
        Image(
            painter = painterResource(id = R.drawable.admin),
            contentDescription = "Admin",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("YOU ARE AN ADMIN")
        }
    }

}