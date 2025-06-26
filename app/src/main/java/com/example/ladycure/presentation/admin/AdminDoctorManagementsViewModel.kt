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
import java.text.SimpleDateFormat
import java.util.Locale

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
            // Validate all fields before proceeding
            val validationError = validateDoctor(doctor)
            if (validationError != null) {
                errorMessage = validationError
                return@let
            }

            viewModelScope.launch {
                if (doctor.role == Role.DOCTOR) {
                    val updates = buildUpdateMap(doctor)
                    val result = userRepo.updateUser(doctor.id, updates)
                    errorMessage =
                        if (result.isSuccess) "Doctor updated successfully."
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
    private fun validateDoctor(doctor: Doctor): String? {
        return when {
            doctor.name.isBlank() -> "Name cannot be empty"
            doctor.name.length > 50 -> "Name is too long (max 50 characters)"
            doctor.surname.isBlank() -> "Surname cannot be empty"
            doctor.surname.length > 50 -> "Surname is too long (max 50 characters)"
            doctor.email.isBlank() -> "Email cannot be empty"
            !isValidEmail(doctor.email) -> "Please enter a valid email address"
            doctor.dateOfBirth.isBlank() -> "Date of birth cannot be empty"
            !isValidBirthDate(doctor.dateOfBirth) -> "Date of birth must be in yyyy-MM-dd format"
            doctor.phone.isBlank() -> "Phone number cannot be empty"
            !isValidPhone(doctor.phone) -> "Please enter a valid phone number"
            doctor.address.isBlank() -> "Address cannot be empty"
            doctor.city.isBlank() -> "City cannot be empty"
            doctor.consultationPrice <= 0 -> "Consultation price must be positive"
            doctor.experience < 0 -> "Experience cannot be negative"
            doctor.bio.isBlank() -> "Bio cannot be empty"
            doctor.bio.length < 20 -> "Bio should be at least 20 characters"
            doctor.languages.isEmpty() -> "At least one language must be specified"
            else -> null
        }
    }

    // Validation helper functions
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidBirthDate(date: String): Boolean {
        val pattern = Regex("""^\d{4}-\d{2}-\d{2}$""")
        if (!pattern.matches(date)) return false

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("""^[+]?[\d\s-]{6,15}$"""))
    }

}