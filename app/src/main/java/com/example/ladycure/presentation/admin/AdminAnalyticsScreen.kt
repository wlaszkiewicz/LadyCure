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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ladycure.utility.SnackbarController

/**
 * Admin analytics screen displaying key metrics and charts.
 *
 * Shows user, doctor, and application statistics along with growth charts and
 * a pie chart of application status. Supports selecting different time periods.
 *
 * @param navController Navigation controller for screen navigation.
 * @param snackbarController Controller to show snackbar messages.
 * @param viewModel ViewModel providing analytics data and state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    viewModel: AdminAnalyticsViewModel = viewModel()
) {
    var errorMessage = viewModel.errorMessage

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarController.showMessage(it)
            viewModel.errorMessage = null
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimePeriod.entries.forEach { period ->
                OutlinedButton(
                    onClick = { viewModel.updateTimePeriod(period) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (viewModel.selectedTimePeriod == period)
                            DefaultPrimary.copy(alpha = 0.2f)
                        else
                            Color.Transparent,
                        contentColor = DefaultPrimary
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (viewModel.selectedTimePeriod == period) DefaultPrimary else DefaultPrimary.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Text(period.displayName, fontSize = 12.sp)
                }
            }
        }

        if (viewModel.isLoading) {
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
                        value = viewModel.totalStats["totalUsers"]?.toString() ?: "0",
                        color = DefaultPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Active Doctors",
                        value = viewModel.totalStats["activeDoctors"]?.toString() ?: "0",
                        color = BabyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Pending Apps",
                        value = viewModel.totalStats["pendingApplications"]?.toString() ?: "0",
                        color = Yellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                AnalyticsChartCard(
                    title = "All User Growth",
                    color = DefaultPrimary,
                    data = viewModel.userGrowthData
                )

                AnalyticsChartCard(
                    title = "Patient Growth",
                    color = Purple,
                    data = viewModel.patientGrowthData
                )

                AnalyticsChartCard(
                    title = "Doctor Growth",
                    color = BabyBlue,
                    data = viewModel.doctorGrowthData
                )

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
                        if (viewModel.applicationStats.isNotEmpty()) {
                            PieChart(
                                data = viewModel.applicationStats.mapKeys {
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

                AnalyticsChartCard(
                    title = "User Age Distribution",
                    color = Purple,
                    data = viewModel.usersAgeData
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Card composable that displays a chart with a title.
 *
 * Shows a bar chart if data is available, otherwise displays a "No data" message.
 *
 * @param title Title to display above the chart.
 * @param color Color used for the chart bars and title.
 * @param data List of pairs representing labels and values for the chart.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
private fun AnalyticsChartCard(
    title: String,
    color: Color,
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (data.isNotEmpty()) {
                BarChart(
                    data = data,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp)
                )
            } else {
                Text(
                    "No data available",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

/**
 * A summary card showing a numeric value and title.
 *
 * Typically used to display key stats like total users, active doctors, etc.
 *
 * @param title Title describing the stat.
 * @param value String value to display prominently.
 * @param color Color used for the value text.
 * @param modifier Modifier to be applied to the card.
 */
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

/**
 * A horizontally scrollable bar chart displaying data values with labels.
 *
 * @param data List of label-value pairs to plot.
 * @param color Color of the bars.
 * @param modifier Modifier to be applied to the canvas.
 */
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

/**
 * A pie chart representing categorical data distribution.
 *
 * Displays slices for each category colored by status, along with a legend.
 *
 * @param data Map of category labels to their corresponding integer values.
 * @param modifier Modifier to be applied to the pie chart layout.
 */
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

/**
 * Enum representing selectable time periods for analytics.
 *
 * @property displayName User-friendly name of the time period.
 */
enum class TimePeriod(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");
}