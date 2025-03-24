package com.example.ladycure.domain

import com.example.ladycure.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.register(email, password)
    }
}