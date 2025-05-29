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
import com.example.ladycure.repository.AuthRepository
import com.example.ladycure.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class WelcomeViewModel(private val authRepository: AuthRepository,
    private val userRepository: UserRepository) : ViewModel() {
    // States
    var isLoading by mutableStateOf(true)
    var currentUser by mutableStateOf<FirebaseUser?>(null)
    var userRole by mutableStateOf<String?>(null)
    var showBiometricError by mutableStateOf(false)
    var showPasswordDialog by mutableStateOf(false)
    var password by mutableStateOf("")
    var authenticationSuccess by mutableStateOf(false) // New state

    // Biometric
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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
                }

                override fun onAuthenticationFailed() {
                    showBiometricError = true
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Authenticate to access LadyCure")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

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
                Toast.makeText(context, "No biometric features available on this device", Toast.LENGTH_SHORT).show()
                showPasswordDialog = true
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(context, "Biometric features are currently unavailable", Toast.LENGTH_SHORT).show()
                showPasswordDialog = true
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(context, "Security update required for biometric authentication", Toast.LENGTH_SHORT).show()
                showPasswordDialog = true
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Toast.makeText(context, "Biometric authentication is not supported on this device", Toast.LENGTH_SHORT).show()
                showPasswordDialog = true
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                }
                context.startActivity(enrollIntent)
            }
            else -> {
                showPasswordDialog = true
            }
        }
    }

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
                        Toast.makeText(context, "Authentication failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetAuthState() {
        authenticationSuccess = false
    }
}