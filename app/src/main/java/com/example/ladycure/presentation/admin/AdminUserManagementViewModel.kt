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

/**
 * ViewModel for managing users in the admin panel.
 *
 * Provides functionality to load, search, add, edit, and delete users.
 * Handles UI state such as dialogs visibility and loading indicators,
 * as well as error messages.
 *
 * @property userRepo Repository for user data operations.
 * @property authRepo Repository for authentication-related operations.
 */
class AdminUserManagementViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    var showAddUserDialog by mutableStateOf(false)
        private set

    var showEditUserDialog by mutableStateOf(false)
        private set

    var showDeleteUserDialog by mutableStateOf(false)
        private set

    var selectedUser by mutableStateOf<User?>(null)
        private set

    var editedUser by mutableStateOf<User?>(null)
        private set

    var newUser by mutableStateOf(User.empty().copy(role = Role.USER))
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoadingUsers by mutableStateOf(false)
        private set

    var users by mutableStateOf<List<Map<String, Any>>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        internal set

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

    /**
     * Loads users from the repository.
     * Updates [users], [isLoadingUsers], and [errorMessage] accordingly.
     */
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

    /**
     * Updates the current search query string.
     *
     * @param query New search query.
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    /** Shows the Add User dialog. */
    fun showAddUserDialog() {
        showAddUserDialog = true
    }

    /** Dismisses the Add User dialog. */
    fun dismissAddUserDialog() {
        showAddUserDialog = false
    }

    /**
     * Shows the Edit User dialog for a selected user.
     *
     * @param user The user to edit.
     */
    fun showEditUserDialog(user: User) {
        selectedUser = user
        editedUser = user.copy()
        showEditUserDialog = true
    }

    /** Dismisses the Edit User dialog. */
    fun dismissEditUserDialog() {
        showEditUserDialog = false
    }

    /**
     * Shows the Delete User confirmation dialog for a selected user.
     *
     * @param user The user to delete.
     */
    fun showDeleteUserDialog(user: User) {
        selectedUser = user
        showDeleteUserDialog = true
    }

    /** Dismisses the Delete User dialog. */
    fun dismissDeleteUserDialog() {
        showDeleteUserDialog = false
    }

    /**
     * Updates the user data currently being created in the Add User dialog.
     *
     * @param user Updated user data.
     */
    fun updateNewUser(user: User) {
        newUser = user
    }

    /**
     * Updates the user data currently being edited in the Edit User dialog.
     *
     * @param user Updated user data.
     */
    fun updateEditedUser(user: User) {
        editedUser = user
    }

    /**
     * Validates and saves changes made to the edited user.
     * Shows appropriate error messages if validation fails.
     * Reloads users on success.
     */
    fun saveUserChanges() {
        viewModelScope.launch {
            selectedUser?.let { originalUser ->

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