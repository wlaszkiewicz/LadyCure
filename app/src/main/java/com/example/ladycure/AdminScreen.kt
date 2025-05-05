package com.example.ladycure

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ladycure.repository.AuthRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {

    val authRepo = AuthRepository()

    Button(
        onClick = {
            authRepo.signOut()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Log out")
    }
//    // State management
//    var selectedTab by remember { mutableStateOf("Users") } // "Users" or "Doctors"
//    var searchQuery by remember { mutableStateOf("") }
//    var showEditDialog by remember { mutableStateOf(false) }
//    var selectedUser by remember { mutableStateOf<User?>(null) }
//    var editedUser by remember { mutableStateOf(User()) }
//
//    // Firestore
//    val db = FirebaseFirestore.getInstance("telecure")
//    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
//
//    // Fetch users from Firestore
//    LaunchedEffect(selectedTab) {
//        db.collection("users")
//            .whereEqualTo("role", if (selectedTab == "Doctors") "doctor" else "user")
//            .addSnapshotListener { snapshot, _ ->
//                allUsers = snapshot?.documents?.mapNotNull { doc ->
//                    User(
//                        id = doc.id,
//                        name = doc.getString("name") ?: "",
//                        surname = doc.getString("surname") ?: "",
//                        email = doc.getString("email") ?: "",
//                        role = doc.getString("role") ?: "user",
//                        dob = doc.getString("dob") ?: "",
//                        specification = doc.getString("specification") ?: "",
//                        address = doc.getString("address") ?: "",
//                        consultationPrice = doc.getString("consultationPrice") ?: "",
//                        availability = doc.getString("availability") ?: "",
//                        rating = doc.getDouble("rating"),
//                        reviews = doc.getString("reviews") ?: ""
//                    )
//                } ?: emptyList()
//            }
//    }
//
//    // Filter users based on search query
//    val filteredUsers = remember(allUsers, searchQuery) {
//        if (searchQuery.isBlank()) {
//            allUsers
//        } else {
//            allUsers.filter { user ->
//                user.name.contains(searchQuery, ignoreCase = true) ||
//                        user.surname.contains(searchQuery, ignoreCase = true) ||
//                        user.email.contains(searchQuery, ignoreCase = true)
//            }
//        }
//    }
//
//    // Edit Dialog
//    if (showEditDialog && selectedUser != null) {
//
//        EditUserDialog(
//            user = editedUser,
//            onDismiss = { showEditDialog = false },
//            onSave = {
//                selectedUser?.let { user ->
//                    val updates = hashMapOf<String, Any>(
//                        "name" to editedUser.name,
//                        "surname" to editedUser.surname,
//                        "email" to editedUser.email,
//                        "role" to editedUser.role,
//                        "dob" to editedUser.dob
//                    )
//
//                    if (editedUser.role == "doctor") {
//                        updates["specification"] = editedUser.specification
//                        updates["address"] = editedUser.address
//                        updates["consultationPrice"] = editedUser.consultationPrice
//                        updates["availability"] = editedUser.availability
//                        editedUser.rating?.let { updates["rating"] = it }
//                    }
//
//                    db.collection("users").document(user.id)
//                        .update(updates)
//                        .addOnSuccessListener {
//                            showEditDialog = false
//
//                            val currentTab = selectedTab
//                            selectedTab = ""
//                            selectedTab = currentTab
//                        }
//                        .addOnFailureListener { e ->
//                            println("Error updating user: ${e.message}")
//                        }
//                }
//            },
//            onUserChange = { editedUser = it }
//        )
//    }
//    Scaffold(
//        topBar = {
//            Column {
//                CenterAlignedTopAppBar(
//                    title = {
//                        Text(
//                            "Admin Dashboard",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = DefaultPrimary
//                        )
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(
//                                Icons.AutoMirrored.Filled.ArrowBack,
//                                contentDescription = "Back",
//                                tint = DefaultPrimary
//                            )
//                        }
//                    },
//                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                        containerColor = DefaultBackground,
//                        titleContentColor = DefaultPrimary,
//                        navigationIconContentColor = DefaultPrimary
//                    )
//                )
//
//                // Search bar
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = DefaultBackground,
//                        unfocusedContainerColor = DefaultBackground,
//                        focusedIndicatorColor = DefaultPrimary,
//                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                    ),
//                    placeholder = {
//                        Text(
//                            "Search ${selectedTab.lowercase()}...",
//                            color = DefaultOnPrimary.copy(alpha = 0.5f)
//                        )
//                    }
//                )
//
//                // Tab selection
//                TabRow(
//                    selectedTabIndex = if (selectedTab == "Users") 0 else 1,
//                    containerColor = DefaultBackground,
//                    contentColor = DefaultPrimary,
//                    indicator = { tabPositions ->
//                        TabRowDefaults.Indicator(
//                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "Users") 0 else 1]),
//                            color = DefaultPrimary,
//                            height = 2.dp
//                        )
//                    }
//                ) {
//                    Tab(
//                        selected = selectedTab == "Users",
//                        onClick = { selectedTab = "Users" },
//                        text = {
//                            Text(
//                                "Users",
//                                color = if (selectedTab == "Users") DefaultPrimary else DefaultOnPrimary.copy(
//                                    alpha = 0.6f
//                                )
//                            )
//                        }
//                    )
//                    Tab(
//                        selected = selectedTab == "Doctors",
//                        onClick = { selectedTab = "Doctors" },
//                        text = {
//                            Text(
//                                "Doctors",
//                                color = if (selectedTab == "Doctors") DefaultPrimary else DefaultOnPrimary.copy(
//                                    alpha = 0.6f
//                                )
//                            )
//                        }
//                    )
//                }
//            }
//        },
//        containerColor = DefaultBackground
//    ) { padding ->
//        when {
//            filteredUsers.isEmpty() -> {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(padding),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (allUsers.isEmpty()) {
//                        CircularProgressIndicator(color = DefaultPrimary)
//                    } else {
//                        Text(
//                            "No ${selectedTab.lowercase()} found",
//                            color = DefaultOnPrimary.copy(alpha = 0.6f)
//                        )
//                    }
//                }
//            }
//
//            else -> {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(padding)
//                        .padding(horizontal = 16.dp),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    items(filteredUsers) { user ->
//                        UserCard(
//                            user = user,
//                            onEditClick = {
//                                selectedUser = user
//                                editedUser = user.copy()
//                                showEditDialog = true
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun UserCard(
//    user: User,
//    onEditClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = DefaultPrimary.copy(alpha = 0.1f)
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text(
//                        text = "${user.name} ${user.surname}",
//                        color = DefaultPrimaryVariant,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//
//                    Text(
//                        text = user.email,
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "DOB: ${user.dob}",
//                            color = DefaultOnPrimary.copy(alpha = 0.6f),
//                            fontSize = 13.sp
//                        )
//
//                        RoleBadge(role = user.role)
//                    }
//                }
//
//                IconButton(
//                    onClick = onEditClick,
//                    modifier = Modifier.size(40.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Edit,
//                        contentDescription = "Edit user",
//                        tint = DefaultPrimary
//                    )
//                }
//            }
//
//            // Additional doctor information
//            if (user.role == "doctor") {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 12.dp),
//                    verticalArrangement = Arrangement.spacedBy(6.dp)
//                ) {
//                    Text(
//                        text = "Specialization: ${user.specification}",
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//
//                    Text(
//                        text = "Address: ${user.address}",
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//
//                    Text(
//                        text = "Consultation price: ${user.consultationPrice} PLN",
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//
//                    Text(
//                        text = "Availability: ${user.availability.ifEmpty { "Not specified" }}",
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//
//                    Text(
//                        text = "Rating: ${user.rating?.toString() ?: "No ratings"}",
//                        color = DefaultOnPrimary.copy(alpha = 0.8f),
//                        fontSize = 14.sp
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun RoleBadge(role: String) {
//    val (backgroundColor, roleTextColor) = when (role) {
//        "admin" -> DefaultPrimary.copy(alpha = 0.2f) to DefaultPrimary
//        "doctor" -> Color(0xFFCB52C8).copy(alpha = 0.2f) to Color(0xFFCB52C8)
//        else -> Color(0xFFEF55DB).copy(alpha = 0.2f) to Color(0xFFEF55DB)
//    }
//
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(8.dp))
//            .background(backgroundColor)
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//    ) {
//        Text(
//            text = when (role) {
//                "admin" -> "Admin"
//                "doctor" -> "Doctor"
//                else -> "User"
//            },
//            color = roleTextColor,
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
//@Composable
//private fun EditUserDialog(
//    user: User,
//    onDismiss: () -> Unit,
//    onSave: () -> Unit,
//    onUserChange: (User) -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 50.dp),
//        title = {
//            Text(
//                "Edit User",
//                color = DefaultPrimary,
//                fontWeight = FontWeight.SemiBold
//            )
//        },
//        text = {
//            Column(
//                modifier = Modifier
//                    .verticalScroll(rememberScrollState())
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                // Basic user info
//                OutlinedTextField(
//                    value = user.name,
//                    onValueChange = { onUserChange(user.copy(name = it)) },
//                    label = { Text("First Name") },
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = DefaultBackground,
//                        unfocusedContainerColor = DefaultBackground,
//                        focusedIndicatorColor = DefaultPrimary,
//                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                    )
//                )
//
//                OutlinedTextField(
//                    value = user.surname,
//                    onValueChange = { onUserChange(user.copy(surname = it)) },
//                    label = { Text("Last Name") },
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = DefaultBackground,
//                        unfocusedContainerColor = DefaultBackground,
//                        focusedIndicatorColor = DefaultPrimary,
//                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                    )
//                )
//
//                OutlinedTextField(
//                    value = user.email,
//                    onValueChange = { onUserChange(user.copy(email = it)) },
//                    label = { Text("Email") },
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = DefaultBackground,
//                        unfocusedContainerColor = DefaultBackground,
//                        focusedIndicatorColor = DefaultPrimary,
//                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                    )
//                )
//
//                OutlinedTextField(
//                    value = user.dob,
//                    onValueChange = { onUserChange(user.copy(dob = it)) },
//                    label = { Text("Date of Birth") },
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = TextFieldDefaults.colors(
//                        focusedContainerColor = DefaultBackground,
//                        unfocusedContainerColor = DefaultBackground,
//                        focusedIndicatorColor = DefaultPrimary,
//                        unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                    )
//                )
//
//                Text("Role", style = MaterialTheme.typography.labelLarge, color = DefaultPrimary)
//                RoleSelection(
//                    selectedRole = user.role,
//                    onRoleSelected = { onUserChange(user.copy(role = it)) }
//                )
//
//                // Doctor-specific fields (shown only when role is doctor)
//                if (user.role == "doctor") {
//                    Text(
//                        "Doctor Details",
//                        style = MaterialTheme.typography.labelLarge,
//                        color = DefaultPrimary
//                    )
//
//                    OutlinedTextField(
//                        value = user.specification,
//                        onValueChange = { onUserChange(user.copy(specification = it)) },
//                        label = { Text("Specialization") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = DefaultBackground,
//                            unfocusedContainerColor = DefaultBackground,
//                            focusedIndicatorColor = DefaultPrimary,
//                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                        )
//                    )
//
//                    OutlinedTextField(
//                        value = user.address,
//                        onValueChange = { onUserChange(user.copy(address = it)) },
//                        label = { Text("Address") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = DefaultBackground,
//                            unfocusedContainerColor = DefaultBackground,
//                            focusedIndicatorColor = DefaultPrimary,
//                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                        )
//                    )
//
//                    OutlinedTextField(
//                        value = user.consultationPrice,
//                        onValueChange = { onUserChange(user.copy(consultationPrice = it)) },
//                        label = { Text("Consultation Price (PLN)") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = DefaultBackground,
//                            unfocusedContainerColor = DefaultBackground,
//                            focusedIndicatorColor = DefaultPrimary,
//                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                        )
//                    )
//
//                    OutlinedTextField(
//                        value = user.availability,
//                        onValueChange = { onUserChange(user.copy(availability = it)) },
//                        label = { Text("Availability") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = DefaultBackground,
//                            unfocusedContainerColor = DefaultBackground,
//                            focusedIndicatorColor = DefaultPrimary,
//                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                        )
//                    )
//
//                    OutlinedTextField(
//                        value = user.rating?.toString() ?: "",
//                        onValueChange = {
//                            val newRating = it.toDoubleOrNull()
//                            onUserChange(user.copy(rating = newRating))
//                        },
//                        label = { Text("Rating") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = TextFieldDefaults.colors(
//                            focusedContainerColor = DefaultBackground,
//                            unfocusedContainerColor = DefaultBackground,
//                            focusedIndicatorColor = DefaultPrimary,
//                            unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
//                        )
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = onSave,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = DefaultPrimary,
//                    contentColor = Color.White
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text("Save Changes")
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    "Cancel",
//                    color = DefaultPrimary
//                )
//            }
//        },
//        shape = RoundedCornerShape(16.dp),
//        containerColor = DefaultBackground
//    )
//}
//
//@Composable
//private fun RoleSelection(
//    selectedRole: String,
//    onRoleSelected: (String) -> Unit
//) {
//    val roles = listOf("user", "doctor", "admin")
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        roles.forEach { role ->
//            FilterChip(
//                selected = selectedRole == role,
//                onClick = { onRoleSelected(role) },
//                label = {
//                    Text(
//                        role.capitalize(),
//                        color = if (selectedRole == role) Color.White else DefaultPrimary
//                    )
//                },
//                modifier = Modifier.weight(1f),
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = DefaultPrimary,
//                    selectedLabelColor = Color.White,
//                    containerColor = Color.Transparent,
//                    labelColor = DefaultPrimary
//                ),
//                shape = RoundedCornerShape(8.dp)
//            )
//        }
//    }
}
