package com.example.ladycure.presentation.admin

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Red
import Yellow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.domain.model.ApplicationStatus
import com.example.ladycure.domain.model.DoctorApplication
import com.example.ladycure.presentation.admin.components.ApplicationDetailsDialog
import com.example.ladycure.presentation.home.components.Screen.AdminAnalytics
import com.example.ladycure.utility.SnackbarController
import java.time.format.DateTimeFormatter

/**
 * Admin dashboard screen displaying key statistics, pending doctor applications,
 * and logout functionality.
 *
 * @param navController Used to navigate between screens.
 * @param snackbarController Controller to show snackbar messages.
 * @param viewModel ViewModel providing data and state management for the dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    var errorMessage = viewModel.errorMessage
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarController.showMessage(it)
            viewModel.errorMessage = null
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AdminDashboardHeader(
            onLogoutClick = { viewModel.showLogoutDialog() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnalyticsSummaryCard(
            stats = viewModel.stats,
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
                IconButton(onClick = { viewModel.refreshApplications() }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh Applications",
                        tint = DefaultPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoadingApplications) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DefaultPrimary)
                }
            } else if (viewModel.doctorApplications.isEmpty()) {
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
                    items(viewModel.doctorApplications.size) { index ->
                        ApplicationItemCard(
                            application = viewModel.doctorApplications[index],
                            onClick = { viewModel.showApplicationDetails(viewModel.doctorApplications[index]) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (viewModel.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(navController) },
                    colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary)
                ) { Text("Log out", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                    Text("Cancel", color = DefaultPrimary)
                }
            }
        )
    }

    if (viewModel.showApplicationsDialog && viewModel.selectedApplication != null) {
        ApplicationDetailsDialog(
            application = viewModel.selectedApplication!!,
            onDismiss = { viewModel.dismissApplicationDetails() },
            onStatusChange = { newStatus, comment, justComment ->
                viewModel.updateApplicationStatus(newStatus, comment, justComment)
            },
            onApprove = { viewModel.approveApplication() }
        )
    }
}

/**
 * Displays a summary card of analytics statistics with a clickable action
 * to navigate to a detailed analytics screen.
 *
 * @param stats Map of statistic keys and their integer values to be displayed.
 * @param onClick Callback invoked when the user taps on the card.
 */
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

/**
 * Displays a statistic item with a colored value and a label below it.
 *
 * @param label Descriptive label for the statistic.
 * @param color Color to display the statistic value.
 * @param value Statistic value displayed prominently.
 */
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

/**
 * Header row for the admin dashboard, showing the screen title and a logout button.
 *
 * @param onLogoutClick Callback invoked when the logout button is clicked.
 */
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

/**
 * Card component representing a single doctor application item in the pending applications list.
 *
 * Shows the applicant's name, status, specialty, experience, submission date, and current workplace.
 * Card background color is styled based on the application status.
 *
 * @param application The [DoctorApplication] data model representing the application.
 * @param onClick Callback invoked when the card is clicked.
 * @param modifier Optional [Modifier] for styling.
 */
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




