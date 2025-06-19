package com.example.ladycure.presentation.admin

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.R
import com.example.ladycure.data.repository.AdminRepository
import com.example.ladycure.data.repository.ApplicationRepository
import com.example.ladycure.data.repository.AuthRepository
import com.example.ladycure.data.repository.UserRepository
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorApplication
import com.example.ladycure.domain.model.Role
import com.example.ladycure.domain.model.Speciality
import com.example.ladycure.domain.model.User
import com.example.ladycure.presentation.admin.components.AddUserDialog
import com.example.ladycure.presentation.admin.components.AdminSearchBar
import com.example.ladycure.presentation.admin.components.DeleteConfirmationDialog
import com.example.ladycure.presentation.admin.components.DoctorList
import com.example.ladycure.presentation.admin.components.EditDoctorDialog
import com.example.ladycure.presentation.admin.components.EditUserDialog
import com.example.ladycure.presentation.admin.components.EmptyView
import com.example.ladycure.presentation.admin.components.LoadingView
import com.example.ladycure.presentation.admin.components.UserList
import com.example.ladycure.presentation.admin.components.buildUpdateMap
import com.example.ladycure.presentation.home.components.Screen.AdminAnalytics
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsSummaryCard(
    stats: Map<String, Int>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Analytics,
                    contentDescription = "Analytics",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Analytics Overview",
                    style = MaterialTheme.typography.titleLarge.copy(color = DefaultOnPrimary)
                )
            }
            listOf(
                Color(0xFFFFF0F5), // light pink
                Color(0xFFF0F8FF), // light blue
                Color(0xFFFAFAD2), // light yellow
                Color(0xFFE9FFEB), // light green
                Color(0xFFE2DCFA) // light purple
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for ((key, value) in stats) {
                    StatisticItem(
                        label = when (key) {
                            "totalUsers" -> "Total Users"
                            "activeDoctors" -> "Active Doctors"
                            "pendingApplications" -> "Pending Apps"
                            else -> key.replaceFirstChar { it.uppercase() }
                        },
                        color = when (key) {
                            "totalUsers" -> DefaultPrimary
                            "activeDoctors" -> BabyBlue
                            "pendingApplications" -> Yellow
                            else -> DefaultPrimary
                        },
                        value = value.toString()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "More detailed charts and metrics are available in the full analytics section.",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f)
            )
            TextButton(onClick = onClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View Full Analytics", color = DefaultOnPrimary)
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Full Analytics",
                        tint = DefaultPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticItem(label: String, color: Color, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.9f)
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(color = DefaultOnPrimary.copy(alpha = 0.7f))
        )
    }
}

@Composable

