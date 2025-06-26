import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.User
import com.example.ladycure.presentation.admin.components.buildUpdateMap
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AdminUserManagementViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {
    // Dialog visibility states
    var showAddUserDialog by mutableStateOf(false)
        private set

    var showEditUserDialog by mutableStateOf(false)
        private set

    var showDeleteUserDialog by mutableStateOf(false)
        private set

    // User selection states
    var selectedUser by mutableStateOf<User?>(null)
        private set

    var editedUser by mutableStateOf<User?>(null)
        private set

    // New user state
    var newUser by mutableStateOf(User.empty().copy(role = Role.USER))
        private set

    // Search and loading states
    var searchQuery by mutableStateOf("")
        private set

    var isLoadingUsers by mutableStateOf(false)
        private set

    // Data state
    var users by mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        internal set

    // Computed properties
    val allUsers
        get() = users
            .filter { it["role"] != Role.DOCTOR.value }
            .map { User.fromMap(it) }

    val filteredUsers
        get() = if (searchQuery.isBlank()) {
            allUsers
        } else {
            allUsers.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true)
            }
        }

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoadingUsers = true
            val result = userRepo.getUsers()
            if (result.isSuccess) {
                users = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = "Failed to load users: ${result.exceptionOrNull()?.message}"
            }
            isLoadingUsers = false
        }
    }

    // Search functionality
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // Dialog control methods
    fun showAddUserDialog() {
        showAddUserDialog = true
    }

    fun dismissAddUserDialog() {
        showAddUserDialog = false
    }

    fun showEditUserDialog(user: User) {
        selectedUser = user
        editedUser = user.copy()
        showEditUserDialog = true
    }

    fun dismissEditUserDialog() {
        showEditUserDialog = false
    }

    fun showDeleteUserDialog(user: User) {
        selectedUser = user
        showDeleteUserDialog = true
    }

    fun dismissDeleteUserDialog() {
        showDeleteUserDialog = false
    }

    // User data modification methods
    fun updateNewUser(user: User) {
        newUser = user
    }

    fun updateEditedUser(user: User) {
        editedUser = user
    }

    fun saveUserChanges() {
        viewModelScope.launch {
            selectedUser?.let { originalUser ->
                // Validate before proceeding
                when {
                    editedUser?.name.isNullOrBlank() -> {
                        errorMessage = "Name cannot be empty"
                        return@let
                    }
                    editedUser?.surname.isNullOrBlank() -> {
                        errorMessage = "Surname cannot be empty"
                        return@let
                    }
                    editedUser?.email.isNullOrBlank() -> {
                        errorMessage = "Email cannot be empty"
                        return@let
                    }
                    !isValidEmail(editedUser?.email.orEmpty()) -> {
                        errorMessage = "Please enter a valid email address"
                        return@let
                    }
                    editedUser?.dateOfBirth.isNullOrBlank() -> {
                        errorMessage = "Date of birth cannot be empty"
                        return@let
                    }
                    !isValidBirthDate(editedUser?.dateOfBirth.orEmpty()) -> {
                        errorMessage = "Date of birth must be in yyyy-MM-dd format"
                        return@let
                    }
                }

                val updates = buildUpdateMap(editedUser!!)
                val result = userRepo.updateUser(originalUser.id, updates)

                if (result.isSuccess) {
                    errorMessage = if (editedUser!!.role == Role.DOCTOR) {
                        "User converted to doctor successfully"
                    } else {
                        "User updated successfully"
                    }
                    showEditUserDialog = false
                    loadUsers()
                } else {
                    errorMessage = "Failed to update user: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    // Helper function for email validation
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun isValidBirthDate(date: String): Boolean {
        val pattern = Regex("""^\d{4}-\d{2}-\d{2}$""") // yyyy-MM-dd format
        if (!pattern.matches(date)) return false

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false // Strict parsing
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addUser() {
        viewModelScope.launch {
            // Uncomment when ready to implement
            // val result = authRepo.createUserInAuthAndDb(newUser.copy(role = Role.USER), "Password123")
            // if (result.isSuccess) {
            //     snackbarController.showMessage("User created successfully.")
            //     loadUsers()
            // } else {
            //     snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
            // }
            showAddUserDialog = false
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            // Uncomment when ready to implement
            // val result = authRepo.deleteUserAccount(selectedUser!!.id)
            // if (result.isSuccess) {
            //     snackbarController.showMessage("User deleted.")
            //     loadUsers()
            // } else {
            //     snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
            // }
            showDeleteUserDialog = false
        }
    }
}