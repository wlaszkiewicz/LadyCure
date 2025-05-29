package com.example.ladycure.screens

import DefaultPrimary
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ladycure.data.Role
import com.example.ladycure.data.User
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch

import com.example.ladycure.presentation.admin.*
import com.example.ladycure.repository.UserRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController, snackbarController: SnackbarController) {

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showEditUserDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf<User?>(null) }
    var newUser by remember { mutableStateOf(User.empty()) }

    var showEditDoctorDialog by remember { mutableStateOf(false) }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var editedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var newDoctor by remember { mutableStateOf(Doctor.empty()) }

    val userRepo = UserRepository()
    val authRepo = AuthRepository()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State management
    var selectedTab by remember { mutableStateOf("Users") }
    var searchQuery by remember { mutableStateOf("") }

    // Separate loading states
    var isLoadingUsers by remember { mutableStateOf(false) }
    var isLoadingDoctors by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Fetch all data on first load
    LaunchedEffect(Unit) {
        isLoadingUsers = true
        isLoadingDoctors = true

        val usersResult = userRepo.getUsers()
        if (usersResult.isSuccess) {
            users = usersResult.getOrNull() ?: emptyList()
        } else {
            snackbarController.showMessage("Failed to load users: ${usersResult.exceptionOrNull()?.message}")
        }

        isLoadingUsers = false
        isLoadingDoctors = false
    }

    // Filter data based on current tab
    val allDoctors = remember(users) {
        users.filter { it["role"] == Role.DOCTOR.value }
            .map { Doctor.fromMap(it) }
    }

    val allUsers = remember(users) {
        users.filter { it["role"] != Role.DOCTOR.value }
            .map { User.fromMap(it) }
    }

    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) allUsers else {
            allUsers.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true)
            }
        }
    }

    val filteredDoctors = remember(allDoctors, searchQuery) {
        if (searchQuery.isBlank()) allDoctors else {
            allDoctors.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true) ||
                        it.speciality.displayName.contains(searchQuery, true) ||
                        it.address.contains(searchQuery, true) ||
                        it.city.contains(searchQuery, true)
            }
        }
    }
    Scaffold(
        topBar = {
            AdminTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onLogout = {
                    showLogoutDialog = true
                }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = {
                        newUser = User.empty()
                        newDoctor = Doctor.empty()
                        showAddDialog = true
                    },
                    containerColor = DefaultPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add user")
                }

            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            isLoadingUsers && selectedTab == "Users" -> LoadingView()
            isLoadingDoctors && selectedTab == "Doctors" -> LoadingView()
            filteredUsers.isEmpty() && selectedTab == "Users" -> EmptyView(selectedTab)
            filteredDoctors.isEmpty() && selectedTab == "Doctors" -> EmptyView(selectedTab)
            selectedTab == "Users" -> UserList(
                users = filteredUsers,
                onEditClick = { user ->
                    selectedUser = user
                    editedUser = user
                    showEditUserDialog = true
                },
                onDeleteClick = { user ->
                    selectedUser = user
                    showDeleteDialog = true
                },
                modifier = Modifier.padding(padding)
            )

            else -> DoctorList(
                doctors = filteredDoctors,
                onEditClick = { doctor ->
                    selectedDoctor = doctor
                    editedDoctor = doctor.copyDoc()
                    showEditDoctorDialog = true
                },
                onDeleteClick = { doctor ->
                    selectedDoctor = doctor
                    showDeleteDialog = true
                },
                modifier = Modifier.padding(padding)
            )
        }

        if (showEditUserDialog && editedUser != null) {
            EditUserDialog(
                user = editedUser!!,
                onDismiss = { showEditUserDialog = false },
                onSave = {
                    coroutineScope.launch {
                        selectedUser?.let { originalUser ->
                            val updates = buildUpdateMap(editedUser!!)

                            val result = userRepo.updateUser(originalUser.id, updates)

                            if (result.isSuccess) {
                                snackbarController.showMessage(
                                    if (editedUser!!.role == Role.DOCTOR)
                                        "User converted to doctor successfully"
                                    else
                                        "User updated successfully"
                                )
                                showEditUserDialog = false
                                val refreshResult = userRepo.getUsers()
                                if (refreshResult.isSuccess) {
                                    users = refreshResult.getOrNull() ?: emptyList()
                                }
                            } else {
                                snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                },
                onUserChange = { editedUser = it }
            )
        }

        if (showEditDoctorDialog && editedDoctor != null) {
            EditDoctorDialog(
                doctor = editedDoctor!!,
                onDismiss = { showEditDoctorDialog = false },
                onSave = {
                    if (editedDoctor?.role == Role.DOCTOR) {
                        coroutineScope.launch {
                            selectedDoctor?.let { doctor ->
                                val updates = buildUpdateMap(editedDoctor!!)
                                val result = userRepo.updateUser(doctor.id, updates)
                                if (result.isSuccess) {
                                    snackbarController.showMessage("Doctor updated successfully")
                                    showEditDoctorDialog = false
                                    // Refresh data
                                    val refreshResult = userRepo.getUsers()
                                    if (refreshResult.isSuccess) {
                                        users = refreshResult.getOrNull() ?: emptyList()
                                    }
                                } else {
                                    snackbarController.showMessage("Error updating doctor: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    } else if (editedDoctor?.role == Role.USER) {
                        coroutineScope.launch {
                            selectedDoctor?.let { doctor ->
                                val updates = buildUpdateMap(editedDoctor!! as User)
                                val result = userRepo.docToUserUpdate(doctor.id, updates)
                                if (result.isSuccess) {
                                    snackbarController.showMessage("Doctor converted to user successfully")
                                    showEditDoctorDialog = false
                                    // Refresh data
                                    val refreshResult = userRepo.getUsers()
                                    if (refreshResult.isSuccess) {
                                        users = refreshResult.getOrNull() ?: emptyList()
                                    }
                                } else {
                                    snackbarController.showMessage("Error updating doctor: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    }
                },
                onDoctorChange = { editedDoctor = it }
            )
        }

        if (showDeleteDialog && selectedUser != null) {
            DeleteConfirmationDialog(
                user = selectedUser!!,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
//                    coroutineScope.launch {
//                        val result = authRepo.deleteUser(selectedUser!!.id)
//                        if (result.isSuccess) {
//                            snackbarController.showMessage("User deleted successfully")
//                            showDeleteDialog = false
//                            // Refresh data
//                            val refreshResult = authRepo.getUsers()
//                            if (refreshResult.isSuccess) {
//                                users = refreshResult.getOrNull() ?: emptyList()
//                            }
//                        } else {
//                            snackbarController.showMessage("Error deleting user: ${result.exceptionOrNull()?.message}")
//                        }
//                    }
                    snackbarController.showMessage("User deleted successfully. NOT REALLY XDDD NOT IMPLEMENTED")
                }
            )
        }

        if (showAddDialog) {
            AddUserDialog(
                user = newUser,
                onDismiss = { showAddDialog = false },
                onSave = {
//                        coroutineScope.launch {
//                            val result = authRepo.createUser(newUser.toMap())
//                            if (result.isSuccess) {
//                                snackbarController.showMessage("User created successfully")
//                                showAddDialog = false
//                                // Refresh data
//                                val refreshResult = authRepo.getUsers()
//                                if (refreshResult.isSuccess) {
//                                    users = refreshResult.getOrNull() ?: emptyList()
//                                }
//                            } else {
//                                snackbarController.showMessage("Error creating user: ${result.exceptionOrNull()?.message}")
//                            }
//                        }
                    snackbarController.showMessage("User created successfully. NOT REALLY XDDD NOT IMPLEMENTED")
                },
                onUserChange = { newUser = it }
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log out") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                authRepo.signOut()
                                navController.navigate("login") {
                                    popUpTo("admin") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text("Log out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}



