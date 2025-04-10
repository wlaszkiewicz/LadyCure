package com.example.ladycure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val role: String,
    val dob: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    // LadyCure color scheme
    val primaryColor = Color(0xFFFF6B8B) // Brand pink
    val surfaceColor = Color(0xFFF8F8F8) // Light background
    val cardColor = Color(0xFFFFFFFF) // White cards
    val textColor = Color(0xFF333333) // Dark text
    val secondaryTextColor = Color(0xFF666666) // Secondary text

    val db = FirebaseFirestore.getInstance("telecure")
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf(User("", "", "", "", "", "")) }

    // Fetch users from Firestore
    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            users = snapshot?.documents?.mapNotNull { document ->
                User(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    surname = document.getString("surname") ?: "",
                    email = document.getString("email") ?: "",
                    role = document.getString("role") ?: "user",
                    dob = document.getString("dob") ?: ""
                )
            } ?: emptyList()
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = editedUser,
            primaryColor = primaryColor,
            onDismiss = { showEditDialog = false },
            onSave = {
                selectedUser?.let { user ->
                    db.collection("users").document(user.id).update(
                        mapOf(
                            "name" to editedUser.name,
                            "surname" to editedUser.surname,
                            "email" to editedUser.email,
                            "role" to editedUser.role,
                            "dob" to editedUser.dob
                        )
                    ).addOnSuccessListener { showEditDialog = false }
                }
            },
            onUserChange = { editedUser = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "User Management",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = surfaceColor
    ) { padding ->
        when {
            users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            cardColor = cardColor,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onEditClick = {
                                selectedUser = user
                                editedUser = user.copy()
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    cardColor: Color,
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${user.name} ${user.surname}",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    color = secondaryTextColor,
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOB: ${user.dob}",
                        color = secondaryTextColor,
                        fontSize = 13.sp
                    )

                    RoleBadge(role = user.role, primaryColor = primaryColor)
                }
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit user",
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String, primaryColor: Color) {
    val (backgroundColor, roleTextColor) = when (role) {
        "admin" -> primaryColor.copy(alpha = 0.2f) to primaryColor
        "doctor" -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
        else -> Color(0xFF2196F3).copy(alpha = 0.2f) to Color(0xFF2196F3)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = when (role) {
                "admin" -> "Admin"
                "doctor" -> "Doctor"
                else -> "User"
            },
            color = roleTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditUserDialog(
    user: User,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUserChange: (User) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit User",
                color = primaryColor,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = user.name,
                    onValueChange = { onUserChange(user.copy(name = it)) },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = user.surname,
                    onValueChange = { onUserChange(user.copy(surname = it)) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = user.email,
                    onValueChange = { onUserChange(user.copy(email = it)) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = user.dob,
                    onValueChange = { onUserChange(user.copy(dob = it)) },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Text("Role", style = MaterialTheme.typography.labelLarge)

                RoleSelection(
                    selectedRole = user.role,
                    primaryColor = primaryColor,
                    onRoleSelected = { onUserChange(user.copy(role = it)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Cancel",
                    color = primaryColor
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFFFFFFF)
    )
}

@Composable
private fun RoleSelection(
    selectedRole: String,
    primaryColor: Color,
    onRoleSelected: (String) -> Unit
) {
    val roles = listOf("user", "doctor", "admin")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        roles.forEach { role ->
            FilterChip(
                selected = selectedRole == role,
                onClick = { onRoleSelected(role) },
                label = {
                    Text(
                        role.capitalize(),
                        color = if (selectedRole == role) Color.White else primaryColor
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = primaryColor
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}