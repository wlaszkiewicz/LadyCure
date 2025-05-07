package com.example.ladycure

import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.data.Role
import com.example.ladycure.data.User
import com.example.ladycure.data.doctor.Doctor
import com.example.ladycure.data.doctor.Speciality
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

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

        val usersResult = authRepo.getUsers()
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
                navController = navController,
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

                            val result = authRepo.updateUser(originalUser.id, updates)

                            if (result.isSuccess) {
                                snackbarController.showMessage(
                                    if (editedUser!!.role == Role.DOCTOR)
                                        "User converted to doctor successfully"
                                    else
                                        "User updated successfully"
                                )
                                showEditUserDialog = false
                                val refreshResult = authRepo.getUsers()
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
                                val result = authRepo.updateUser(doctor.id, updates)
                                if (result.isSuccess) {
                                    snackbarController.showMessage("User updated successfully")
                                    showEditDoctorDialog = false
                                    // Refresh data
                                    val refreshResult = authRepo.getUsers()
                                    if (refreshResult.isSuccess) {
                                        users = refreshResult.getOrNull() ?: emptyList()
                                    }
                                } else {
                                    snackbarController.showMessage("Error updating user: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        }
                    } else if (editedDoctor?.role == Role.USER) {
                        coroutineScope.launch {
                            selectedDoctor?.let { doctor ->
                                val updates = buildUpdateMap(editedDoctor!! as User)
                                val result = authRepo.docToUserUpdate(doctor.id, updates)
                                if (result.isSuccess) {
                                    snackbarController.showMessage("User data updated successfully")
                                    showEditDoctorDialog = false
                                    // Refresh data
                                    val refreshResult = authRepo.getUsers()
                                    if (refreshResult.isSuccess) {
                                        users = refreshResult.getOrNull() ?: emptyList()
                                    }
                                } else {
                                    snackbarController.showMessage("Error updating user: ${result.exceptionOrNull()?.message}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(
    navController: NavController,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 16.dp)
        ) {
            Text(
                "Admin Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        DefaultPrimary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }

        }

        AdminSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange
        )

        // Tab selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = selectedTab == "Users",
                onClick = { onTabSelected("Users") },
                label = { Text("Users") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedTab == "Doctors",
                onClick = { onTabSelected("Doctors") },
                label = { Text("Doctors") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun UserList(
    users: List<User>,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users) { user ->
            UserCard(
                user = user,
                onEditClick = { onEditClick(user) },
                onDeleteClick = { onDeleteClick(user) }
            )
        }
    }
}


@Composable
private fun DoctorList(
    doctors: List<Doctor>,
    onEditClick: (Doctor) -> Unit,
    onDeleteClick: (Doctor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(doctors) { doctor ->
            DoctorCard(
                doctor = doctor,
                onEditClick = { onEditClick(doctor) },
                onDeleteClick = { onDeleteClick(doctor) }
            )
        }
    }
}

@Composable
fun AdminSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    "Search...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            maxLines = 1
        )

        if (searchQuery.isNotEmpty()) {
            IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.profilePictureUrl.isEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Profile picture",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        AsyncImage(
                            model = user.profilePictureUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${user.name} ${user.surname}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Date of Birth",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        user.dateOfBirth,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                RoleBadge(role = user.role)
            }

        }
    }
}

@Composable
private fun DoctorCard(
    doctor: Doctor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (doctor.profilePictureUrl.isEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Profile picture",
                            tint = Color(0xFF5FB9C9),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        AsyncImage(
                            model = doctor.profilePictureUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${doctor.name} ${doctor.surname}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = doctor.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Date of Birth",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        doctor.dateOfBirth,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                RoleBadge(role = doctor.role)
            }
            DoctorDetailsSection(doctor = doctor)
        }
    }
}

