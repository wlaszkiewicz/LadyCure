package com.example.ladycure.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.LocalDate


data class DailyPeriodData(
    val date: LocalDate,
    var isPeriodDay: Boolean = false,
    var notes: String = "",
    var moodEmoji: String? = null,
    var flowIntensity: String? = null,
    var symptoms: List<String> = emptyList()
)

data class PeriodTrackerSettings(
    val averagePeriodLength: Int = 5,
    val averageCycleLength: Int = 28,
    val lastPeriodStartDate: LocalDate? = null
)

fun getPredictedPeriodStartDates(
    lastPeriodStartDate: LocalDate?,
    averageCycleLength: Int
): Set<LocalDate> {
    val predictedStarts = mutableSetOf<LocalDate>()
    if (lastPeriodStartDate == null) return predictedStarts

    predictedStarts.add(lastPeriodStartDate)
    var currentPrediction = lastPeriodStartDate

    repeat(12) {
        currentPrediction = currentPrediction?.plusDays(averageCycleLength.toLong())
        currentPrediction?.let { predictedStarts.add(it) }
    }

    currentPrediction = lastPeriodStartDate
    repeat(3) {
        currentPrediction = currentPrediction?.minusDays(averageCycleLength.toLong())
        currentPrediction?.let { predictedStarts.add(it) }
    }

    return predictedStarts
}

fun getPredictedOvulationDates(
    lastPeriodStartDate: LocalDate?,
    averageCycleLength: Int
): Set<LocalDate> {
    val predictedOvulations = mutableSetOf<LocalDate>()
    if (lastPeriodStartDate == null) return predictedOvulations

    getPredictedPeriodStartDates(lastPeriodStartDate, averageCycleLength).forEach { periodStart ->
        predictedOvulations.add(periodStart.minusDays(14))
    }
    return predictedOvulations
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(
    navController: NavHostController,
    viewModel: PeriodTrackerViewModel = hiltViewModel()
) {
    val dimens = rememberResponsiveDimens()
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDailyDetailDialog by remember { mutableStateOf(false) }
    var selectedDateForDetail by remember { mutableStateOf<LocalDate?>(null) }
    var showDailySummarySheet by remember { mutableStateOf(false) }
    var selectedDateForSummary by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(currentMonth) {
        viewModel.loadMonthData(currentMonth)
    }


    val predictedPeriodStarts by remember(
        viewModel.periodSettings.lastPeriodStartDate,
        viewModel.periodSettings.averageCycleLength
    ) {
        mutableStateOf(
            getPredictedPeriodStartDates(
                viewModel.periodSettings.lastPeriodStartDate,
                viewModel.periodSettings.averageCycleLength
            )
        )
    }

    val predictedOvulationDays by remember(
        viewModel.periodSettings.lastPeriodStartDate,
        viewModel.periodSettings.averageCycleLength
    ) {
        mutableStateOf(
            getPredictedOvulationDates(
                viewModel.periodSettings.lastPeriodStartDate,
                viewModel.periodSettings.averageCycleLength
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Period Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            Icons.Default.Settings,
                            "Settings",
                            tint = DefaultPrimary,
                            modifier = Modifier.size(dimens.w(28 / 411f))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DefaultBackground
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DefaultBackground)
                    .padding(paddingValues)
                    .padding(horizontal = dimens.w(16 / 411f))
            ) {
                MonthNavigationHeader(currentMonth, onMonthChange = { currentMonth = it })

                Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.w(16 / 411f))
                    ) {

                        WeekdayHeaders()

                        Spacer(modifier = Modifier.height(8.dp))

                        CalendarGrid(
                            currentMonth = currentMonth,
                            periodSettings = viewModel.periodSettings,
                            dailyDataMap = viewModel.dailyDataMap,
                            predictedPeriodStarts = predictedPeriodStarts,
                            predictedOvulationDays = predictedOvulationDays,
                            onDayClick = { date ->
                                selectedDateForDetail = date
                                val dailyData = viewModel.dailyDataMap[date]
                                if (dailyData?.isPeriodDay == true || dailyData?.notes?.isNotBlank() == true ||
                                    dailyData?.moodEmoji != null || dailyData?.symptoms?.isNotEmpty() == true ||
                                    dailyData?.flowIntensity != null
                                ) {
                                    selectedDateForSummary = date
                                    showDailySummarySheet = true
                                } else {
                                    showDailyDetailDialog = true
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

                PredictionCard(predictedPeriodStarts, predictedOvulationDays)
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    currentSettings = viewModel.periodSettings,
                    onSave = { newSettings ->
                        showSettingsDialog = false
                        viewModel.saveSettings(newSettings)
                    },
                    onCancel = { showSettingsDialog = false }
                )
            }

            if (showDailyDetailDialog && selectedDateForDetail != null) {
                DailyDetailDialog(
                    date = selectedDateForDetail!!,
                    initialDailyData = viewModel.dailyDataMap[selectedDateForDetail] ?: DailyPeriodData(
                        selectedDateForDetail!!
                    ),
                    onSave = { updatedData ->
                        showDailyDetailDialog = false
                        viewModel.saveDailyData(updatedData)
                    },
                    onCancel = { showDailyDetailDialog = false }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = dimens.h(16 / 914f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = showDailySummarySheet && selectedDateForSummary != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    selectedDateForSummary?.let { date ->
                        val dailyData = viewModel.dailyDataMap[date] ?: DailyPeriodData(date)
                        DailySummarySheet(
                            date = date,
                            dailyData = dailyData,
                            onEdit = {
                                showDailySummarySheet = false
                                selectedDateForDetail = it
                                showDailyDetailDialog = true
                            },
                            onClose = { showDailySummarySheet = false }
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PeriodTrackerScreenPreview() {
    PeriodTrackerScreen(navController = rememberNavController())
}
