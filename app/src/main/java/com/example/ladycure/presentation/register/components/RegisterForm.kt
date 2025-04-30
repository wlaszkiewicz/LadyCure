package com.example.ladycure.presentation.register.components
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladycure.presentation.register.RegisterUiState
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.util.Calendar

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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email Field
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
            isError = state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email).matches(),
            supportingText = {
                if (state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
                    Text("Please enter a valid email")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
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
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.surname,
                onValueChange = onSurnameChange,
                label = { Text("Last name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Surname") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.weight(1f)
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
            isError = state.password.isNotBlank() && state.password.length < 8,
            supportingText = {
                if (state.password.isNotBlank() && state.password.length < 8) {
                    Text("Password must be at least 8 characters")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.confirmPassword.isNotBlank() && state.password != state.confirmPassword,
            supportingText = {
                if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) {
                    Text("Passwords don't match")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onRegisterClick() }),
            modifier = Modifier.fillMaxWidth()
        )

        // Register Button
        Button(
            onClick = onRegisterClick,
            enabled = state.isValid() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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


@Composable
@Preview
fun RegisterFormPreview() {
    RegisterForm(
        state = RegisterUiState(),
        onEmailChange = {},
        onNameChange = {},
        onSurnameChange = {},
        onDateSelected = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = {}
    )
}

