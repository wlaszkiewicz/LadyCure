package com.example.ladycure.presentation.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.User
import com.example.ladycure.presentation.admin.components.buildUpdateMap
import kotlinx.coroutines.launch

class AdminDoctorManagementViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {
    var showAddDoctorDialog by mutableStateOf(false)
        private set

    var showEditDoctorDialog by mutableStateOf(false)
        private set

    var showDeleteDoctorDialog by mutableStateOf(false)
        private set

    var selectedDoctor by mutableStateOf<Doctor?>(null)
        private set

    var editedDoctor by mutableStateOf<Doctor?>(null)
        private set

    var newDoctorAsUser by mutableStateOf(User.empty().copy(role = Role.DOCTOR))
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoadingDoctors by mutableStateOf(false)
        private set

    var users by mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    val allDoctors
        get() = users
            .filter { it["role"] == Role.DOCTOR.value }
            .map { Doctor.fromMap(it) }

    var errorMessage by mutableStateOf<String?>(null)
        internal set

    val filteredDoctors
        get() = if (searchQuery.isBlank()) allDoctors else {
            allDoctors.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true) ||
                        it.speciality.displayName.contains(searchQuery, true) ||
                        it.address.contains(searchQuery, true) ||
                        it.city.contains(searchQuery, true)
            }
        }

    init {
        loadDoctors()
    }

    fun loadDoctors() {
        viewModelScope.launch {
            isLoadingDoctors = true
            val result = userRepo.getUsers()
            if (result.isSuccess) {
                users = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = "Failed to load doctors: ${result.exceptionOrNull()?.message}"
            }
            isLoadingDoctors = false
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun showAddDoctorDialog() {
        showAddDoctorDialog = true
    }

    fun dismissAddDoctorDialog() {
        showAddDoctorDialog = false
    }

    fun showEditDoctorDialog(doctor: Doctor) {
        selectedDoctor = doctor
        editedDoctor = doctor.copyDoc()
        showEditDoctorDialog = true
    }

    fun dismissEditDoctorDialog() {
        showEditDoctorDialog = false
    }

    fun showDeleteDoctorDialog(doctor: Doctor) {
        selectedDoctor = doctor
        showDeleteDoctorDialog = true
    }

    fun dismissDeleteDoctorDialog() {
        showDeleteDoctorDialog = false
    }

    fun updateNewDoctor(user: User) {
        newDoctorAsUser = user
    }

    fun updateEditedDoctor(doctor: Doctor) {
        editedDoctor = doctor
    }

    fun saveDoctorChanges() {
        editedDoctor?.let { doctor ->
            viewModelScope.launch {
                if (doctor.role == Role.DOCTOR) {
                    val updates = buildUpdateMap(doctor)
                    val result = userRepo.updateUser(doctor.id, updates)
                    errorMessage =
                        if (result.isSuccess) "Doctor updated."
                        else "Error: ${result.exceptionOrNull()?.message}"

                } else {
                    val userToUpdate = doctor.toUser()
                    val updates = buildUpdateMap(userToUpdate)
                    val result = userRepo.docToUserUpdate(doctor.id, updates)
                    errorMessage =
                        if (result.isSuccess) "Doctor demoted to User."
                        else "Error: ${result.exceptionOrNull()?.message}"

                }
                loadDoctors()
                showEditDoctorDialog = false
            }
        }
    }

    fun deleteDoctor() {
        selectedDoctor?.let { doctor ->
            viewModelScope.launch {
                // Uncomment when ready to implement
                // val result = authRepo.deleteUserAccount(doctor.id)
                // if (result.isSuccess) {
                //     snackbarController.showMessage("Doctor deleted.")
                //     loadDoctors()
                // } else {
                //     snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
                // }
                showDeleteDoctorDialog = false
            }
        }
    }

    fun addDoctor() {
        viewModelScope.launch {
            // Uncomment when ready to implement
            // val result = authRepo.createUserInAuthAndDb(newDoctorAsUser, "Password123")
            // if (result.isSuccess) {
            //     snackbarController.showMessage("Doctor created successfully.")
            //     loadDoctors()
            // } else {
            //     snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
            // }
            showAddDoctorDialog = false
        }
    }
}