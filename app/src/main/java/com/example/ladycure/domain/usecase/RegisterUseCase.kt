package com.example.ladycure.domain.usecase

import com.example.ladycure.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterUseCase @Inject constructor(private val authRepository: AuthRepository) {
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