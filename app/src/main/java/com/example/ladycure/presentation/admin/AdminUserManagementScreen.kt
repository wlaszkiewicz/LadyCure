package com.example.ladycure.presentation.admin

import AdminUserManagementViewModel
import DefaultPrimary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ladycure.presentation.admin.components.AddUserDialog
import com.example.ladycure.presentation.admin.components.AdminSearchBar
import com.example.ladycure.presentation.admin.components.DeleteConfirmationDialog
import com.example.ladycure.presentation.admin.components.EditUserDialog
import com.example.ladycure.presentation.admin.components.EmptyView
import com.example.ladycure.presentation.admin.components.LoadingView
import com.example.ladycure.presentation.admin.components.UserList
import com.example.ladycure.utility.SnackbarController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    snackbarController: SnackbarController,
    viewModel: AdminUserManagementViewModel = viewModel()
) {
    val errorMessage = viewModel.errorMessage
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarController.showMessage(it)
            viewModel.errorMessage = null // Reset error message after showing
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User Management",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )

            IconButton(
                onClick = { viewModel.loadUsers() },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh Users",
                    tint = DefaultPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        AdminSearchBar(
            searchQuery = viewModel.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (viewModel.isLoadingUsers) {
            LoadingView()
        } else if (viewModel.filteredUsers.isEmpty()) {
            EmptyView(
                if (viewModel.searchQuery.isBlank()) "No users found."
                else "No users match your search."
            )
        } else {
            UserList(
                users = viewModel.filteredUsers,
                onEditClick = { viewModel.showEditUserDialog(it) },
                onDeleteClick = { viewModel.showDeleteUserDialog(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (viewModel.showAddUserDialog) {
        AddUserDialog(
            user = viewModel.newUser,
            onDismiss = { viewModel.dismissAddUserDialog() },
            onSave = { viewModel.addUser() },
            onUserChange = { viewModel.updateNewUser(it) }
        )
    }

    if (viewModel.showEditUserDialog && viewModel.editedUser != null) {
        EditUserDialog(
            user = viewModel.editedUser!!,
            onDismiss = { viewModel.dismissEditUserDialog() },
            onSave = { viewModel.saveUserChanges() },
            onUserChange = { viewModel.updateEditedUser(it) }
        )
    }

    if (viewModel.showDeleteUserDialog && viewModel.selectedUser != null) {
        DeleteConfirmationDialog(
            user = viewModel.selectedUser!!,
            onDismiss = { viewModel.dismissDeleteUserDialog() },
            onConfirm = { viewModel.deleteUser() }
        )
    }
}