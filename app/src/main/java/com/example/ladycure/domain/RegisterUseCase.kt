package com.example.ladycure.domain

import com.example.ladycure.data.repository.AuthRepository

/**
 * Use case responsible for handling user registration.
 *
 * @property authRepository The repository responsible for authentication operations.
 */
class RegisterUseCase(private val authRepository: AuthRepository) {
    /**
     * Registers a new user with the provided details.
     *
     * @param email The user's email address.
     * @param name The user's first name.
     * @param surname The user's last name.
     * @param dateOfBirth The user's date of birth as a string.
     * @param password The password for the new account.
     * @return A [Result] containing a success message or an error.
     */
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