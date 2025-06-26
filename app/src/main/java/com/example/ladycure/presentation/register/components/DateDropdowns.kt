package com.example.ladycure.presentation.register.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
        /**
         * A Composable function that displays a button which, when clicked, opens a date picker dialog.
         *
         * @param selectedDate The [LocalDate] currently selected and displayed on the button.
         * @param onDateSelected A callback function invoked when a new date is selected in the dialog.
         * @param modifier [Modifier] to be applied to the button.
         */
fun DatePickerButton(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }
    val now = LocalDate.now()

    if (showDialog.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = IntRange(now.year - 100, now.year - 18),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val instant = Instant.ofEpochMilli(it)
                            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateSelected(date)
                        }
                        showDialog.value = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false }
                ) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedButton(
        onClick = { showDialog.value = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = DateTimeFormatter.ofPattern("MMM dd, yyyy").format(selectedDate),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}