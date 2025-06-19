package com.example.ladycure.presentation.admin

import BabyBlue
import DefaultOnPrimary
import DefaultPrimary
import Green
import Purple
import Red
import Yellow
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ladycure.data.repository.AdminRepository
import com.example.ladycure.utility.SnackbarController
import kotlinx.coroutines.async

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    navController: NavController,
    snackbarController: SnackbarController
) {
    val adminRepo = remember { AdminRepository() }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var userGrowthData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var patientGrowthData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var doctorGrowthData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var usersAgeData by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var applicationStats by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var totalStats by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    // Time period selection
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.MONTHLY) }

    // Fetch data on first load or when time period changes
    LaunchedEffect(selectedTimePeriod) {
        isLoading = true
        try {
            // Fetch all data in parallel
            val userGrowthDeferred = coroutineScope.async {
                adminRepo.getUserGrowthData(selectedTimePeriod)
            }
            val patientGrowthDeferred = coroutineScope.async {
                adminRepo.getPatientGrowthData(selectedTimePeriod)
            }
            val doctorGrowthDeferred = coroutineScope.async {
                adminRepo.getDoctorGrowthData(selectedTimePeriod)
            }
            val applicationStatsDeferred = coroutineScope.async {
                adminRepo.getApplicationStats()
            }
            val totalStatsDeferred = coroutineScope.async {
                adminRepo.getAdminStats()
            }
            val usersAgeDeferred = coroutineScope.async {
                adminRepo.getUsersAgeData()
            }

            // Wait for all requests to complete
            val userGrowthResult = userGrowthDeferred.await()
            userGrowthData = when {
                userGrowthResult.isSuccess -> userGrowthResult.getOrNull() ?: emptyList()
                else -> {
                    snackbarController.showMessage(
                        userGrowthResult.exceptionOrNull()?.message
                            ?: "Failed to load user growth data"
                    )
                    emptyList()
                }
            }
            val patientGrowthResult = patientGrowthDeferred.await()
            patientGrowthData = when {
                patientGrowthResult.isSuccess -> patientGrowthResult.getOrNull() ?: emptyList()
                else -> {
                    snackbarController.showMessage(
                        patientGrowthResult.exceptionOrNull()?.message
                            ?: "Failed to load patient growth data"
                    )
                    emptyList()
                }
            }
            val doctorGrowthResult = doctorGrowthDeferred.await()
            doctorGrowthData = when {
                doctorGrowthResult.isSuccess -> doctorGrowthResult.getOrNull() ?: emptyList()
                else -> {
                    snackbarController.showMessage(
                        doctorGrowthResult.exceptionOrNull()?.message
                            ?: "Failed to load doctor growth data"
                    )
                    emptyList()
                }
            }
            val applicationStatsResult = applicationStatsDeferred.await()
            applicationStats = when {
                applicationStatsResult.isSuccess -> applicationStatsResult.getOrNull() ?: emptyMap()
                else -> {
                    snackbarController.showMessage(
                        applicationStatsResult.exceptionOrNull()?.message
                            ?: "Failed to load application stats"
                    )
                    emptyMap()
                }
            }
            val totalStatsResult = totalStatsDeferred.await()
            totalStats = when {
                totalStatsResult.isSuccess -> totalStatsResult.getOrNull() ?: emptyMap()
                else -> {
                    snackbarController.showMessage(
                        totalStatsResult.exceptionOrNull()?.message ?: "Failed to load total stats"
                    )
                    emptyMap()
                }
            }
            val usersAgeResult = usersAgeDeferred.await()
            usersAgeData = when {
                usersAgeResult.isSuccess -> usersAgeResult.getOrNull() ?: emptyList()
                else -> {
                    snackbarController.showMessage(
                        usersAgeResult.exceptionOrNull()?.message ?: "Failed to load users age data"
                    )
                    emptyList()
                }
            }

        } catch (e: Exception) {
            snackbarController.showMessage("Failed to load analytics: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Admin Analytics",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DefaultPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        // Time period selector - make it scrollable if needed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimePeriod.entries.forEach { period ->
                OutlinedButton(
                    onClick = { selectedTimePeriod = period },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedTimePeriod == period)
                            DefaultPrimary.copy(alpha = 0.2f)
                        else
                            Color.Transparent,
                        contentColor = DefaultPrimary
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedTimePeriod == period) DefaultPrimary else DefaultPrimary.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Text(period.displayName, fontSize = 12.sp)
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DefaultPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        title = "Total Users",
                        value = totalStats["totalUsers"]?.toString() ?: "0",
                        color = DefaultPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Active Doctors",
                        value = totalStats["activeDoctors"]?.toString() ?: "0",
                        color = BabyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Pending Apps",
                        value = totalStats["pendingApplications"]?.toString() ?: "0",
                        color = Yellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Growth Charts - better spacing and sizing

                // User Growth Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "All User Growth",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = DefaultPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (userGrowthData.isNotEmpty()) {
                            BarChart(
                                data = userGrowthData,
                                color = DefaultPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No user growth data available",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                //Patient Growth Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Patient Growth",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Purple,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (patientGrowthData.isNotEmpty()) {
                            BarChart(
                                data = patientGrowthData,
                                color = Purple,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No patient growth data available",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Doctor Growth Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Doctor Growth",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = BabyBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (doctorGrowthData.isNotEmpty()) {
                            BarChart(
                                data = doctorGrowthData,
                                color = BabyBlue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No doctor growth data available",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
                // Application Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Application Status",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Green,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (applicationStats.isNotEmpty()) {
                            PieChart(
                                data = applicationStats.mapKeys {
                                    it.key.replaceFirstChar { char ->
                                        if (char.isLowerCase()) char.titlecase() else char.toString()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Text(
                                "No application data available",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Age Distribution Chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "User Age Distribution",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Purple,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (usersAgeData.isNotEmpty()) {
                            BarChart(
                                data = usersAgeData,
                                color = Purple,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No age data available",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second }?.toFloat() ?: 1f

    Row(
        modifier = modifier
            .padding(bottom = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current

        val density = LocalDensity.current
        val barSpacing = with(density) { 8.dp.toPx() }
        val canvasHeight = with(density) { 200.dp.toPx() }
        val minBarWidth = with(density) { 32.dp.toPx() }
        val totalSpacing = barSpacing * (data.size - 1)
        val horizontalPaddingPx = with(density) { 30.dp.toPx() } * 2
        val screenWidthPx = context.resources.displayMetrics.widthPixels
        val availableWidth = (screenWidthPx - horizontalPaddingPx).toFloat()
        val barWidthPx = if (data.isNotEmpty()) {
            ((availableWidth - totalSpacing) / data.size).coerceAtLeast(minBarWidth)
        } else {
            minBarWidth
        }
        val canvasWidth = barWidthPx * data.size + totalSpacing + with(density) { 8.dp.toPx() }

        Canvas(
            modifier = Modifier
                .width(with(LocalDensity.current) { canvasWidth.toDp() })
                .height(with(LocalDensity.current) { canvasHeight.toDp() })
        ) {
            val maxBarHeight = size.height * 0.8f

            data.forEachIndexed { index, (label, value) ->
                val barHeight = (value.toFloat() / maxValue) * maxBarHeight
                val left = index * (barWidthPx + barSpacing) + 4.dp.toPx()
                val top = size.height - barHeight

                // Draw bar
                drawRoundRect(
                    color = color.copy(alpha = 0.7f),
                    topLeft = Offset(left, top),
                    size = Size(barWidthPx, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        value.toString(),
                        left + barWidthPx / 2,
                        top - 8.dp.toPx(),
                        Paint().apply {
                            this.color = color.toArgb()
                            textSize = 12.sp.toPx()
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }

                // Draw time label below the bar, centered
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        label,
                        left + barWidthPx / 2,
                        size.height + 20.dp.toPx(),
                        Paint().apply {
                            this.color = DefaultOnPrimary.toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            var startAngle = -90f
            val radius = size.minDimension / 2f - 20.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)

            data.entries.forEach { (label, value) ->
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = when (label) {
                        "Approved" -> Green.copy(alpha = 0.7f)
                        "Pending" -> Yellow.copy(alpha = 0.7f)
                        "Rejected" -> Red.copy(alpha = 0.7f)
                        else -> DefaultPrimary.copy(alpha = 0.7f)
                    },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            data.keys.forEach { label ->
                val color = when (label) {
                    "Approved" -> Green.copy(alpha = 0.7f)
                    "Pending" -> Yellow.copy(alpha = 0.7f)
                    "Rejected" -> Red.copy(alpha = 0.7f)
                    else -> DefaultPrimary
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                            .background(color, shape = MaterialTheme.shapes.small)
                    )
                    Text(
                        "$label (${(data[label]!! / total * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DefaultOnPrimary
                        )
                    )
                }
            }
        }
    }
}

enum class TimePeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");
}