fun AdminDashboardHeader(
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Consistent with HomeScreen Header padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DefaultPrimary
        )

        IconButton(
            onClick = onLogoutClick,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = DefaultPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ApplicationItemCard(
    application: DoctorApplication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (application.status) {
        ApplicationStatus.PENDING -> Yellow
        ApplicationStatus.APPROVED -> Green
        ApplicationStatus.REJECTED -> Red
        ApplicationStatus.NEEDS_MORE_INFO -> BabyBlue
    }

    val gradientColors = listOf(
        statusColor.copy(alpha = 0.05f),
        statusColor.copy(alpha = 0.02f),
        Color.White
    )

    Card(
        modifier = modifier
            .width(300.dp)
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = 100f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header row with name and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${application.firstName} ${application.lastName}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = DefaultOnPrimary.copy(alpha = 0.9f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = application.status.displayName.replace("_", " ")
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(application.speciality.icon),
                        contentDescription = "Specialty",
                        tint = DefaultPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        application.speciality.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DefaultPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 4.dp
                        ), // the painter of speciality has some additional padding so we add so they can be alligned
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Experience
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = "Experience",
                            tint = DefaultOnPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${application.yearsOfExperience} yrs exp",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Submission date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Submission date",
                            tint = DefaultOnPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            application.submissionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.7f),
                            )
                        )
                    }
                }

                if (application.currentWorkplace.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            application.currentWorkplace,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DefaultOnPrimary.copy(alpha = 0.6f),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun ApplicationItemCardPreview() {
    val application = DoctorApplication(
        userId = "housemd-001",
        firstName = "Gregory",
        lastName = "House",
        email = "house@princetonplainsboro.com",
        dateOfBirth = LocalDate.of(1959, 6, 11),
        licenseNumber = "MD42069",
        licensePhotoUrl = "https://somecdn.com/house/license.jpg",
        diplomaPhotoUrl = "https://somecdn.com/house/diploma.jpg",
        speciality = Speciality.OTHER,
        yearsOfExperience = 25,
        currentWorkplace = "Princeton-Plainsboro Teaching Hospital",
        phoneNumber = "+1-555-420-6969",
        address = "221B Vicodin Lane", // I had to 😈
        city = "Princeton",
        status = ApplicationStatus.PENDING,
        submissionDate = LocalDate.now(),
        reviewNotes = "Walks with a cane, limps, but diagnoses what no one else can. Slight attitude issue. Genius level intellect. Proceed with caution."
    )


    ApplicationItemCard(
        application = application,
        onClick = {}
    )
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ApplicationDetailsDialog(
    application: DoctorApplication,
    onDismiss: () -> Unit,
    onStatusChange: (ApplicationStatus, String?, Boolean) -> Unit,
    onApprove: () -> Unit,
) {

    // Add these state variables at the top of the ApplicationDetailsDialog composable
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
                // Header
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

                // Key details in a clean layout
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

                // Documents section
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

                // Notes section
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

                // Action buttons
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
                            // Request More Info Button
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

                            // Reject Button
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
                            // Approve Button
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

                            // Reject Button
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

    // Status change confirmation dialog
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
                    } // Ensure comment is provided for REJECTED and NEEDS_MORE_INFO
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

// Add these dialogs at the bottom of the ApplicationDetailsDialog composable
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


// Add this new composable function anywhere in your file
@Composable
fun ImageViewDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DefaultPrimary
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        IconButton(onClick = {
                            downloadFile(
                                context = context,
                                url = imageUrl,
                                fileName = title + ".jpg",
                                title = title,
                                description = "Downloading $title"
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = DefaultPrimary
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DefaultPrimary
                            )
                        }
                    }
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .clip(RoundedCornerShape(12.dp)),
                )


            }
        }
    }
}

