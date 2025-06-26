package com.example.ladycure.presentation.admin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.User

/**
 * Displays a card representing a [User], showing profile picture, name, email,
 * date of birth, and role badge. Includes buttons to edit or delete the user.
 *
 * @param user The user data to display.
 * @param onEditClick Callback invoked when the edit button is clicked.
 * @param onDeleteClick Callback invoked when the delete button is clicked.
 */
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

/**
 * Dialog for editing an existing [User]. Displays a [UserForm] for input and
 * buttons to save changes or cancel editing.
 *
 * @param user The current user data to edit.
 * @param onDismiss Callback invoked to dismiss the dialog.
 * @param onSave Callback invoked to save changes.
 * @param onUserChange Callback triggered when any user field is updated.
 */
@Composable
fun EditUserDialog(
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
                onUserChange = onUserChange
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

/**
 * Dialog for adding a new [User]. Displays a [UserForm] for input and
 * buttons to create the user or cancel the operation.
 *
 * @param user The initial user data (usually empty).
 * @param onDismiss Callback invoked to dismiss the dialog.
 * @param onSave Callback invoked to save the new user.
 * @param onUserChange Callback triggered when any user field is updated.
 */
@Composable
fun AddUserDialog(
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
                onUserChange = onUserChange
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

/**
 * Form for entering or editing [User] details including name, surname, email,
 * date of birth, and role. If the role is [Role.DOCTOR], shows additional
 * doctor-specific fields using [DoctorDetailsDialogSection].
 *
 * @param user The user data to display and edit.
 * @param onUserChange Callback triggered when the user data is modified.
 */
@Composable
private fun UserForm(
    user: User,
    onUserChange: (User) -> Unit
) {

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
            label = { Text("Date of Birth (yyyy-MM-dd)") },
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


/**
 * Displays a list of [User]s in a vertical scrolling list. Each user is
 * represented by a [UserCard] with edit and delete actions.
 *
 * @param users The list of users to display.
 * @param onEditClick Callback invoked with the selected user when the edit button is clicked.
 * @param onDeleteClick Callback invoked with the selected user when the delete button is clicked.
 * @param modifier Optional [Modifier] to be applied to the list container.
 */
@Composable
fun UserList(
    users: List<User>,
    onEditClick: (User) -> Unit,
    onDeleteClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
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

