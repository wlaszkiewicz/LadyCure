package com.example.ladycure.presentation.welcome

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

/**
 * ViewModel for the Welcome screen, handling user authentication and role management.
 *
 * @param authRepository Repository for authentication-related operations.
 * @param userRepository Repository for user-related data operations.
 */
class WelcomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    /** Indicates if the data is currently loading. */
    var isLoading by mutableStateOf(true)

    /** The currently authenticated Firebase user. */
    var currentUser by mutableStateOf<FirebaseUser?>(null)

    /** The role of the current user. */
    var userRole by mutableStateOf<String?>(null)

    /** Indicates if a biometric error should be displayed. */
    var showBiometricError by mutableStateOf(false)

    /** Indicates if the password dialog should be shown. */
    var showPasswordDialog by mutableStateOf(false)

    /** The password entered by the user. */
    var password by mutableStateOf("")

    /** Indicates if biometric or password authentication was successful. */
    var authenticationSuccess by mutableStateOf(false)

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    /**
     * Initializes the biometric authentication components.
     *
     * @param context The Android context, typically a FragmentActivity.
     */
    fun initializeBiometric(context: Context) {
        val executor = ContextCompat.getMainExecutor(context)

        biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showBiometricError = true
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticationSuccess = true
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            authRepository.updateFcmToken(token)
                        }
                }


                override fun onAuthenticationFailed() {
                    showBiometricError = true
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Authenticate to access LadyCure")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    /**
     * Sets up a listener for Firebase authentication state changes.
     * Updates [currentUser] and [userRole] accordingly.
     */
    fun setupAuthListener() {
        Firebase.auth.addAuthStateListener { auth ->
            currentUser = auth.currentUser
            isLoading = false

            if (currentUser != null) {
                viewModelScope.launch {
                    userRole = try {
                        userRepository.getUserRole().getOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    /**
     * Initiates biometric authentication. Handles different biometric states
     * and prompts the user accordingly or falls back to password authentication.
     *
     * @param context The Android context.
     */
    fun authenticateWithBiometrics(context: Context) {
        authenticationSuccess = false
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    context,
                    "No biometric features available on this device",
                    Toast.LENGTH_SHORT
                ).show()
                showPasswordDialog = true
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(
                    context,
                    "Biometric features are currently unavailable",
                    Toast.LENGTH_SHORT
                ).show()
                showPasswordDialog = true
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    context,
                    "Security update required for biometric authentication",
                    Toast.LENGTH_SHORT
                ).show()
                showPasswordDialog = true
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(
                    context,
                    "Biometric authentication is not supported on this device",
                    Toast.LENGTH_SHORT
                ).show()
                showPasswordDialog = true
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                context.startActivity(enrollIntent)
            }

            else -> {
                showPasswordDialog = true
            }
        }
    }

    /**
     * Authenticates the user with a password.
     *
     * @param navController The NavController for navigation actions.
     * @param context The Android context.
     */
    fun authenticateWithPassword(navController: NavController, context: Context) {
        viewModelScope.launch {
            try {
                authRepository.authenticate(
                    email = currentUser?.email ?: "",
                    password = password,
                    navController = navController,
                    onSuccess = {
                        authenticationSuccess = true
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(
                            context,
                            "Authentication failed: $errorMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Resets the authentication success state.
     */
    fun resetAuthState() {
        authenticationSuccess = false
    }
}