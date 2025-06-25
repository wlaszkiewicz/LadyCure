package com.example.ladycure.presentation.applications

import BabyBlue
import DefaultBackground
import DefaultPrimary
import Green
import Red
import YellowOrange
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.DoctorApplication
import com.example.ladycure.utility.SnackbarController
import java.time.format.DateTimeFormatter


@Composable
fun DoctorPendingMainScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    viewModel: DoctorPendingViewModel = viewModel()
) {
    val applicationData by viewModel.applicationData.collectAsState()
    val error by viewModel.error.collectAsState()
    val showFullApplicationDialog by viewModel.showFullApplicationDialog.collectAsState()
    val showLogoutConfirmation by viewModel.showLogoutConfirmation.collectAsState()

    LaunchedEffect(error) {
        error.let {
            snackbarController.showMessage(it.toString())
            viewModel.clearError()
        }
    }

    if (applicationData == null) {
        // Show loading or error state
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = DefaultPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Loading application data...", fontSize = 18.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DefaultBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), // Consistent with HomeScreen Header padding
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Doctor Application Status",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = DefaultPrimary
                )

                IconButton(
                    onClick = { viewModel.showLogoutConfirmation() },
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

            Spacer(modifier = Modifier.height(16.dp))

            StatusCard(
                data = applicationData!!,
                status = applicationData?.status ?: ApplicationStatus.PENDING,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            ApplicationPreviewCard(
                application = applicationData!!,
                onViewDetails = { viewModel.showDialog() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ContactSupportCard(modifier = Modifier.fillMaxWidth())
        }
    }

    if (showFullApplicationDialog && applicationData != null) {
        FullApplicationDialog(
            application = applicationData!!,
            onDismiss = { viewModel.dismissDialog() },
            viewModel = viewModel
        )
    }

    if (showLogoutConfirmation) {
        // Show logout confirmation dialog
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutConfirmation() },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissLogoutConfirmation()
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutConfirmation() }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
private fun StatusCard(
    data: DoctorApplication,
    status: ApplicationStatus,
    modifier: Modifier = Modifier
) {
    data class StatusInfo(
        val icon: ImageVector,
        val title: String,
        val description: String,
        val color: Color
    )

    val (icon, title, description, color) = when (status) {
        ApplicationStatus.PENDING -> StatusInfo(
            Icons.Default.Pending,
            "Application Pending",
            "Hi! Your application is under review. We're working on it and will notify you once it's processed.",
            YellowOrange
        )

        ApplicationStatus.APPROVED -> StatusInfo(
            Icons.Default.Info,
            "Application Approved",
            "Congratulations! Your application has been approved. You can now access all doctor features.",
            Green
        )

        ApplicationStatus.REJECTED -> StatusInfo(
            Icons.Default.Info,
            "Application Rejected",
            "We're sorry, but your application couldn't be approved at this time. Please check the reason below.",
            Red
        )

        ApplicationStatus.NEEDS_MORE_INFO -> StatusInfo(
            Icons.Default.Info,
            "Revision Required",
            "Your application needs some additional information. Please review the comments and resubmit.",
            BabyBlue
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Status",
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }

            if (status == ApplicationStatus.REJECTED || status == ApplicationStatus.NEEDS_MORE_INFO) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "Feedback: ${data.reviewNotes ?: "No additional feedback provided"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationPreviewCard(
    application: DoctorApplication,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Application",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DefaultPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DefaultPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = application.submissionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.labelSmall,
                        color = DefaultPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Doctor info section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Doctor",
                        tint = DefaultPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "${application.firstName} ${application.lastName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = application.speciality.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row with 2 items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // License Number
                    GridItem(
                        icon = Icons.Default.Badge,
                        title = "License Number",
                        value = application.licenseNumber,
                        modifier = Modifier.weight(1f)
                    )

                    // Experience
                    GridItem(
                        icon = Icons.Default.WorkHistory,
                        title = "Experience",
                        value = "${application.yearsOfExperience} years",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second row with 2 items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Workplace
                    GridItem(
                        icon = Icons.Default.LocalHospital,
                        title = "Workplace",
                        value = application.currentWorkplace,
                        modifier = Modifier.weight(1f)
                    )

                    GridItem(
                        icon = Icons.Default.Phone,
                        title = "Phone Number",
                        value = application.phoneNumber,
                        modifier = Modifier.weight(1f)
                    )

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // More subtle button
            OutlinedButton(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = DefaultPrimary
                ),
                border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "View Full Application",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun GridItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DefaultPrimary.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = DefaultPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 26.dp) // Align with icon
            )
        }
    }
}

