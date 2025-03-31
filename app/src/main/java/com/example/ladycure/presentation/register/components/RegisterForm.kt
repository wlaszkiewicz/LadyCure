package com.example.ladycure.presentation.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RegisterForm(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onDaySelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("📧 Email") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("🌸 Name") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = state.surname,
            onValueChange = onSurnameChange,
            label = { Text("💖 Surname") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🎂 Date of Birth",
            style =  MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().padding(horizontal = 14.dp),
            textAlign = TextAlign.Left
        )

        DateDropdowns(
            selectedDay = state.selectedDay,
            selectedMonth = state.selectedMonth,
            selectedYear = state.selectedYear,
            onDaySelected = onDaySelected,
            onMonthSelected = onMonthSelected,
            onYearSelected = onYearSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("🔒 Password") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("💜 Confirm Password") },
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onRegisterClick() })
        )

        Button(
            onClick = onRegisterClick,
            enabled = state.isValid() && !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDA70D6),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (state.isSuccess) {
                Text("❤", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("✨ Register Now! ✨", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        state.errorMessage?.let {
            Text(
                text = "⚠️ Oopsie! ${it} Try again, cutie! 💕",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
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
        onDaySelected = {},
        onMonthSelected = {},
        onYearSelected = {},
        onPasswordChange = {},
        onConfirmPasswordChange = {},
        onRegisterClick = {}
    )
}