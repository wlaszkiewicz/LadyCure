package com.example.ladycure.presentation.admin

import com.example.ladycure.presentation.admin.AdminUserManagementViewModel
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ladycure.presentation.admin.components.AddUserDialog
import com.example.ladycure.presentation.admin.components.AdminSearchBar
import com.example.ladycure.presentation.admin.components.DeleteConfirmationDialog
import com.example.ladycure.presentation.admin.components.EditUserDialog
import com.example.ladycure.presentation.admin.components.EmptyView
import com.example.ladycure.presentation.admin.components.LoadingView
import com.example.ladycure.presentation.admin.components.UserList
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    snackbarController: SnackbarController,
    viewModel: AdminUserManagementViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
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
            .padding(horizontal = dimens.w(0.039f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.h(0.022f)),
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
                modifier = Modifier.size(dimens.w(0.073f))
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

        Spacer(modifier = Modifier.height(dimens.h(0.011f)))

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