fun downloadFile(
    context: Context,
    url: String,
    fileName: String,
    title: String,
    description: String
) {
    val request = DownloadManager.Request(url.toUri())
        .apply {
            setTitle(title)
            setDescription(description)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}

@Composable
private fun DetailRow(label: String, value: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    snackbarController: SnackbarController
) {
    val applicationRepo = ApplicationRepository()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val adminRepo = remember { AdminRepository() }
    val authRepo = remember { AuthRepository() }

    val coroutineScope = rememberCoroutineScope()

    var isLoadingApplications by remember { mutableStateOf(true) }
    val doctorApplications = remember { mutableStateListOf<DoctorApplication>() }

    var showApplicationsDialog by remember { mutableStateOf(false) }
    var selectedApplication by remember { mutableStateOf<DoctorApplication?>(null) }

    var stats by remember {
        mutableStateOf(
            mapOf(
                "totalUsers" to 0,
                "activeDoctors" to 0,
                "pendingApplications" to 0
            )
        )
    }

    suspend fun refreshApplications(
        applicationRepo: ApplicationRepository = ApplicationRepository(),
        doctorApplications: MutableList<DoctorApplication>,
        snackbarController: SnackbarController,
        setLoading: (Boolean) -> Unit
    ) {
        setLoading(true)
        val result = applicationRepo.getAllApplications()
        if (result.isSuccess) {
            doctorApplications.clear()
            doctorApplications.addAll(result.getOrNull() as List<DoctorApplication>)
            setLoading(false)
        } else {
            snackbarController.showMessage("Failed to refresh applications: ${result.exceptionOrNull()?.message}")
            setLoading(false)
        }
    }

    LaunchedEffect(Unit) {
        var result = adminRepo.getAdminStats()
        if (result.isSuccess) {
            stats = (result.getOrNull() ?: mapOf(
                "totalUsers" to 0,
                "activeDoctors" to 0,
                "pendingApplications" to 0
            )) as Map<String, Int>
        } else {
            snackbarController.showMessage("Failed to load stats: ${result.exceptionOrNull()?.message}")
        }
    }

    LaunchedEffect(Unit) {
        var result = applicationRepo.getAllApplications()
        if (result.isSuccess) {
            doctorApplications.clear()
            doctorApplications.addAll(
                (result.getOrNull() ?: emptyList<DoctorApplication>())
            )
            isLoadingApplications = false
        } else {
            snackbarController.showMessage("Failed to load applications: ${result.exceptionOrNull()?.message}")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AdminDashboardHeader(
            onLogoutClick = { showLogoutDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnalyticsSummaryCard(
            stats = stats,
            onClick = { navController.navigate(AdminAnalytics.route) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pending Doctor Applications",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = {
                    coroutineScope.launch {
                        refreshApplications(
                            applicationRepo,
                            doctorApplications,
                            snackbarController,
                            { isLoadingApplications = it })
                    }
                }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh Applications",
                        tint = DefaultPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingApplications) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DefaultPrimary)
                }
            } else if (doctorApplications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 16.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No pending applications.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DefaultOnPrimary.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(doctorApplications.size) { index ->
                        ApplicationItemCard(
                            application = doctorApplications[index],
                            onClick = {
                                selectedApplication = doctorApplications[index]
                                showApplicationsDialog = true
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            authRepo.signOut()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                ) { Text("Log out", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        "Cancel",
                        color = DefaultPrimary
                    )
                }
            }
        )
    }

    if (showApplicationsDialog) {
        ApplicationDetailsDialog(
            application = selectedApplication!!,
            onDismiss = { showApplicationsDialog = false },
            onStatusChange = { newStatus, comment, justComment ->
                coroutineScope.launch {
                    val result = applicationRepo.updateApplicationStatus(
                        selectedApplication!!.userId,
                        newStatus.displayName,
                        comment ?: selectedApplication!!.reviewNotes ?: ""
                    )
                    if (result.isSuccess) {
                        if (justComment == true) {
                            snackbarController.showMessage("Comment updated successfully.")
                        } else {
                            snackbarController.showMessage("Application status changed to ${newStatus.displayName} successfully.")
                            showApplicationsDialog = false
                        }
                        refreshApplications(
                            applicationRepo,
                            doctorApplications,
                            snackbarController
                        ) {
                            isLoadingApplications = it
                        }

                    } else {
                        snackbarController.showMessage("Failed to change application status: ${result.exceptionOrNull()?.message}")
                    }
                }
            },
            onApprove = {
                coroutineScope.launch {
                    val result = applicationRepo.approveApplication(
                        selectedApplication!!.userId,
                    )
                    if (result.isSuccess) {
                        snackbarController.showMessage("Application approved successfully.")
                        showApplicationsDialog = false
                        refreshApplications(
                            applicationRepo,
                            doctorApplications,
                            snackbarController
                        ) {
                            isLoadingApplications = it
                        }
                    } else {
                        snackbarController.showMessage("Failed to approve application: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    snackbarController: SnackbarController
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var editedUser by remember { mutableStateOf<User?>(null) }
    var newUser by remember { mutableStateOf(User.empty().copy(role = Role.USER)) }

    val userRepo = remember { UserRepository() }
    val authRepo = remember { AuthRepository() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isLoadingUsers by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoadingUsers = true
        val result = userRepo.getUsers()
        if (result.isSuccess) {
            users = result.getOrNull() ?: emptyList()
        } else {
            snackbarController.showMessage("Failed to load users: ${result.exceptionOrNull()?.message}")
        }
        isLoadingUsers = false
    }

    var allUsers = remember(users) {
        users.filter { it["role"] != Role.DOCTOR.value }
            .map { User.fromMap(it) }
    }


    val filteredUsers = remember(allUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            allUsers
        } else {
            allUsers.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true)
            }
        }
    }

    fun refreshUserData() {
        coroutineScope.launch {
            isLoadingUsers = true
            val result = userRepo.getUsers()
            if (result.isSuccess) {
                users = result.getOrNull() ?: emptyList()
            } else {
                snackbarController.showMessage("Failed to refresh users: ${result.exceptionOrNull()?.message}")
            }
            isLoadingUsers = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "User Management",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            IconButton(
                onClick = { refreshUserData() },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh Users",
                    tint = DefaultPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        AdminSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingUsers) {
            LoadingView()
        } else if (filteredUsers.isEmpty()) {
            EmptyView(if (searchQuery.isBlank()) "No users found." else "No users match your search.")
        } else {
            UserList(
                users = filteredUsers,
                onEditClick = { user ->
                    selectedUser = user
                    editedUser = user.copy()
                    showEditUserDialog = true
                },
                onDeleteClick = { user ->
                    selectedUser = user
                    showDeleteUserDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            user = newUser,
            onDismiss = { showAddUserDialog = false },
            onSave = {
//                    coroutineScope.launch {
//                        val result = authRepo.createUserInAuthAndDb(newUser.copy(role = Role.USER), "Password123") // Force USER role
//                        if (result.isSuccess) {
//                            snackbarController.showMessage("User created successfully.")
//                            refreshUserData()
//                        } else {
//                            snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
//                        }
//                        showAddUserDialog = false
//                    }
            },
            onUserChange = { newUser = it } // Dialog should not change role here for users
        )
    }

    if (showEditUserDialog && editedUser != null) {
        EditUserDialog(
            user = editedUser!!,
            onDismiss = { showEditUserDialog = false },
            onSave = {
                coroutineScope.launch {
                    selectedUser?.let { originalUser ->
                        val updates = buildUpdateMap(editedUser!!)

                        val result = userRepo.updateUser(originalUser.id, updates)

                        if (result.isSuccess) {
                            snackbarController.showMessage(
                                if (editedUser!!.role == Role.DOCTOR)
                                    "User converted to doctor successfully"
                                else
                                    "User updated successfully"
                            )
                            showEditUserDialog = false
                            var refreshResult = userRepo.getUsers()
                            allUsers = refreshResult.getOrNull()
                                ?.mapNotNull { User.fromMap(it) }
                                ?.filter { it.role != Role.DOCTOR } ?: emptyList()
                        } else {
                            snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            },
            onUserChange = { editedUser = it })
    }

    if (showDeleteUserDialog && selectedUser != null) {
        DeleteConfirmationDialog(
            user = selectedUser!!,
            onDismiss = { showDeleteUserDialog = false },
            onConfirm = {
//                    coroutineScope.launch {
//                        val result = authRepo.deleteUserAccount(selectedUser!!.id)
//                        if (result.isSuccess) {
//                            snackbarController.showMessage("User deleted.")
//                            refreshUserData()
//                        } else {
//                            snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
//                        }
//                        showDeleteUserDialog = false
//                    }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorManagementScreen(
    snackbarController: SnackbarController
) {
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var showEditDoctorDialog by remember { mutableStateOf(false) }
    var showDeleteDoctorDialog by remember { mutableStateOf(false) }

    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var editedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var newDoctorAsUser by remember {
        mutableStateOf(
            User.empty().copy(role = Role.DOCTOR)
        )
    } // For AddUserDialog

    val userRepo = remember { UserRepository() }
    val authRepo = remember { AuthRepository() }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isLoadingDoctors by remember { mutableStateOf(false) }
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    fun refreshDoctorData() {
        coroutineScope.launch {
            isLoadingDoctors = true
            val result = userRepo.getUsers()
            if (result.isSuccess) {
                users = result.getOrNull() ?: emptyList()
            } else {
                snackbarController.showMessage("Failed to refresh doctors: ${result.exceptionOrNull()?.message}")
            }
            isLoadingDoctors = false
        }
    }

    val allDoctors = remember(users) {
        users.filter { it["role"] == Role.DOCTOR.value }
            .map { Doctor.fromMap(it) }
    }

    LaunchedEffect(Unit) {
        refreshDoctorData()
    }

    val filteredDoctors = remember(allDoctors, searchQuery) {
        if (searchQuery.isBlank()) allDoctors else {
            allDoctors.filter {
                it.name.contains(searchQuery, true) ||
                        it.surname.contains(searchQuery, true) ||
                        it.email.contains(searchQuery, true) ||
                        it.speciality.displayName.contains(searchQuery, true) ||
                        it.address.contains(searchQuery, true) ||
                        it.city.contains(searchQuery, true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "Doctor Management",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary
            )
            IconButton(
                onClick = { refreshDoctorData() },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Refresh Users",
                    tint = DefaultPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        AdminSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoadingDoctors) {
            LoadingView()
        } else if (filteredDoctors.isEmpty()) {
            EmptyView(if (searchQuery.isBlank()) "No doctors found." else "No doctors match your search.")
        } else {
            DoctorList(
                doctors = filteredDoctors,
                onEditClick = { doctor ->
                    selectedDoctor = doctor
                    editedDoctor = doctor.copyDoc()
                    showEditDoctorDialog = true
                },
                onDeleteClick = { doctor ->
                    selectedDoctor = doctor
                    showDeleteDoctorDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }


    if (showAddDoctorDialog) {
        AddUserDialog(
            user = newDoctorAsUser,
            onDismiss = { showAddDoctorDialog = false },
            onSave = {
//                    coroutineScope.launch {
//                        // createUserInAuthAndDb should handle the DOCTOR role and add doctor-specific fields.
//                        // The AddUserDialog needs to collect doctor-specific info if role is DOCTOR.
//                        val result = authRepo.createUserInAuthAndDb(newDoctorAsUser, "Password123")
//                        if (result.isSuccess) {
//                            snackbarController.showMessage("Doctor created successfully.")
//                            refreshDoctorData()
//                        } else {
//                            snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
//                        }
//                        showAddDoctorDialog = false
//                    }
            },
            onUserChange = {
                newDoctorAsUser = it
            }
        )
    }

    if (showEditDoctorDialog && editedDoctor != null) {
        EditDoctorDialog(
            doctor = editedDoctor!!,
            onDismiss = { showEditDoctorDialog = false },
            onSave = {
                coroutineScope.launch {
                    if (editedDoctor!!.role == Role.DOCTOR) {
                        val updates = buildUpdateMap(editedDoctor!!)
                        val result = userRepo.updateUser(editedDoctor!!.id, updates)
                        snackbarController.showMessage(if (result.isSuccess) "Doctor updated." else "Error: ${result.exceptionOrNull()?.message}")
                    } else {
                        val userToUpdate = editedDoctor!!.toUser()
                        val updates = buildUpdateMap(userToUpdate)
                        val result = userRepo.docToUserUpdate(editedDoctor!!.id, updates)
                        snackbarController.showMessage(if (result.isSuccess) "Doctor demoted to User." else "Error: ${result.exceptionOrNull()?.message}")
                    }
                    refreshDoctorData()
                    showEditDoctorDialog = false
                }
            },
            onDoctorChange = { editedDoctor = it }
        )
    }

    if (showDeleteDoctorDialog && selectedDoctor != null) {
        DeleteConfirmationDialog(
            user = selectedDoctor!!,
            onDismiss = { showDeleteDoctorDialog = false },
            onConfirm = {
//                    coroutineScope.launch {
//                        val result = authRepo.deleteUserAccount(selectedDoctor!!.id)
//                        if (result.isSuccess) {
//                            snackbarController.showMessage("Doctor deleted.")
//                            refreshDoctorData()
//                        } else {
//                            snackbarController.showMessage("Error: ${result.exceptionOrNull()?.message}")
//                        }
//                        showDeleteDoctorDialog = false
//                    }
            }
        )
    }
}
