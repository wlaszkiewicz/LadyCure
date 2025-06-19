package com.example.ladycure.presentation.availability.components

import DefaultPrimary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun RecurringPatternDialog(
    selectedDaysOfWeek: Set<DayOfWeek>,
    onApply: (Set<DayOfWeek>, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var durationWeeks by remember { mutableStateOf(4) }
    val tempSelectedDays = remember { mutableStateOf(selectedDaysOfWeek.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Recurring Pattern",
                style = MaterialTheme.typography.titleMedium.copy(color = DefaultPrimary)
            )
        },
        text = {
            Column {
                Text(
                    "Select days of the week:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                // Day of week selection
                val days = DayOfWeek.values()
                LazyColumn {
                    items(days) { day ->
                        val isSelected = tempSelectedDays.value.contains(day)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelectedDays.value = if (isSelected) {
                                        tempSelectedDays.value - day
                                    } else {
                                        tempSelectedDays.value + day
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    tempSelectedDays.value = if (checked) {
                                        tempSelectedDays.value + day
                                    } else {
                                        tempSelectedDays.value - day
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DefaultPrimary,
                                    uncheckedColor = DefaultPrimary.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Duration options
                Text("Repeat for how many weeks?", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (durationWeeks > 1) durationWeeks-- },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Decrease")
                    }
                    Text(
                        "$durationWeeks weeks",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(
                        onClick = { if (durationWeeks < 12) durationWeeks++ },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = DefaultPrimary
                        )
                    ) {
                        Icon(Icons.Default.ChevronRight, "Increase")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(tempSelectedDays.value, durationWeeks) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DefaultPrimary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}