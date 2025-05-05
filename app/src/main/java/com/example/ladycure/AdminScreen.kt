package com.example.ladycure

import DefaultBackground
import DefaultPrimary
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ladycure.data.Role
import com.example.ladycure.data.User
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, snakbarController: SnackbarController) {
    val authRepo = AuthRepository()

    // State management
    var selectedTab by remember { mutableStateOf("Users") } // "Users" or "Doctors"
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf<User?>(null) }
    val errorMessage = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(true) }

    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var allDoctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch users from Firestore
    LaunchedEffect(selectedTab) {
        val result = authRepo.getUsers()
        if (result.isSuccess) {
            users = result.getOrNull() ?: emptyList()
            isLoading.value = false
        } else {
            isLoading.value = false
            errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"
        }
    }


    allDoctors = users.filter { it["role"] == Role.DOCTOR.value }
        .map { user ->
            Doctor.fromMap(user)
        }

    allUsers = users.filter { it["role"] != Role.DOCTOR.value }
        .map { user ->
            User.fromMap(user)
        }

    LaunchedEffect(errorMessage) {
        if (errorMessage.value.isNotEmpty()) {
            snakbarController.showMessage(
                message = errorMessage.value
            )
            errorMessage.value = ""
        }
    }

    // Filter users based on search query
    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank() && selectedTab == "Users") {
            allUsers
        } else if (searchQuery.isBlank() && selectedTab == "Doctors") {
            allDoctors
        } else if (selectedTab == "Doctors") {
            allDoctors.filter { doctor ->
                doctor.name.contains(searchQuery, ignoreCase = true) ||
                        doctor.surname.contains(searchQuery, ignoreCase = true) ||
                        doctor.email.contains(searchQuery, ignoreCase = true)
            }
        } else {
            allUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                        user.surname.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && selectedUser != null && editedUser != null) {
        EditUserDialog(
            user = editedUser!!,
            onDismiss = { showEditDialog = false },
            onSave = {
                selectedUser?.let { user ->
                    val updates = hashMapOf<String, Any>(
                        "name" to editedUser!!.name,
                        "surname" to editedUser!!.surname,
                        "email" to editedUser!!.email,
                        "role" to editedUser!!.role.value,
                        "dob" to editedUser!!.dateOfBirth,
                        "profilePictureUrl" to editedUser!!.profilePictureUrl
                    )

                    if (editedUser!!.role == Role.DOCTOR && editedUser is Doctor) {
                        val doctor = editedUser as Doctor
                        updates["speciality"] = doctor.speciality.displayName
                        updates["address"] = doctor.address
                        updates["consultationPrice"] = doctor.consultationPrice
                        updates["availability"] = doctor.availability
                        updates["rating"] = doctor.rating
                        updates["experience"] = doctor.experience
                        updates["languages"] = doctor.languages
                        updates["city"] = doctor.city
                        updates["phoneNumber"] = doctor.phoneNumber
                        updates["bio"] = doctor.bio
                    }

                    coroutineScope.launch {
                        val result = authRepo.updateUser(
                            userId = user.id,
                            updatedData = updates
                        )

                        if (result.isSuccess) {
                            showEditDialog = false
                            val currentTab = selectedTab
                            selectedTab = ""
                            selectedTab = currentTab
                            snakbarController.showMessage(
                                message = "User updated successfully"
                            )
                        } else {
                            snakbarController.showMessage(
                                message = result.exceptionOrNull()?.message ?: "Error updating user"
                            )
                        }
                    }

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
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            "Search ${selectedTab.lowercase()}...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                )

                // Tab selection
                SecondaryTabRow(
                    selectedTabIndex = if (selectedTab == "Users") 0 else 1,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = DefaultBackground,
                    contentColor = DefaultPrimary,
                    indicator = {},
                    divider = {},
                ) {
                    Tab(
                        selected = selectedTab == "Users",
                        onClick = { selectedTab = "Users" },
                        text = {
                            Text(
                                "Users",
                                color = if (selectedTab == "Users") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == "Doctors",
                        onClick = { selectedTab = "Doctors" },
                        text = {
                            Text(
                                "Doctors",
                                color = if (selectedTab == "Doctors") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            Button(
                onClick = {
                    authRepo.signOut()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            ) {
                Text(text = "Log out")
            }
        }
    ) { padding ->
        when {
            filteredUsers.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text(
                            "No ${selectedTab.lowercase()} found",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                                editedUser = when (user) {
                                    is Doctor -> user.copy()
                                    else -> user.copy()
                                }
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
            containerColor = Color.White.copy(alpha = 0.8f),
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${user.name} ${user.surname}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = user.email,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOB: ${user.dateOfBirth}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Additional doctor information
            if (user.role == Role.DOCTOR && user is Doctor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Specialization: ${user.speciality.displayName}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Address: ${user.address}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Consultation price: ${user.consultationPrice} PLN",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Rating: ${user.rating}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: Role) {
    val (backgroundColor, roleTextColor) = when (role) {
        Role.ADMIN -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.primary
        Role.DOCTOR -> Color(0xFFCB52C8).copy(alpha = 0.2f) to Color(0xFFCB52C8)
        else -> Color(0xFFEF55DB).copy(alpha = 0.2f) to Color(0xFFEF55DB)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = role.value,
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Basic user info
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
                    value = user.dateOfBirth,
                    onValueChange = { onUserChange(user.copy(dateOfBirth = it)) },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Text(
                    "Role",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                RoleSelection(
                    selectedRole = user.role,
                    onRoleSelected = { onUserChange(user.copy(role = it)) }
                )

                // Doctor-specific fields (shown only when role is doctor)
                if (user.role == Role.DOCTOR && user is Doctor) {
                    val doctor = user as Doctor
                    Text(
                        "Doctor Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = doctor.speciality.displayName,
                        onValueChange = {
                            val newSpeciality = Speciality.fromDisplayName(it)
                            onUserChange(doctor.copy(speciality = newSpeciality))
                        },
                        label = { Text("Specialization") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = doctor.address,
                        onValueChange = { onUserChange(doctor.copy(address = it)) },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = doctor.consultationPrice.toString(),
                        onValueChange = {
                            val price = it.toIntOrNull() ?: doctor.consultationPrice
                            onUserChange(doctor.copy(consultationPrice = price))
                        },
                        label = { Text("Consultation Price ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = doctor.rating.toString(),
                        onValueChange = {
                            val rating = it.toDoubleOrNull() ?: doctor.rating
                            onUserChange(doctor.copy(rating = rating))
                        },
                        label = { Text("Rating") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
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
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun RoleSelection(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Role.values().forEach { role ->
            FilterChip(
                selected = selectedRole == role,
                onClick = { onRoleSelected(role) },
                label = {
                    Text(
                        role.value,
                        color = if (selectedRole == role) Color.White else MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}