package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import DefaultSecondary
import androidx.compose.foundation.clickable
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
    val db = FirebaseFirestore.getInstance("telecure")
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf(User("", "", "", "", "", "")) }

    // Fetch users from Firestore
    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val userList = mutableListOf<User>()
            snapshot?.documents?.forEach { document ->
                val user = User(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    surname = document.getString("surname") ?: "",
                    email = document.getString("email") ?: "",
                    role = document.getString("role") ?: "user",
                    dob = document.getString("dob") ?: ""
                )
                userList.add(user)
            }
            users = userList
        }
    }

    if (showEditDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit User Data",
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editedUser.name,
                        onValueChange = { editedUser = editedUser.copy(name = it) },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DefaultBackground,
                            unfocusedContainerColor = DefaultBackground,
                            focusedLabelColor = DefaultPrimary,
                            unfocusedLabelColor = DefaultPrimary.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedUser.surname,
                        onValueChange = { editedUser = editedUser.copy(surname = it) },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DefaultBackground,
                            unfocusedContainerColor = DefaultBackground,
                            focusedLabelColor = DefaultPrimary,
                            unfocusedLabelColor = DefaultPrimary.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedUser.email,
                        onValueChange = { editedUser = editedUser.copy(email = it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DefaultBackground,
                            unfocusedContainerColor = DefaultBackground,
                            focusedLabelColor = DefaultPrimary,
                            unfocusedLabelColor = DefaultPrimary.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedUser.dob,
                        onValueChange = { editedUser = editedUser.copy(dob = it) },
                        label = { Text("Date of Birth") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DefaultBackground,
                            unfocusedContainerColor = DefaultBackground,
                            focusedLabelColor = DefaultPrimary,
                            unfocusedLabelColor = DefaultPrimary.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Role:",
                        color = DefaultPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val roles = listOf("user", "doctor", "admin")
                    roles.forEach { role ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { editedUser = editedUser.copy(role = role) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (editedUser.role == role) DefaultPrimary.copy(alpha = 0.2f)
                                else DefaultBackground
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                RadioButton(
                                    selected = editedUser.role == role,
                                    onClick = { editedUser = editedUser.copy(role = role) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = DefaultPrimary
                                    )
                                )
                                Text(
                                    text = role.capitalize(),
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = DefaultOnPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUser?.let { user ->
                            db.collection("users").document(user.id).update(
                                mapOf(
                                    "name" to editedUser.name,
                                    "surname" to editedUser.surname,
                                    "email" to editedUser.email,
                                    "role" to editedUser.role,
                                    "dob" to editedUser.dob
                                )
                            ).addOnSuccessListener {
                                showEditDialog = false
                            }.addOnFailureListener { e ->
                                println("Error updating document: $e")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DefaultPrimary,
                        contentColor = DefaultOnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Cancel",
                        color = DefaultPrimary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = DefaultBackground
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Admin Dashboard",
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DefaultPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DefaultBackground,
                    titleContentColor = DefaultPrimary
                )
            )
        },
        containerColor = DefaultBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            if (users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Loading users...",
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DefaultPrimary.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${user.name} ${user.surname}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DefaultPrimary
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 14.sp,
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "DOB: ${user.dob}",
                                    fontSize = 14.sp,
                                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Role: ${user.role.capitalize()}",
                                    fontSize = 14.sp,
                                    color = when (user.role) {
                                        "admin" -> DefaultPrimary
                                        "doctor" -> DefaultSecondary
                                        else -> DefaultOnPrimary.copy(alpha = 0.6f)
                                    },
                                    fontWeight = when (user.role) {
                                        "admin", "doctor" -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    }
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedUser = user
                                    editedUser = user.copy()
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit user",
                                    tint = DefaultPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
