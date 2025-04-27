package com.example.ladycure.utility

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarController(
    private val scope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState
) {
    fun showMessage(message: String, actionLabel: String? = null) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel
            )
        }
    }
}

enum class SnackbarType {
    ERROR, SUCCESS, INFO
}