@Composable
private fun ContactSupportCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Need Help?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary
            )

            Text(
                text = "If you have any questions about your application status or need assistance, please contact our support team.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            OutlinedButton(
                onClick = { /* Open email client or support form */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DefaultPrimary
                ),
                border = BorderStroke(1.dp, DefaultPrimary)
            ) {
                Text(
                    text = "Contact Support",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun FullApplicationDialog(
    application: DoctorApplication,
    onDismiss: () -> Unit,
    viewModel: DoctorPendingViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header with close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DefaultPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = 100f
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Application Details",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = DefaultPrimary
                            )
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DefaultPrimary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = DefaultPrimary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp)
                        .weight(1f)
                ) {
                    // Personal Information Section
                    SectionHeader(
                        title = "Personal Information",
                        icon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplicationDetailRow(
                        icon = Icons.Default.Badge,
                        title = "Full Name",
                        value = "${application.firstName} ${application.lastName}"
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value = application.email
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.Cake,
                        title = "Date of Birth",
                        value = application.dateOfBirth.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.Phone,
                        title = "Phone Number",
                        value = application.phoneNumber
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.LocationOn,
                        title = "Address",
                        value = "${application.address}, ${application.city}"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Professional Information Section
                    SectionHeader(
                        title = "Professional Information",
                        icon = Icons.Default.Work
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApplicationDetailRow(
                        icon = Icons.Default.MedicalServices,
                        title = "Speciality",
                        value = application.speciality.displayName
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.Badge,
                        title = "License Number",
                        value = application.licenseNumber
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.History,
                        title = "Years of Experience",
                        value = "${application.yearsOfExperience} years"
                    )
                    ApplicationDetailRow(
                        icon = Icons.Default.Business,
                        title = "Current Workplace",
                        value = application.currentWorkplace
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Documents Section
                    SectionHeader(
                        title = "Documents",
                        icon = Icons.Default.Description
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // License Photo
                    DocumentPreviewItem(
                        title = "Medical License",
                        imageUrl = application.licensePhotoUrl,
                        context = context,
                        modifier = Modifier.padding(bottom = 16.dp),
                        viewModel = viewModel
                    )

                    // Diploma
                    DocumentPreviewItem(
                        title = "Medical Diploma",
                        imageUrl = application.diplomaPhotoUrl,
                        context = context,
                        modifier = Modifier.padding(bottom = 16.dp),
                        viewModel = viewModel
                    )
                }

                // Footer with submission date
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DefaultPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, DefaultPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Submitted on: ${
                            application.submissionDate.format(
                                DateTimeFormatter.ofPattern(
                                    "MMM dd, yyyy"
                                )
                            )
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        color = DefaultPrimary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = DefaultPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DefaultPrimary
            )
        )
    }
}

@Composable
private fun DocumentPreviewItem(
    title: String,
    imageUrl: String,
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: DoctorPendingViewModel = viewModel()
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$title:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray
                )
            )

            TextButton(
                onClick = {
                    viewModel.downloadFile(
                        context = context,
                        url = imageUrl,
                        fileName = "${title.lowercase().replace(" ", "_")}.jpg",
                        title = title,
                        description = "Downloading $title"
                    )
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "Download",
                    style = MaterialTheme.typography.labelSmall,
                    color = DefaultPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download $title",
                    tint = DefaultPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp)),
            error = rememberVectorPainter(Icons.Default.ImageNotSupported),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ApplicationDetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = DefaultPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}