package com.example.ladycure.presentation.register.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ladycure.presentation.register.RegisterUiState
import java.time.LocalDate

@Composable
fun RegisterForm(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val (emailFocus, firstNameFocus, lastNameFocus, passwordFocus, confirmPasswordFocus) = remember {
        List(5) { FocusRequester() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Email Field
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            isError = state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email)
                .matches(),
            supportingText = {
                if (state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email)
                        .matches()
                ) {
                    Text("Please enter a valid email")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { firstNameFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocus)
        )

        // Name Fields Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("First name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { lastNameFocus.requestFocus() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(firstNameFocus)
            )

            OutlinedTextField(
                value = state.surname,
                onValueChange = onSurnameChange,
                label = { Text("Last name") },
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Surname") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(lastNameFocus)
            )
        }

        // Date of Birth Section
        Text(
            text = "Date of Birth",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
        )

        DatePickerButton(
            selectedDate = state.selectedDate,
            onDateSelected = { date ->
                onDateSelected(date)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Error message for date of birth
        if (state.selectedDate.isAfter(LocalDate.now().minusYears(18))) {
            Text(
                text =
                    "We are sorry, you must be at least 18 years old",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }


        // Password Fields
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.password.isNotBlank() && state.password.length < 8
                    || state.password.isNotBlank() && !state.password.matches(Regex(".*[A-Z].*"))
                    || state.password.isNotBlank() && !state.password.matches(Regex(".*[0-9].*"))
                    || state.password.isNotBlank() && !state.password.matches(Regex(".*[!@#$%^&*].*")),
            supportingText = {
                if (state.password.isNotBlank() && state.password.length < 8) {
                    Text("Password must be at least 8 characters")
                } else if (state.password.isNotBlank() && !state.password.matches(Regex(".*[A-Z].*"))) {
                    Text("Password must contain at least one uppercase letter")
                } else if (state.password.isNotBlank() && !state.password.matches(Regex(".*[0-9].*"))) {
                    Text("Password must contain at least one number")
                } else if (state.password.isNotBlank() && !state.password.matches(Regex(".*[!@#$%^&*].*"))) {
                    Text("Password must contain at least one special character")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { confirmPasswordFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocus)
        )

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.LockReset,
                    contentDescription = "Confirm Password"
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.confirmPassword.isNotBlank() && state.password != state.confirmPassword,
            supportingText = {
                if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) {
                    Text("Passwords don't match")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (state.isValid()) {
                    onRegisterClick()
                    focusManager.clearFocus()
                } else {
                    when (state.getFirstInvalidField()) {
                        "email" -> emailFocus.requestFocus()
                        "firstName" -> firstNameFocus.requestFocus()
                        "lastName" -> lastNameFocus.requestFocus()
                        "password" -> passwordFocus.requestFocus()
                        "confirmPassword" -> confirmPasswordFocus.requestFocus()
                        // For date of birth, you might need special handling
                        "dob" -> {}
                    }
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocus)
        )

        // Register Button
        Button(
            onClick = onRegisterClick,
            enabled = state.isValid() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

