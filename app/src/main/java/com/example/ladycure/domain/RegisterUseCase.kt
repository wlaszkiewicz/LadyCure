package com.example.ladycure.domain

import com.example.ladycure.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        name: String,
        surname: String,
        dateOfBirth: String,
        password: String
    ): Result<String> {
        return authRepository.register(email, name, surname, dateOfBirth, password, role = "user")
    }
}