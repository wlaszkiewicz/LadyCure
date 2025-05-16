package com.example.ladycure.screens

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import LadyCureTheme
import YellowOrange
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.R
import com.example.ladycure.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val authRepo = AuthRepository()

    LaunchedEffect(Unit) {
        // Check auth state asynchronously
        Firebase.auth.addAuthStateListener { auth ->
            isLoggedIn = auth.currentUser != null
            isLoading = false
        }
    }

    LoadingScreen(isLoading = isLoading)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val result = try {
                authRepo.getUserRole()
            } catch (e: Exception) {
                Result.failure(e)
            }

            when {
                result.isSuccess -> {
                    val role = result.getOrNull()
                    when (role) {
                        "admin" -> navController.navigate("admin") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }

                        "doctor" -> navController.navigate("doctor_main") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }

                        else -> navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }

                else -> {
                    Toast.makeText(
                        context,
                        result.exceptionOrNull()?.message ?: "Error fetching user role",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    if (!isLoading && !isLoggedIn) {
        GenderSelectionScreen(navController)
    }
}

@Composable
fun LoadingScreen(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

sealed class GenderSelection {
    object Woman : GenderSelection()
    object Man : GenderSelection()
    object OtherOrPreferNotToSay : GenderSelection()
}


@Composable
fun GenderSelectionScreen(navController: NavController) {
    var genderSelected by remember { mutableStateOf<GenderSelection?>(null) }
    var showNonWomanDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LadyCureTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Welcome to LadyCure!",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "If you feel comfortable telling us, please select:",
                modifier = Modifier.padding(bottom = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GenderOptionCard(
                    icon = Icons.Default.Woman,
                    title = "Woman",
                    description = "Primary user experience",
                    onClick = {
                        genderSelected = GenderSelection.Woman
                        showNonWomanDialog = false

                    },
                    backgroundColor = Color(0xFFFFF0F5),
                    color = DefaultPrimary,
                )

                GenderOptionCard(
                    icon = Icons.Default.RestoreFromTrash,
                    title = "Man",
                    description = "Limited functionality",
                    onClick = {
                        genderSelected = GenderSelection.Man
                        showNonWomanDialog = true

                    },
                    backgroundColor = Color(0xFFF0F8FF),
                    color = BabyBlue,
                )

                GenderOptionCard(
                    icon = Icons.Default.Star,
                    title = "Other",
                    description = "Custom experience",
                    onClick = {
                        genderSelected = GenderSelection.OtherOrPreferNotToSay
                        showNonWomanDialog = true

                    },
                    backgroundColor = Color(0xFFFAFAD2),
                    color = YellowOrange,
                )
            }

            // Subtle "prefer not to say" option
            TextButton(
                onClick = {
                    genderSelected = GenderSelection.OtherOrPreferNotToSay
                    showNonWomanDialog = true
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "I prefer not to say",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
    when (genderSelected) {
        GenderSelection.Woman -> navController.navigate("login")
        GenderSelection.Man,
        GenderSelection.OtherOrPreferNotToSay -> {
            if (showNonWomanDialog) {
                NonWomanWelcomeDialog(
                    onContinue = {
                        showNonWomanDialog = false
                        navController.navigate("login")
                    },
                    onUninstall = {
                        val packageName = context.packageName
                        val intent = Intent(
                            Intent.ACTION_DELETE,
                            Uri.fromParts("package", packageName, null)
                        )
                        context.startActivity(intent)
                        showNonWomanDialog = false
                    },
                    onDismiss = {
                        showNonWomanDialog = false
                    },
                )
            }
        }

        null -> Unit

    }

}

@Composable
fun NonWomanWelcomeDialog(
    onContinue: () -> Unit,
    onUninstall: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, title = {
            Text(
                text = "Maybe not for you?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.akczuali_kapi),
                        contentDescription = "Welcome illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .zIndex(1f)
                            .height(170.dp)
                            .offset(y = (0).dp),
                        contentScale = ContentScale.Fit
                    )

                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .padding(top = 170.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text =
                                "Our app is designed primarily for women's health needs. " +
                                        "However, if you still find our services helpful, you're welcome to continue.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier.width(200.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Continue to App")
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Continue",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onUninstall,
                    modifier = Modifier.width(200.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DefaultPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Not for me")
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Not for me",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        modifier = Modifier.wrapContentSize()
    )
}

//@Composable
//fun LoginRegisterScreen(navController: NavController) {
//    LadyCureTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.icon),
//                    contentDescription = "App Logo",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 30.dp)
//                        .padding(bottom = 32.dp),
//                    contentScale = ContentScale.FillWidth
//                )
//                Button(
//                    onClick = { navController.navigate("login") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 32.dp)
//                        .height(55.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Text(
//                        text = "Login",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    )
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedButton(
//                    onClick = { navController.navigate("register") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 32.dp)
//                        .height(55.dp),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    Text(
//                        text = "Register",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun GenderOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    color: Color

) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color.copy(alpha = 0.9f)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    LadyCureTheme {
        WelcomeScreen(navController)
    }
}
