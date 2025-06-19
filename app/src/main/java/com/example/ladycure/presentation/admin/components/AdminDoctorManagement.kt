package com.example.ladycure.presentation.admin.components

import DefaultPrimary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.Speciality

@Composable
fun DoctorDetailsDialogSection(
    doctor: Doctor,
    onDoctorChange: (Doctor) -> Unit
) {
    Divider()

    // Professional Information Section
    Text("Professional Information", style = MaterialTheme.typography.titleSmall)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Specialization Dropdown
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = doctor.speciality.displayName,
                onValueChange = {},
                label = { Text("Specialization") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Speciality.entries.forEach { speciality ->
                    DropdownMenuItem(
                        text = { Text(speciality.displayName) },
                        onClick = {
                            onDoctorChange(doctor.copyDoc(speciality = speciality))
                            expanded = false
                        }
                    )
                }
            }
        }

        // Location Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = doctor.city,
                onValueChange = { onDoctorChange(doctor.copyDoc(city = it)) },
                label = { Text("City") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.phone,
                onValueChange = { onDoctorChange(doctor.copyDoc(phone = it)) },
                label = { Text("Phone Number") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = doctor.address,
            onValueChange = { onDoctorChange(doctor.copyDoc(address = it)) },
            label = { Text("Full Address") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Divider()

    // Professional Details Section
    Text("Professional Details", style = MaterialTheme.typography.titleSmall)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = doctor.consultationPrice.toString(),
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null) {
                        onDoctorChange(
                            doctor.copyDoc(
                                consultationPrice = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                label = { Text("Consultation Fee") },
                modifier = Modifier.weight(1f),
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = doctor.experience.toString(),
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null) {
                        onDoctorChange(
                            doctor.copyDoc(
                                experience = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                label = { Text("Experience") },
                modifier = Modifier.weight(1f),
                suffix = { Text("years") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Rating with visual indicator
        Column {
            Text("Rating", style = MaterialTheme.typography.labelMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RatingBar(
                    rating = doctor.rating,
                    onRatingChange = { newRating ->
                        val clampedRating = newRating.coerceIn(0.5, 5.0)
                        onDoctorChange(doctor.copyDoc(rating = clampedRating))
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    Divider()

    // Languages Section
    Text("Languages", style = MaterialTheme.typography.titleSmall)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var newLanguage by remember { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newLanguage,
                onValueChange = { newLanguage = it },
                label = { Text("Add language") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (newLanguage.isNotBlank() && !doctor.languages.contains(newLanguage)) {
                        onDoctorChange(doctor.copyDoc(languages = doctor.languages + newLanguage))
                        newLanguage = ""
                    }
                },
                enabled = newLanguage.isNotBlank()
            ) {
                Text("Add")
            }
        }

        if (doctor.languages.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                doctor.languages.forEach { language ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(language) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onDoctorChange(
                                        doctor.copyDoc(
                                            languages = doctor.languages - language
                                        )
                                    )
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = DefaultPrimary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        } else {
            Text(
                "No languages added",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    Divider()

    // Biography Section
    Text("Biography", style = MaterialTheme.typography.titleSmall)
    OutlinedTextField(
        value = doctor.bio,
        onValueChange = { onDoctorChange(doctor.copyDoc(bio = it)) },
        label = { Text("Professional background") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        minLines = 3,
        maxLines = 5
    )
}


@Composable
fun EditDoctorDialog(
    doctor: Doctor,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDoctorChange: (Doctor) -> Unit,
    isSaving: Boolean = false
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                "Edit Doctor Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            DoctorForm(
                doctor = doctor,
                onDoctorChange = onDoctorChange
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary,
                    disabledContainerColor = DefaultPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { if (!isSaving) onDismiss() },
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
private fun DoctorForm(
    doctor: Doctor,
    onDoctorChange: (Doctor) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Information Section
        Text("Personal Information", style = MaterialTheme.typography.titleSmall)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = doctor.name,
                onValueChange = { onDoctorChange(doctor.copyDoc(name = it)) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.surname,
                onValueChange = { onDoctorChange(doctor.copyDoc(surname = it)) },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.email,
                onValueChange = { onDoctorChange(doctor.copyDoc(email = it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctor.dateOfBirth,
                onValueChange = { onDoctorChange(doctor.copyDoc(dateOfBirth = it)) },
                label = { Text("Date of Birth (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Role", style = MaterialTheme.typography.labelLarge)
            RoleSelection(
                selectedRole = doctor.role,
                onRoleSelected = { onDoctorChange(doctor.copyDoc(role = it)) }
            )
        }

        if (doctor.role == Role.DOCTOR) {
            DoctorDetailsDialogSection(
                doctor = doctor,
                onDoctorChange = { newDoctor ->
                    onDoctorChange(newDoctor)
                }
            )

        }
    }
}


@Composable
private fun DoctorDetailsSection(doctor: Doctor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Divider(modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Specialization",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    doctor.speciality.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Consultation Fee",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${doctor.consultationPrice} PLN",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Rating",
                    style = MaterialTheme.typography.labelSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${doctor.rating}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Experience",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${doctor.experience} years",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Address",
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            doctor.address,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun DoctorCard(
    doctor: Doctor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (doctor.profilePictureUrl.isEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.AccountBox,
                            contentDescription = "Profile picture",
                            tint = Color(0xFF5FB9C9),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        AsyncImage(
                            model = doctor.profilePictureUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${doctor.name} ${doctor.surname}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = doctor.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Date of Birth",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        doctor.dateOfBirth,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                RoleBadge(role = doctor.role)
            }
            DoctorDetailsSection(doctor = doctor)
        }
    }
}


@Composable
fun DoctorList(
    doctors: List<Doctor>,
    onEditClick: (Doctor) -> Unit,
    onDeleteClick: (Doctor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(doctors) { doctor ->
            DoctorCard(
                doctor = doctor,
                onEditClick = { onEditClick(doctor) },
                onDeleteClick = { onDeleteClick(doctor) }
            )
        }
    }
}
