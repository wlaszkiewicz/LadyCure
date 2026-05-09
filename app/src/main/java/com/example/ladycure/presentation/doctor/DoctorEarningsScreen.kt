package com.example.ladycure.presentation.doctor

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ladycure.presentation.admin.SummaryCard
import com.example.ladycure.presentation.admin.TimePeriod
import com.example.ladycure.ui.theme.BabyBlue
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.Purple
import com.example.ladycure.ui.theme.Yellow
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import com.example.ladycure.utility.SnackbarController



val appointmentTypeColors = listOf(
    Purple,
    BabyBlue,
    DefaultPrimary,
    Yellow.copy(alpha = 0.7f),
    Purple.copy(alpha = 0.5f),
    DefaultPrimary.copy(alpha = 0.5f),
    BabyBlue.copy(alpha = 0.5f),
)

fun getColorForAppointmentType(index: Int): Color {
    return appointmentTypeColors[index % appointmentTypeColors.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorEarningsScreen(
    navController: NavController,
    snackbarController: SnackbarController,
    viewModel: DoctorEarningsViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()

    LaunchedEffect(viewModel.error) {
        viewModel.error?.let {
            snackbarController.showMessage(it)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.w(0.039f), vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DefaultPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Earnings Overview",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefaultPrimary,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimens.h(0.017f)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimePeriod.entries.forEach { period ->
                OutlinedButton(
                    onClick = { viewModel.selectTimePeriod(period) },
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
                        .padding(bottom = dimens.h(0.017f)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        title = "Earnings this month",
                        value = "$${viewModel.thisMonthEarnings.toInt()}",
                        color = DefaultPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Total\nEarnings",
                        value = "$${viewModel.totalEarnings.toInt()}",
                        color = BabyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Total Appointments",
                        value = viewModel.totalAppointments.toString(),
                        color = Yellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(0.029f),
                            vertical = dimens.h(0.013f)
                        )
                    ) {
                        Text(
                            "Earnings Over Time",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = DefaultPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (viewModel.earningsData.isNotEmpty()) {
                            BarChart(
                                data = viewModel.earningsData.map { it.first to it.second.toInt() },
                                isCurrency = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimens.h(0.197f))
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No earnings data available",
                                modifier = Modifier.padding(vertical = dimens.h(0.017f))
                            )
                        }
                    }
                }


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = dimens.h(0.219f),
                            max = dimens.h(0.438f)
                        ), // Adjust as needed
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(0.029f),
                            vertical = dimens.h(0.013f)
                        )
                    ) {
                        Text(
                            "Earnings by Appointment Type",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Purple,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (viewModel.earningsByType.isNotEmpty()) {
                            PieChart(
                                data = viewModel.earningsByType,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        } else {
                            Text(
                                "No earnings by type data available",
                                modifier = Modifier.padding(vertical = dimens.h(0.017f))
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = dimens.w(0.029f),
                            vertical = dimens.h(0.013f)
                        )
                    ) {
                        Text(
                            "Earnings by Appointment Type",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = BabyBlue,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (viewModel.earningsByType.isNotEmpty()) {
                            val earningsByTypeList =
                                viewModel.earningsByType.map { it.key to it.value.toInt() }
                            val totalEarningsByType = viewModel.earningsByType.values.sum()

                            BarChart(
                                data = earningsByTypeList,
                                isCurrency = true,
                                useTypeColors = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimens.h(0.197f))
                                    .padding(top = 8.dp)
                            )

                            AppointmentTypeLegend(
                                data = earningsByTypeList,
                                showPercentage = false,
                                total = totalEarningsByType.toDouble(),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                "No earnings by type data available",
                                modifier = Modifier.padding(vertical = dimens.h(0.017f))
                            )
                        }
                    }
                }

                SummaryCard(
                    title = "Most Popular Appointment Type",
                    value = viewModel.mostPopularType?.let { "${it.first} (${it.second})" } ?: "N/A",
                    color = Purple,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimens.h(0.017f)))
            }
        }
    }
}


@Composable
fun AppointmentTypeLegend(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = false,
    total: Double? = null
) {
    val sortedData = data.sortedByDescending { it.second }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        sortedData.forEachIndexed { index, (label, value) ->
            val color = getColorForAppointmentType(index)
            val percentageText = if (showPercentage && total != null) {
                val percentage = (value.toDouble() / total * 100).toInt()
                " ($percentage%)"
            } else ""

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = MaterialTheme.shapes.small)
                )
                Text(
                    text = "$label$percentageText",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PieChart(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val dimens = rememberResponsiveDimens()
    val total = data.values.sum()
    if (total <= 0) {
        return Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No earnings data available")
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.h(0.175f))
        ) {
            var startAngle = -90f
            val radius = size.minDimension / 2f - 20.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)

            data.entries.forEachIndexed { index, (_, value) ->
                val sweepAngle = ((value / total) * 360f).toFloat().coerceAtMost(360f)
                val color = getColorForAppointmentType(index)

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }

        val dataList = data.map { it.key to it.value.toInt() }
        AppointmentTypeLegend(
            data = dataList,
            showPercentage = true,
            total = total,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    isCurrency: Boolean = false,
    modifier: Modifier = Modifier,
    useTypeColors: Boolean = false // Add this parameter
) {
    val dimens = rememberResponsiveDimens()
    val maxValue = data.maxOfOrNull { it.second }?.toFloat() ?: 1f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(bottom = dimens.h(0.017f))
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

                    val barColor = if (useTypeColors) {
                        getColorForAppointmentType(index)
                    } else {
                        DefaultPrimary.copy(alpha = 0.7f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidthPx, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        val displayValue = if (isCurrency) "$$value" else value.toString()
                        drawText(
                            displayValue,
                            left + barWidthPx / 2,
                            top - 8.dp.toPx(),
                            Paint().apply {
                                this.color = barColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }

                    if (!useTypeColors) {

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
    }
}