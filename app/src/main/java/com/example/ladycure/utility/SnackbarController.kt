package com.example.ladycure.utility

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarController(
    private val scope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState
) {
    fun showSnackbar(message: String, actionLabel: String? = null) {
        scope.launch {
            snackbarHostState.showSnackbar(message, actionLabel)
//                when (result) {
//                    SnackbarResult.ActionPerformed -> {
//                        // Handle action click if needed
//                    }
//                    SnackbarResult.Dismissed -> {
//                        // Handle dismissal if needed
//                    }
//                }
        }
    }
}

enum class SnackbarType {
    ERROR, SUCCESS, INFO
}

