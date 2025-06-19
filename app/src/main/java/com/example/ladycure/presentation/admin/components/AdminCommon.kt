package com.example.ladycure.presentation.admin.components

import DefaultPrimary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.User


@Composable
fun RoleBadge(role: Role) {
    val backgroundColor = when (role) {
        Role.USER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        Role.DOCTOR -> Color(0xFF5FB9C9).copy(alpha = 0.1f)
        else -> Color(0xFF7050AB).copy(alpha = 0.1f)
    }
    val textColor = when (role) {
        Role.USER -> MaterialTheme.colorScheme.primary
        Role.DOCTOR -> Color(0xFF5FB9C9)
        else -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = role.value,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun RatingBar(
    rating: Double,
    onRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    starCount: Int = 5
) {
    var showManualInput by remember { mutableStateOf(false) }
    var manualRating by remember { mutableStateOf(rating.toString()) }

    Column(modifier = modifier) {
        // Star rating and edit button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Star rating interface
            Row {
                for (i in 1..starCount) {
                    val starValue = i.toDouble()
                    val isHalfStar = (rating >= starValue - 0.5) && (rating < starValue)
                    val isFullStar = rating >= starValue

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                onRatingChange(starValue - 0.5)
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        onRatingChange(starValue - 0.5)
                                    },
                                    onDoubleTap = {
                                        onRatingChange(starValue)
                                    }
                                )
                            }
                    ) {
                        if (isFullStar) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Full star",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (isHalfStar) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = "Half star background",
                                    tint = Color.Gray,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.StarHalf,
                                    contentDescription = "Half star",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.CenterStart)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = "Empty star",
                                tint = Color.Gray,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Rating value display
            Text(
                text = "%.1f".format(rating),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Manual input toggle button
            IconButton(
                onClick = {
                    showManualInput = !showManualInput
                    if (!showManualInput) {
                        val newRating = manualRating.toDoubleOrNull()?.coerceIn(0.5, 5.0) ?: rating
                        onRatingChange(newRating)
                        manualRating = newRating.toString()
                    } else {
                        manualRating = rating.toString()
                    }
                }
            ) {
                Icon(
                    imageVector = if (showManualInput) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (showManualInput) "Close manual input" else "Edit manually",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Manual input field (shown when enabled)
        if (showManualInput) {
            OutlinedTextField(
                value = manualRating,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() ||
                        newValue.matches(Regex("^\\d*\\.?\\d*$")) &&
                        newValue.count { it == '.' } <= 1
                    ) {
                        manualRating = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Enter rating (0.5-5.0)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                trailingIcon = {
                    if (manualRating.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val newRating =
                                    manualRating.toDoubleOrNull()?.coerceIn(0.5, 5.0) ?: rating
                                onRatingChange(newRating)
                                manualRating = newRating.toString()
                                showManualInput = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply rating",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun RoleSelection(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Role.entries.forEach { role ->
            FilterChip(
                selected = selectedRole == role,
                onClick = { onRoleSelected(role) },
                label = { Text(role.value) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DefaultPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = { Text("Are you sure you want to delete ${user.name} ${user.surname}?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = DefaultPrimary)
    }
}

@Composable
fun EmptyView(selectedTab: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No ${selectedTab.lowercase()} found",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

fun buildUpdateMap(user: User): Map<String, Any> {
    return mutableMapOf<String, Any>().apply {
        put("name", user.name)
        put("surname", user.surname)
        put("email", user.email)
        put("role", user.role.value)
        put("dob", user.dateOfBirth)
        put("profilePictureUrl", user.profilePictureUrl)

        if (user is Doctor && user.role == Role.DOCTOR) {
            put("speciality", user.speciality.displayName)
            put("address", user.address)
            put("consultationPrice", user.consultationPrice)
            put("rating", user.rating)
            put("experience", user.experience)
            put("languages", user.languages)
            put("city", user.city)
            put("phone", user.phone)
            put("bio", user.bio)
        }
    }
}