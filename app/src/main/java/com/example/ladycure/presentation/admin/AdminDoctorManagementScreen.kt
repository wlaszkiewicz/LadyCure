package com.example.ladycure.presentation.admin

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
import androidx.navigation.NavController
import com.example.ladycure.presentation.admin.components.AddUserDialog
import com.example.ladycure.presentation.admin.components.AdminSearchBar
import com.example.ladycure.presentation.admin.components.DeleteConfirmationDialog
import com.example.ladycure.presentation.admin.components.DoctorList
import com.example.ladycure.presentation.admin.components.EditDoctorDialog
import com.example.ladycure.presentation.admin.components.EmptyView
import com.example.ladycure.presentation.admin.components.LoadingView
import com.example.ladycure.utility.SnackbarController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorManagementScreen(
    snackbarController: SnackbarController,
    navController: NavController,
    viewModel: AdminDoctorManagementViewModel = viewModel()
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Doctor Management",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            IconButton(
                onClick = { viewModel.loadDoctors() },
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

        if (viewModel.isLoadingDoctors) {
            LoadingView()
        } else if (viewModel.filteredDoctors.isEmpty()) {
            EmptyView(
                if (viewModel.searchQuery.isBlank()) "No doctors found."
                else "No doctors match your search."
            )
        } else {
            DoctorList(
                doctors = viewModel.filteredDoctors,
                onEditClick = { viewModel.showEditDoctorDialog(it) },
                onDeleteClick = { viewModel.showDeleteDoctorDialog(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (viewModel.showAddDoctorDialog) {
        AddUserDialog(
            user = viewModel.newDoctorAsUser,
            onDismiss = { viewModel.dismissAddDoctorDialog() },
            onSave = { viewModel.addDoctor() },
            onUserChange = { viewModel.updateNewDoctor(it) }
        )
    }

    if (viewModel.showEditDoctorDialog && viewModel.editedDoctor != null) {
        EditDoctorDialog(
            doctor = viewModel.editedDoctor!!,
            onDismiss = { viewModel.dismissEditDoctorDialog() },
            onSave = { viewModel.saveDoctorChanges() },
            onDoctorChange = { viewModel.updateEditedDoctor(it) },
            onEditAvailabilityClick = {
                viewModel.editedDoctor?.let { doctor ->
                    navController.navigate("adminEditAvailability/${doctor.id}")
                }
            }
        )
    }

    if (viewModel.showDeleteDoctorDialog && viewModel.selectedDoctor != null) {
        DeleteConfirmationDialog(
            user = viewModel.selectedDoctor!!,
            onDismiss = { viewModel.dismissDeleteDoctorDialog() },
            onConfirm = { viewModel.deleteDoctor() }
        )
    }
}

