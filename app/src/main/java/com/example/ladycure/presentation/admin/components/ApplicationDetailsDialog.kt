package com.example.ladycure.presentation.admin.components

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ladycure.R
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.DoctorApplication

/**
 * Displays a detailed dialog for reviewing and managing a doctor's application.
 *
 * Shows personal details, contact info, documents (license, diploma),
 * and the current application status with options to approve, reject,
 * or request more information. Allows editing review notes and viewing documents.
 *
 * @param application The [DoctorApplication] object containing all application details.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onStatusChange Callback invoked when the application status is changed.
 *        Parameters:
 *        - new status ([ApplicationStatus]),
 *        - optional comment ([String]?) related to the status change,
 *        - boolean indicating whether notes have been edited inline (`true`) or via dialog (`false`).
 * @param onApprove Callback invoked specifically when the application is approved.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ApplicationDetailsDialog(
    application: DoctorApplication,
    onDismiss: () -> Unit,
    onStatusChange: (ApplicationStatus, String?, Boolean) -> Unit,
    onApprove: () -> Unit,
) {

    var showLicense by remember { mutableStateOf(false) }
    var showDiploma by remember { mutableStateOf(false) }

    var editedComment by remember { mutableStateOf(application.reviewNotes ?: "No notes") }
    var showEditComment by remember { mutableStateOf(false) }
    var showStatusChangeDialog by remember { mutableStateOf<ApplicationStatus?>(null) }

    val statusColor = when (application.status) {
        ApplicationStatus.PENDING -> Yellow
        ApplicationStatus.APPROVED -> Green
        ApplicationStatus.REJECTED -> Red
        ApplicationStatus.NEEDS_MORE_INFO -> BabyBlue
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${application.firstName} ${application.lastName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            application.speciality.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = application.status.displayName.replace("_", " ")
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = statusColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow("Email", application.email)
                    DetailRow("Phone", application.phoneNumber)
                    DetailRow("Experience", "${application.yearsOfExperience} years")
                    DetailRow("Workplace", application.currentWorkplace)
                    DetailRow("License", application.licenseNumber.ifEmpty { "Not provided" })
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (application.licensePhotoUrl.isNotEmpty() || application.diplomaPhotoUrl.isNotEmpty()) {
                    Text(
                        "Documents",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DefaultPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (application.licensePhotoUrl.isNotEmpty()) {
                        TextButton(
                            onClick = { showLicense = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_license),
                                contentDescription = "View License",
                                tint = DefaultPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Medical License")
                        }
                    }

                    if (application.diplomaPhotoUrl.isNotEmpty()) {
                        TextButton(
                            onClick = { showDiploma = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_diplome),
                                contentDescription = "View Diploma",
                                tint = DefaultPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Diploma")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DefaultPrimary
                            )
                        )

                        IconButton(
                            onClick = { showEditComment = !showEditComment },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showEditComment) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = if (showEditComment) "Close" else "Edit",
                                tint = DefaultPrimary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !showEditComment,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = editedComment,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = showEditComment,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            TextField(
                                value = editedComment,
                                onValueChange = { editedComment = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = DefaultPrimary,
                                    unfocusedIndicatorColor = DefaultPrimary.copy(alpha = 0.5f)
                                ),
                                placeholder = {
                                    Text("Add notes about this application")
                                },
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showEditComment = false }) {
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        showEditComment = false
                                        onStatusChange(
                                            application.status,
                                            editedComment.ifEmpty { null }, true
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (application.status) {
                    ApplicationStatus.PENDING -> {
                        OutlinedButton(
                            onClick = {
                                showStatusChangeDialog = ApplicationStatus.APPROVED
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Green),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Green
                            )
                        ) {
                            Text("Approve")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showStatusChangeDialog = ApplicationStatus.NEEDS_MORE_INFO
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BabyBlue),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = BabyBlue
                                )
                            ) {
                                Text("More Info")
                            }

                            OutlinedButton(
                                onClick = { showStatusChangeDialog = ApplicationStatus.REJECTED },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Red),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Red
                                )
                            ) {
                                Text("Reject")
                            }
                        }
                    }

                    ApplicationStatus.NEEDS_MORE_INFO -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onStatusChange(
                                        ApplicationStatus.APPROVED,
                                        editedComment.ifEmpty { null }, false
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Green),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Green
                                )
                            ) {
                                Text("Approve")
                            }

                            OutlinedButton(
                                onClick = { showStatusChangeDialog = ApplicationStatus.REJECTED },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Red),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Red
                                )
                            ) {
                                Text("Reject")
                            }
                        }
                    }

                    ApplicationStatus.APPROVED -> {
                        Text(
                            "This application has been approved.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Green.copy(alpha = 0.9f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    ApplicationStatus.REJECTED -> {
                        Text(
                            "This application has already been processed and rejected.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Red.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    showStatusChangeDialog?.let { newStatus ->
        var tempComment by remember { mutableStateOf(editedComment) }

        AlertDialog(
            onDismissRequest = { showStatusChangeDialog = null },
            title = {
                Text(
                    when (newStatus) {
                        ApplicationStatus.REJECTED -> "Reject Application?"
                        ApplicationStatus.NEEDS_MORE_INFO -> "Request More Info?"
                        ApplicationStatus.APPROVED -> "Approve Application?"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = when (newStatus) {
                            ApplicationStatus.REJECTED -> Red.copy(alpha = 0.8f)
                            ApplicationStatus.NEEDS_MORE_INFO -> BabyBlue
                            ApplicationStatus.APPROVED -> Green.copy(alpha = 0.8f)
                            else -> DefaultPrimary
                        }
                    )
                )
            },
            text = {
                Column {
                    Text(
                        when (newStatus) {
                            ApplicationStatus.REJECTED -> "Please provide a reason for rejection:"
                            ApplicationStatus.NEEDS_MORE_INFO -> "What additional information is needed?"
                            ApplicationStatus.APPROVED -> "Are you sure you want to approve this application?"
                            else -> ""
                        }
                    )

                    if (newStatus == ApplicationStatus.REJECTED || newStatus == ApplicationStatus.NEEDS_MORE_INFO) {

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = tempComment,
                            onValueChange = { tempComment = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter your notes here")
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (newStatus) {
                            ApplicationStatus.APPROVED -> {
                                onApprove()
                            }

                            else -> {
                                onStatusChange(newStatus, tempComment.ifEmpty { null }, false)
                            }
                        }
                        showStatusChangeDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (newStatus) {
                            ApplicationStatus.REJECTED -> Red.copy(alpha = 0.8f)
                            ApplicationStatus.NEEDS_MORE_INFO -> BabyBlue
                            ApplicationStatus.APPROVED -> Green.copy(alpha = 0.8f)
                            else -> DefaultPrimary
                        },
                        contentColor = Color.White
                    ),
                    enabled = when (newStatus) {
                        ApplicationStatus.APPROVED -> true
                        else -> tempComment.isNotEmpty()
                    }
                ) {
                    Text(
                        when (newStatus) {
                            ApplicationStatus.REJECTED -> "Confirm Reject"
                            ApplicationStatus.NEEDS_MORE_INFO -> "Request Info"
                            ApplicationStatus.APPROVED -> "Confirm Approve"
                            else -> "Confirm"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStatusChangeDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLicense && application.licensePhotoUrl.isNotEmpty()) {
        ImageViewDialog(
            imageUrl = application.licensePhotoUrl,
            title = "Medical License",
            onDismiss = { showLicense = false }
        )
    }

    if (showDiploma && application.diplomaPhotoUrl.isNotEmpty()) {
        ImageViewDialog(
            imageUrl = application.diplomaPhotoUrl,
            title = "Diploma",
            onDismiss = { showDiploma = false }
        )
    }
}

/**
 * Displays a row with a label and a corresponding value, arranged
 * horizontally with space between them.
 *
 * Typically used to display application or user information fields.
 *
 * @param label The label text describing the field.
 * @param value The value text corresponding to the label.
 */
@Composable
internal fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}


