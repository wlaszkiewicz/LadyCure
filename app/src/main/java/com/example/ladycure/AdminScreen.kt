package com.example.ladycure

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import DefaultPrimaryVariant
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
    // State management
    var selectedTab by remember { mutableStateOf("Users") } // "Users" or "Doctors"
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf(User("", "", "", "", "", "")) }

    // Firestore
    val db = FirebaseFirestore.getInstance("telecure")
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    // Fetch users from Firestore
    LaunchedEffect(selectedTab) {
        db.collection("users")
            .whereEqualTo("role", if (selectedTab == "Doctors") "doctor" else "user")
            .addSnapshotListener { snapshot, _ ->
                allUsers = snapshot?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        surname = doc.getString("surname") ?: "",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "user",
                        dob = doc.getString("dob") ?: ""
                    )
                } ?: emptyList()
            }
    }

    // Filter users based on search query
    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                        user.surname.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = editedUser,
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
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Admin Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DefaultBackground,
                        titleContentColor = DefaultPrimary,
                        navigationIconContentColor = DefaultPrimary
                    )
                )

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    ),
                    placeholder = {
                        Text("Search ${selectedTab.lowercase()}...", color = DefaultOnPrimary.copy(alpha = 0.5f))
                    }
                )

                // Tab selection
                TabRow(
                    selectedTabIndex = if (selectedTab == "Users") 0 else 1,
                    containerColor = DefaultBackground,
                    contentColor = DefaultPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "Users") 0 else 1]),
                            color = DefaultPrimary,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == "Users",
                        onClick = { selectedTab = "Users" },
                        text = {
                            Text(
                                "Users",
                                color = if (selectedTab == "Users") DefaultPrimary else DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == "Doctors",
                        onClick = { selectedTab = "Doctors" },
                        text = {
                            Text(
                                "Doctors",
                                color = if (selectedTab == "Doctors") DefaultPrimary else DefaultOnPrimary.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
        },
        containerColor = DefaultBackground
    ) { padding ->
        when {
            filteredUsers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    if (allUsers.isEmpty()) {
                        CircularProgressIndicator(color = DefaultPrimary)
                    } else {
                        Text(
                            "No ${selectedTab.lowercase()} found",
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                    }
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
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
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
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DefaultPrimary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = DefaultPrimaryVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    color = DefaultOnPrimary.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DOB: ${user.dob}",
                        color = DefaultOnPrimary.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )

                    RoleBadge(role = user.role)
                }
            }

            IconButton(
                onClick = onEditClick,
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

@Composable
private fun RoleBadge(role: String) {
    val (backgroundColor, roleTextColor) = when (role) {
        "admin" -> DefaultPrimary.copy(alpha = 0.2f) to DefaultPrimary
        "doctor" -> Color(0xFFCB52C8).copy(alpha = 0.2f) to Color(0xFFCB52C8)
        else -> Color(0xFFEF55DB).copy(alpha = 0.2f) to Color(0xFFEF55DB)
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
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUserChange: (User) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit User",
                color = DefaultPrimary,
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
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = user.surname,
                    onValueChange = { onUserChange(user.copy(surname = it)) },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = user.email,
                    onValueChange = { onUserChange(user.copy(email = it)) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = user.dob,
                    onValueChange = { onUserChange(user.copy(dob = it)) },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DefaultBackground,
                        unfocusedContainerColor = DefaultBackground,
                        focusedIndicatorColor = DefaultPrimary,
                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                    )
                )

                Text("Role", style = MaterialTheme.typography.labelLarge, color = DefaultPrimary)

                RoleSelection(
                    selectedRole = user.role,
                    onRoleSelected = { onUserChange(user.copy(role = it)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
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
                    color = DefaultPrimary
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = DefaultBackground
    )
}

@Composable
private fun RoleSelection(
    selectedRole: String,
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
                        color = if (selectedRole == role) Color.White else DefaultPrimary
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Transparent,
                    labelColor = DefaultPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}