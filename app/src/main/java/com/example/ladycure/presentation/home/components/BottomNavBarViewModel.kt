package com.example.ladycure.presentation.home.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ladycure.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomNavBarViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var userRole by mutableStateOf<String?>(null)
        private set

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            val result = userRepository.getUserRole()
            if (result.isSuccess) {
                userRole = result.getOrNull()
            }
        }
    }
}