@Composable
private fun DoctorDetailsSection(doctor: Doctor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Divider(modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Specialization",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    doctor.speciality.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Consultation Fee",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${doctor.consultationPrice} PLN",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Rating",
                    style = MaterialTheme.typography.labelSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${doctor.rating}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Experience",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${doctor.experience} years",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Address",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            doctor.address,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RoleBadge(role: Role) {
    val backgroundColor = when (role) {
        Role.USER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        Role.DOCTOR -> Color(0xFF5FB9C9).copy(alpha = 0.1f)
        else -> Color(0xFF7050AB).copy(alpha = 0.1f)
    }
    val textColor = when (role) {
        Role.USER -> MaterialTheme.colorScheme.primary
        Role.DOCTOR -> Color(0xFF5FB9C9)
        else -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = role.value,
            color = textColor,
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
        title = { Text("Edit User", fontWeight = FontWeight.Bold) },
        text = {
            UserForm(
                user = user,
                onUserChange = onUserChange,
                isEditMode = true
            )
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun EditDoctorDialog(
    doctor: Doctor,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDoctorChange: (Doctor) -> Unit,
    isSaving: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                "Edit Doctor Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DoctorForm(
                doctor = doctor,
                onDoctorChange = onDoctorChange
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    disabledContainerColor = DefaultPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isSaving) onDismiss() },
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun AddUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUserChange: (User) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User", fontWeight = FontWeight.Bold) },
        text = {
            UserForm(
                user = user,
                onUserChange = onUserChange,
                isEditMode = false
            )
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Create User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
private fun UserForm(
    user: User,
    onUserChange: (User) -> Unit,
    isEditMode: Boolean
) {
    // Track whether we're showing doctor fields
    val showDoctorFields = user.role == Role.DOCTOR

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = user.name,
            onValueChange = { onUserChange(user.copy(name = it)) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = user.surname,
            onValueChange = { onUserChange(user.copy(surname = it)) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = user.email,
            onValueChange = { onUserChange(user.copy(email = it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = user.dateOfBirth,
            onValueChange = { onUserChange(user.copy(dateOfBirth = it)) },
            label = { Text("Date of Birth (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Role", style = MaterialTheme.typography.labelLarge)
        RoleSelection(
            selectedRole = user.role,
            onRoleSelected = { newRole ->
                if (newRole == Role.DOCTOR) {
                    onUserChange(user.toDoctor())
                } else {
                    onUserChange(user.copy(role = newRole))
                }
            }
        )

        if (showDoctorFields) {
            val doctor = user as? Doctor ?: user.toDoctor()
            DoctorDetailsDialogSection(
                doctor = doctor,
                onDoctorChange = { newDoctor ->
                    if (newDoctor.role != Role.DOCTOR) {
                        onUserChange(newDoctor.toUser())
                    } else {
                        onUserChange(newDoctor)
                    }
                }
            )
        }
    }
}

@Composable
private fun DoctorForm(
    doctor: Doctor,
    onDoctorChange: (Doctor) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Information Section
        Text("Personal Information", style = MaterialTheme.typography.titleSmall)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = doctor.name,
                onValueChange = { onDoctorChange(doctor.copyDoc(name = it)) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.surname,
                onValueChange = { onDoctorChange(doctor.copyDoc(surname = it)) },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.email,
                onValueChange = { onDoctorChange(doctor.copyDoc(email = it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.dateOfBirth,
                onValueChange = { onDoctorChange(doctor.copyDoc(dateOfBirth = it)) },
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Role", style = MaterialTheme.typography.labelLarge)
            RoleSelection(
                selectedRole = doctor.role,
                onRoleSelected = { onDoctorChange(doctor.copyDoc(role = it)) }
            )
        }

        if (doctor.role == Role.DOCTOR) {

            Divider()

            // Professional Information Section
            Text("Professional Information", style = MaterialTheme.typography.titleSmall)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Specialization Dropdown
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = doctor.speciality.displayName,
                        onValueChange = {},
                        label = { Text("Specialization") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Expand"
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Speciality.entries.forEach { speciality ->
                            DropdownMenuItem(
                                text = { Text(speciality.displayName) },
                                onClick = {
                                    onDoctorChange(doctor.copyDoc(speciality = speciality))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Location Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = doctor.city,
                        onValueChange = { onDoctorChange(doctor.copyDoc(city = it)) },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = doctor.phoneNumber,
                        onValueChange = { onDoctorChange(doctor.copyDoc(phoneNumber = it)) },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = doctor.address,
                    onValueChange = { onDoctorChange(doctor.copyDoc(address = it)) },
                    label = { Text("Full Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Divider()

            // Professional Details Section
            Text("Professional Details", style = MaterialTheme.typography.titleSmall)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = doctor.consultationPrice.toString(),
                        onValueChange = {
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                onDoctorChange(
                                    doctor.copyDoc(
                                        consultationPrice = it.toIntOrNull() ?: 0
                                    )
                                )
                            }
                        },
                        label = { Text("Consultation Fee") },
                        modifier = Modifier.weight(1f),
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = doctor.experience.toString(),
                        onValueChange = {
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                onDoctorChange(
                                    doctor.copyDoc(
                                        experience = it.toIntOrNull() ?: 0
                                    )
                                )
                            }
                        },
                        label = { Text("Experience") },
                        modifier = Modifier.weight(1f),
                        suffix = { Text("years") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Rating with visual indicator
                Column {
                    Text("Rating", style = MaterialTheme.typography.labelMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RatingBar(
                            rating = doctor.rating,
                            onRatingChange = { newRating ->
                                val clampedRating = newRating.coerceIn(0.5, 5.0)
                                onDoctorChange(doctor.copyDoc(rating = clampedRating))
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Divider()

            // Languages Section
            Text("Languages", style = MaterialTheme.typography.titleSmall)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var newLanguage by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newLanguage,
                        onValueChange = { newLanguage = it },
                        label = { Text("Add language") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newLanguage.isNotBlank() && !doctor.languages.contains(newLanguage)) {
                                onDoctorChange(doctor.copyDoc(languages = doctor.languages + newLanguage))
                                newLanguage = ""
                            }
                        },
                        enabled = newLanguage.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }

                if (doctor.languages.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        doctor.languages.forEach { language ->
                            InputChip(
                                selected = true,
                                onClick = {},
                                label = { Text(language) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            onDoctorChange(
                                                doctor.copyDoc(
                                                    languages = doctor.languages - language
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = DefaultPrimary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        "No languages added",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Divider()

            // Biography Section
            Text("Biography", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = doctor.bio,
                onValueChange = { onDoctorChange(doctor.copyDoc(bio = it)) },
                label = { Text("Professional background") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    starCount: Int = 5
) {
    var showManualInput by remember { mutableStateOf(false) }
    var manualRating by remember { mutableStateOf(rating.toString()) }

    Column(modifier = modifier) {
        // Star rating and edit button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Star rating interface
            Row {
                for (i in 1..starCount) {
                    val starValue = i.toDouble()
                    val isHalfStar = (rating >= starValue - 0.5) && (rating < starValue)
                    val isFullStar = rating >= starValue

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                onRatingChange(starValue - 0.5)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        onRatingChange(starValue - 0.5)
                                    },
                                    onDoubleTap = {
                                        onRatingChange(starValue)
                                    }
                                )
                            }
                    ) {
                        if (isFullStar) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Full star",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (isHalfStar) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = "Half star background",
                                    tint = Color.Gray,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.StarHalf,
                                    contentDescription = "Half star",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.fillMaxSize()
                                        .align(Alignment.CenterStart)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = "Empty star",
                                tint = Color.Gray,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Rating value display
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Manual input toggle button
            IconButton(
                onClick = {
                    showManualInput = !showManualInput
                    if (!showManualInput) {
                        val newRating = manualRating.toDoubleOrNull()?.coerceIn(0.5, 5.0) ?: rating
                        onRatingChange(newRating)
                        manualRating = newRating.toString()
                    } else {
                        manualRating = rating.toString()
                    }
                }
            ) {
                Icon(
                    imageVector = if (showManualInput) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (showManualInput) "Close manual input" else "Edit manually",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Manual input field (shown when enabled)
        if (showManualInput) {
            OutlinedTextField(
                value = manualRating,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() ||
                        newValue.matches(Regex("^\\d*\\.?\\d*$")) &&
                        newValue.count { it == '.' } <= 1
                    ) {
                        manualRating = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Enter rating (0.5-5.0)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                trailingIcon = {
                    if (manualRating.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val newRating = manualRating.toDoubleOrNull()?.coerceIn(0.5, 5.0) ?: rating
                                onRatingChange(newRating)
                                manualRating = newRating.toString()
                                showManualInput = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply rating",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DoctorDetailsDialogSection(
    doctor: Doctor,
    onDoctorChange: (Doctor) -> Unit
) {
    Text("Doctor Details", style = MaterialTheme.typography.labelLarge)

    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = doctor.speciality.displayName,
            onValueChange = {},
            label = { Text("Specialization") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Speciality.entries.forEach { speciality ->
                DropdownMenuItem(
                    text = { Text(speciality.displayName) },
                    onClick = {
                        onDoctorChange(doctor.copyDoc(speciality = speciality))
                        expanded = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = doctor.address,
        onValueChange = { onDoctorChange(doctor.copyDoc(address = it)) },
        label = { Text("Address") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = doctor.consultationPrice.toString(),
        onValueChange = {
            onDoctorChange(
                doctor.copyDoc(
                    consultationPrice = it.toIntOrNull() ?: doctor.consultationPrice
                )
            )
        },
        label = { Text("Consultation Fee ($)") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = doctor.rating.toString(),
        onValueChange = {
            onDoctorChange(doctor.copyDoc(rating = it.toDoubleOrNull() ?: doctor.rating))
        },
        label = { Text("Rating (1-5)") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = doctor.experience.toString(),
        onValueChange = {
            onDoctorChange(
                doctor.copyDoc(
                    experience = it.toIntOrNull() ?: doctor.experience
                )
            )
        },
        label = { Text("Experience (years)") },
        modifier = Modifier.fillMaxWidth()
    )

    // Languages chips
    var newLanguage by remember { mutableStateOf("") }
    Column {
        Text("Languages", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newLanguage,
                onValueChange = { newLanguage = it },
                label = { Text("Add language") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newLanguage.isNotBlank()) {
                        onDoctorChange(doctor.copyDoc(languages = doctor.languages + newLanguage))
                        newLanguage = ""
                    }
                }
            ) {
                Text("Add")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            doctor.languages.forEach { language ->
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text(language) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onDoctorChange(
                                    doctor.copyDoc(
                                        languages = doctor.languages - language
                                    )
                                )
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = DefaultPrimary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RoleSelection(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Role.entries.forEach { role ->
            FilterChip(
                selected = selectedRole == role,
                onClick = { onRoleSelected(role) },
                label = { Text(role.value) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete ${user.name} ${user.surname}?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = DefaultPrimary)
    }
}

@Composable
private fun EmptyView(selectedTab: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No ${selectedTab.lowercase()} found",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun buildUpdateMap(user: User): Map<String, Any> {
    return mutableMapOf<String, Any>().apply {
        put("name", user.name)
        put("surname", user.surname)
        put("email", user.email)
        put("role", user.role.value)
        put("dob", user.dateOfBirth)
        put("profilePictureUrl", user.profilePictureUrl)

        if (user is Doctor && user.role == Role.DOCTOR) {
            put("speciality", user.speciality.displayName)
            put("address", user.address)
            put("consultationPrice", user.consultationPrice)
            put("rating", user.rating)
            put("experience", user.experience)
            put("languages", user.languages)
            put("city", user.city)
            put("phoneNumber", user.phoneNumber)
            put("bio", user.bio)
        }
    }
}