package com.example.ladycure.presentation.home

import DefaultBackground
import DefaultOnPrimary
import DefaultPrimary
import android.util.Log
import com.example.ladycure.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ladycure.data.repository.PeriodTrackerRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.max
import kotlin.math.min

// --- Data Models (Moved to top for better visibility, though ideally in a separate 'data' package) ---
data class DailyPeriodData(
    val date: LocalDate,
    var isPeriodDay: Boolean = false,
    var notes: String = "",
    var moodEmoji: String? = null,
    var flowIntensity: String? = null, // Added for flow intensity
    var symptoms: List<String> = emptyList() // Added for symptoms
)

data class PeriodTrackerSettings(
    val averagePeriodLength: Int = 5,
    val averageCycleLength: Int = 28,
    val lastPeriodStartDate: LocalDate? = null
)

// --- Helper Functions ---
fun calculateNextPeriodAndOvulation(
    lastPeriodStartDate: LocalDate?,
    averageCycleLength: Int
): Pair<LocalDate?, LocalDate?> {
    if (lastPeriodStartDate == null) {
        return Pair(null, null)
    }

    val predictedNextPeriodStart = lastPeriodStartDate.plusDays(averageCycleLength.toLong())
    val predictedOvulationDay = predictedNextPeriodStart.minusDays(14)
    return Pair(predictedNextPeriodStart, predictedOvulationDay)
}

fun getPredictedPeriodStartDates(
    lastPeriodStartDate: LocalDate?,
    averageCycleLength: Int
): Set<LocalDate> {
    val predictedStarts = mutableSetOf<LocalDate>()
    if (lastPeriodStartDate == null) return predictedStarts

    predictedStarts.add(lastPeriodStartDate)
    var currentPrediction = lastPeriodStartDate

    // Predict future periods for the next 12 cycles
    repeat(12) {
        currentPrediction = currentPrediction?.plusDays(averageCycleLength.toLong())
        currentPrediction?.let { predictedStarts.add(it) }
    }

    // Predict up to 3 past cycles
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

// --- Period Tracker Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(navController: NavHostController) {
    // Initialize the repository
    val periodTrackerRepository = remember { PeriodTrackerRepository() }

    // Coroutine scope for launching suspend functions from event handlers
    val scope = rememberCoroutineScope()

    // State management
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var periodSettings by remember { mutableStateOf(PeriodTrackerSettings()) }
    val dailyDataMap = remember { mutableStateMapOf<LocalDate, DailyPeriodData>() }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDailyDetailDialog by remember { mutableStateOf(false) }
    var selectedDateForDetail by remember { mutableStateOf<LocalDate?>(null) }
    var showDailySummarySheet by remember { mutableStateOf(false) } // New state for summary sheet
    var selectedDateForSummary by remember { mutableStateOf<LocalDate?>(null) } // New state for summary date

    // Effect to load initial data and settings from Firestore
    LaunchedEffect(Unit) {
        // Load settings
        periodTrackerRepository.getPeriodTrackerSettings().onSuccess { settings ->
            periodSettings = settings
            Log.d("PeriodTrackerScreen", "Loaded settings: $settings")
        }.onFailure { e ->
            Log.e("PeriodTrackerScreen", "Failed to load settings: ${e.message}")
            // Optionally, handle error by setting default settings or showing an error message
        }

        // Load daily data for the current month
        periodTrackerRepository.getDailyPeriodDataForMonth(currentMonth).onSuccess { data ->
            dailyDataMap.clear()
            dailyDataMap.putAll(data)
            Log.d(
                "PeriodTrackerScreen",
                "Loaded daily data for ${currentMonth.month}: ${data.size} entries"
            )
        }.onFailure { e ->
            Log.e(
                "PeriodTrackerScreen",
                "Failed to load daily data for ${currentMonth.month}: ${e.message}"
            )
        }
    }

    // Effect to reload daily data when the month changes
    LaunchedEffect(currentMonth) {
        periodTrackerRepository.getDailyPeriodDataForMonth(currentMonth).onSuccess { data ->
            dailyDataMap.clear()
            dailyDataMap.putAll(data)
            Log.d(
                "PeriodTrackerScreen",
                "Reloaded daily data for ${currentMonth.month}: ${data.size} entries"
            )
        }.onFailure { e ->
            Log.e(
                "PeriodTrackerScreen",
                "Failed to reload daily data for ${currentMonth.month}: ${e.message}"
            )
        }
    }

    // Recalculate predictions whenever periodSettings changes
    val predictedPeriodStarts by remember(
        periodSettings.lastPeriodStartDate,
        periodSettings.averageCycleLength
    ) {
        mutableStateOf(
            getPredictedPeriodStartDates(
                periodSettings.lastPeriodStartDate,
                periodSettings.averageCycleLength
            )
        )
    }

    val predictedOvulationDays by remember(
        periodSettings.lastPeriodStartDate,
        periodSettings.averageCycleLength
    ) {
        mutableStateOf(
            getPredictedOvulationDates(
                periodSettings.lastPeriodStartDate,
                periodSettings.averageCycleLength
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Period Tracker",
                        color = DefaultOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DefaultOnPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, "Settings", tint = DefaultPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DefaultBackground)
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DefaultBackground)
                    .padding(paddingValues)
            ) {
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Month navigation
                    MonthNavigationHeader(currentMonth, onMonthChange = { currentMonth = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekday headers
                    WeekdayHeaders()

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar grid
                    CalendarGrid(
                        currentMonth = currentMonth,
                        periodSettings = periodSettings,
                        dailyDataMap = dailyDataMap,
                        predictedPeriodStarts = predictedPeriodStarts,
                        predictedOvulationDays = predictedOvulationDays,
                        onDayClick = { date ->
                            selectedDateForDetail = date // Always set this for detail dialog
                            val dailyData = dailyDataMap[date]
                            // Check if there's any info for the day
                            if (dailyData?.isPeriodDay == true || dailyData?.notes?.isNotBlank() == true ||
                                dailyData?.moodEmoji != null || dailyData?.symptoms?.isNotEmpty() == true ||
                                dailyData?.flowIntensity != null
                            ) {
                                selectedDateForSummary = date // Set for summary sheet
                                showDailySummarySheet = true
                            } else {
                                showDailyDetailDialog = true // Directly open detail for empty days
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prediction card
                    PredictionCard(predictedPeriodStarts, predictedOvulationDays)
                }

                // Dialogs
                if (showSettingsDialog) {
                    SettingsDialog(
                        currentSettings = periodSettings,
                        onSave = { newSettings ->
                            showSettingsDialog = false
                            periodSettings = newSettings // Update local state immediately
                            // Save new settings to Firestore using the coroutine scope
                            scope.launch {
                                periodTrackerRepository.savePeriodTrackerSettings(newSettings)
                                    .onSuccess {
                                        Log.d("PeriodTrackerScreen", "Settings saved successfully.")
                                    }.onFailure { e ->
                                        Log.e(
                                            "PeriodTrackerScreen",
                                            "Failed to save settings: ${e.message}"
                                        )
                                    }
                            }
                        },
                        onCancel = { showSettingsDialog = false }
                    )
                }

                if (showDailyDetailDialog && selectedDateForDetail != null) {
                    DailyDetailDialog(
                        date = selectedDateForDetail!!,
                        initialDailyData = dailyDataMap[selectedDateForDetail] ?: DailyPeriodData(
                            selectedDateForDetail!!
                        ),
                        onSave = { updatedData ->
                            showDailyDetailDialog = false
                            // Update local state immediately
                            dailyDataMap[updatedData.date] = updatedData
                            // Save updated daily data to Firestore using the coroutine scope
                            scope.launch {
                                periodTrackerRepository.saveDailyPeriodData(updatedData).onSuccess {
                                    Log.d(
                                        "PeriodTrackerScreen",
                                        "Daily data saved successfully for ${updatedData.date}."
                                    )
                                }.onFailure { e ->
                                    Log.e(
                                        "PeriodTrackerScreen",
                                        "Failed to save daily data: ${e.message}"
                                    )
                                }
                            }
                        },
                        onCancel = { showDailyDetailDialog = false }
                    )
                }

                // Daily Summary Sheet (appears at the bottom)
                AnimatedVisibility(
                    visible = showDailySummarySheet && selectedDateForSummary != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    selectedDateForSummary?.let { date ->
                        val dailyData = dailyDataMap[date] ?: DailyPeriodData(date)
                        DailySummarySheet(
                            date = date,
                            dailyData = dailyData,
                            onEdit = {
                                showDailySummarySheet = false // Hide summary
                                selectedDateForDetail = it // Set for detail dialog
                                showDailyDetailDialog = true // Show detail dialog
                            },
                            onClose = { showDailySummarySheet = false }
                        )
                    }
                }
            }
        }
    )
}

data class MoodOption(
    val drawableResId: Int,
    val name: String
)

@Composable
fun MoodGrid(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = remember {
        listOf(
            MoodOption(R.drawable.happy_kapi_emote, "Happy"),
            MoodOption(R.drawable.love_kapi_emote, "Love"),
            MoodOption(R.drawable.mad_kapi_emote, "Mad"),
            MoodOption(R.drawable.sad_kapi_emote, "Sad"),
            MoodOption(R.drawable.sick_kapi_emote, "Sick"),
            MoodOption(R.drawable.tired_kapi_emote, "Tired")
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary,
            modifier = Modifier.padding(bottom = 16.dp) // Reduced bottom padding
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(2.dp), // Reduced vertical spacing
            horizontalArrangement = Arrangement.spacedBy(2.dp), // Reduced horizontal spacing
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp) // Reduced overall height
        ) {
            items(moods) { mood ->
                val isSelected = selectedMood == mood.name
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    label = "moodScale"
                )

                Card(
                    modifier = Modifier
                        .scale(scale)
                        .aspectRatio(1f)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = DefaultPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onMoodSelected(mood.name) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            DefaultPrimary.copy(alpha = 0.2f)
                        else
                            Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = mood.drawableResId),
                            contentDescription = mood.name,
                            modifier = Modifier
                                .fillMaxSize(0.9f) // Make emote fill most of the button
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun MonthNavigationHeader(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Month", tint = DefaultPrimary)
        }
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM")),
            style = MaterialTheme.typography.headlineSmall,
            color = DefaultPrimary,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month", tint = DefaultPrimary)
        }
    }
}

@Composable
private fun WeekdayHeaders() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PredictionCard(
    predictedPeriodStarts: Set<LocalDate>,
    predictedOvulationDays: Set<LocalDate>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Next Cycle Predictions",
                style = MaterialTheme.typography.titleLarge,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.Bold
            )

            val nextPredictedPeriod =
                predictedPeriodStarts.filter { it.isAfter(LocalDate.now()) }.minOrNull()
            nextPredictedPeriod?.let {
                Text(
                    text = "Next Period Expected: ${it.format(DateTimeFormatter.ofPattern("MMM dd,yyyy"))}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary.copy(alpha = 0.8f)
                )
            } ?: Text(
                text = "Set your last period start date in settings to see predictions.",
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.6f)
            )

            val nextPredictedOvulation =
                predictedOvulationDays.filter { it.isAfter(LocalDate.now()) }.minOrNull()
            nextPredictedOvulation?.let {
                Text(
                    text = "Ovulation Day: ${it.format(DateTimeFormatter.ofPattern("MMM dd,yyyy"))}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultPrimary
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    periodSettings: PeriodTrackerSettings,
    dailyDataMap: Map<LocalDate, DailyPeriodData>,
    predictedPeriodStarts: Set<LocalDate>,
    predictedOvulationDays: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.with(TemporalAdjusters.firstDayOfMonth())
    val lastDayOfMonth = currentMonth.with(TemporalAdjusters.lastDayOfMonth())

    var startDay = firstDayOfMonth
    while (startDay.dayOfWeek != DayOfWeek.SUNDAY) {
        startDay = startDay.minusDays(1)
    }

    val daysInCalendar = remember(currentMonth) {
        mutableListOf<LocalDate>().apply {
            var tempDay = startDay
            while (tempDay.isBefore(lastDayOfMonth.plusDays(1)) || tempDay.dayOfWeek != DayOfWeek.SATURDAY) {
                add(tempDay)
                tempDay = tempDay.plusDays(1)
            }
            // Ensure full 6 weeks for visual consistency
            while (size < 42) {
                add(last().plusDays(1)) // Corrected to add next day
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(daysInCalendar) { date ->
            val isCurrentMonth = date.month == currentMonth.month && date.year == currentMonth.year
            val isToday = date == LocalDate.now()
            val dailyData = dailyDataMap[date]

            val isPeriodDay = dailyData?.isPeriodDay ?: false
            val hasNotesOrMood =
                dailyData?.notes?.isNotBlank() == true || dailyData?.moodEmoji != null || dailyData?.symptoms?.isNotEmpty() == true

            val isPredictedPeriodStart = predictedPeriodStarts.contains(date) && !isPeriodDay
            val isPredictedPeriodDayRange = predictedPeriodStarts.any { periodStartDate ->
                date.isAfter(periodStartDate.minusDays(1)) &&
                        date.isBefore(periodStartDate.plusDays(periodSettings.averagePeriodLength.toLong()))
            } && !isPeriodDay

            val isPredictedOvulationDay = predictedOvulationDays.contains(date)

            CalendarDay(
                date = date,
                isCurrentMonth = isCurrentMonth,
                isToday = isToday,
                isPeriodDay = isPeriodDay,
                isPredictedPeriodStart = isPredictedPeriodStart,
                isPredictedPeriodDayRange = isPredictedPeriodDayRange,
                isPredictedOvulationDay = isPredictedOvulationDay,
                hasNotesOrMood = hasNotesOrMood,
                onClick = { if (isCurrentMonth) onDayClick(date) }
            )
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isPeriodDay: Boolean,
    isPredictedPeriodStart: Boolean,
    isPredictedPeriodDayRange: Boolean,
    isPredictedOvulationDay: Boolean,
    hasNotesOrMood: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isPeriodDay -> DefaultPrimary.copy(alpha = 0.8f)
                    isPredictedPeriodDayRange -> DefaultPrimary.copy(alpha = 0.2f)
                    isPredictedOvulationDay -> Color(0xFFC8A2C8).copy(alpha = 0.4f)
                    isToday -> DefaultPrimary.copy(alpha = 0.1f)
                    else -> Color.White.copy(alpha = if (isCurrentMonth) 0.05f else 0.02f)
                }
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) DefaultPrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isPeriodDay -> Color.White
                    isPredictedPeriodDayRange -> DefaultPrimary
                    isPredictedOvulationDay -> Color(0xFF8B008B)
                    isToday -> DefaultPrimary
                    isCurrentMonth -> DefaultOnPrimary
                    else -> DefaultOnPrimary.copy(alpha = 0.4f)
                },
                fontWeight = if (isToday || isPeriodDay || isPredictedPeriodDayRange || isPredictedOvulationDay)
                    FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )

            if (hasNotesOrMood) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary)
                )
            }

            if (isPredictedPeriodStart) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(DefaultPrimary)
                )
            }

            if (isPredictedOvulationDay) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B008B))
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    currentSettings: PeriodTrackerSettings,
    onSave: (PeriodTrackerSettings) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(16.dp)),
            color = DefaultBackground,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PeriodTrackerSettingsContent(
                    currentPeriodSettings = currentSettings,
                    onSave = onSave,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun DailyDetailDialog(
    date: LocalDate,
    initialDailyData: DailyPeriodData,
    onSave: (DailyPeriodData) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        DailyDetailContent(
            date = date,
            initialDailyData = initialDailyData,
            onSave = onSave,
            onCancel = onCancel
        )
    }
}

@Composable
private fun DailySummarySheet(
    date: LocalDate,
    dailyData: DailyPeriodData,
    onEdit: (LocalDate) -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with date and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close", tint = DefaultPrimary.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Display Period Day status
            if (dailyData.isPeriodDay) {
                Text(
                    text = "Period Day: Yes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary
                )
                dailyData.flowIntensity?.let {
                    Text(
                        text = "Flow: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DefaultOnPrimary.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    text = "Period Day: No",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary
                )
            }

            // Display Mood
            dailyData.moodEmoji?.let {
                Text(
                    text = "Mood: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary
                )
            }

            // Display Notes
            if (dailyData.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${dailyData.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                )
            }

            // Display Symptoms
            if (dailyData.symptoms.isNotEmpty()) {
                Text(
                    text = "Symptoms: ${dailyData.symptoms.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Button
            Button(
                onClick = { onEdit(date) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DefaultPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Details", color = Color.White)
            }
        }
    }
}

@Composable
private fun PeriodTrackerSettingsContent(
    currentPeriodSettings: PeriodTrackerSettings,
    onSave: (PeriodTrackerSettings) -> Unit,
    onCancel: () -> Unit
) {
    var periodLength by remember { mutableStateOf(currentPeriodSettings.averagePeriodLength) }
    var cycleLength by remember { mutableStateOf(currentPeriodSettings.averageCycleLength) }
    var lastPeriodStartDate by remember { mutableStateOf(currentPeriodSettings.lastPeriodStartDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Period Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = DefaultOnPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        SettingCard(
            title = "Average Period Length",
            description = "How many days does your period usually last?",
            value = "$periodLength days"
        ) {
            NumberSelector(
                value = periodLength,
                minValue = 1,
                maxValue = 14,
                onValueChange = { periodLength = it }
            )
        }

        SettingCard(
            title = "Average Cycle Length",
            description = "How many days are between the first day of one period and the first day of the next?",
            value = "$cycleLength days"
        ) {
            NumberSelector(
                value = cycleLength,
                minValue = 21,
                maxValue = 45,
                onValueChange = { cycleLength = it }
            )
        }

        SettingCard(
            title = "Last Period Start Date",
            description = "When did your last period start?",
            value = lastPeriodStartDate?.format(DateTimeFormatter.ofPattern("MMM dd,yyyy"))
                ?: "Select Date"
        ) {
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary.copy(alpha = 0.1f),
                    contentColor = DefaultPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DateRange, "Select Date", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lastPeriodStartDate?.format(DateTimeFormatter.ofPattern("MMM dd,yyyy"))
                        ?: "Select Date",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    initialDate = lastPeriodStartDate ?: LocalDate.now(),
                    onDateSelected = { date ->
                        lastPeriodStartDate = date
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultOnPrimary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Cancel",
                    color = DefaultOnPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    onSave(PeriodTrackerSettings(periodLength, cycleLength, lastPeriodStartDate))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DefaultPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun DailyDetailContent(
    date: LocalDate,
    initialDailyData: DailyPeriodData,
    onSave: (DailyPeriodData) -> Unit,
    onCancel: () -> Unit
) {
    var dailyData by remember { mutableStateOf(initialDailyData) }
    var noteText by remember { mutableStateOf(initialDailyData.notes) }
    var selectedFlowIntensity by remember { mutableStateOf(initialDailyData.flowIntensity) } // State for flow intensity
    // Changed to mutableStateOf(initial value.toSet()) and reassigning the set
    var selectedSymptoms by remember { mutableStateOf(initialDailyData.symptoms.toSet()) }

    val maxNoteLength = 200

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F5).copy(alpha = 0.95f) // Light pink background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
        ) {
            // Header with date and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DefaultPrimary, // Changed header text color to DefaultPrimary
                    fontWeight = FontWeight.ExtraBold // Made header bolder
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(32.dp)
                ) { // Increased icon size
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = DefaultPrimary.copy(alpha = 0.7f) // Adjusted tint
                    )
                }
            }

            // Period day toggle
            PeriodDayToggle(
                isPeriodDay = dailyData.isPeriodDay,
                onToggle = {
                    dailyData = dailyData.copy(isPeriodDay = it)
                    if (!it) { // If period day is toggled off, clear flow intensity
                        selectedFlowIntensity = null
                    }
                }
            )

            // Flow intensity selector (only shown if period day)
            AnimatedVisibility(
                visible = dailyData.isPeriodDay,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Flow Intensity",
                        style = MaterialTheme.typography.titleMedium,
                        color = DefaultOnPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowIntensitySelector(
                        selectedFlowIntensity = selectedFlowIntensity, // Pass selected flow
                        onSelectionChanged = { selectedFlowIntensity = it }
                    )
                }
            }

            MoodGrid(
                selectedMood = dailyData.moodEmoji,
                onMoodSelected = { moodName ->
                    dailyData = if (dailyData.moodEmoji == moodName) {
                        dailyData.copy(moodEmoji = null)
                    } else {
                        dailyData.copy(moodEmoji = moodName)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Notes section
            NotesField(
                noteText = noteText,
                maxNoteLength = maxNoteLength,
                onNoteChange = { text ->
                    noteText = text
                    dailyData = dailyData.copy(notes = text)
                }
            )

            // Symptom tracker
            SymptomTracker(
                selectedSymptoms = selectedSymptoms,
                onSymptomToggle = { symptom, isSelected ->
                    selectedSymptoms = if (isSelected) {
                        selectedSymptoms + symptom // Create a new set to trigger recomposition
                    } else {
                        selectedSymptoms - symptom // Create a new set to trigger recomposition
                    }
                }
            )

            // Save/Cancel buttons
            SaveCancelButtons(
                onSave = {
                    onSave(
                        dailyData.copy(
                            flowIntensity = selectedFlowIntensity,
                            symptoms = selectedSymptoms.toList()
                        )
                    )
                },
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun NumberSelector(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onValueChange(max(minValue, value - 1)) },
            modifier = Modifier
                .size(40.dp)
                .background(DefaultPrimary.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("-", color = DefaultPrimary, fontWeight = FontWeight.Bold)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = DefaultOnPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(
            onClick = { onValueChange(min(maxValue, value + 1)) },
            modifier = Modifier
                .size(40.dp)
                .background(DefaultPrimary.copy(alpha = 0.1f), CircleShape)
        ) {
            Text("+", color = DefaultPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(initialDate) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = DefaultBackground
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Previous Month",
                            tint = DefaultPrimary
                        )
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                        style = MaterialTheme.typography.titleLarge,
                        color = DefaultPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            "Next Month",
                            tint = DefaultPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = DefaultOnPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val daysInCalendar = remember(currentMonth) {
                    val firstDayOfMonth = currentMonth.with(TemporalAdjusters.firstDayOfMonth())
                    val lastDayOfMonth = currentMonth.with(TemporalAdjusters.lastDayOfMonth())

                    var startDay = firstDayOfMonth
                    while (startDay.dayOfWeek != DayOfWeek.SUNDAY) {
                        startDay = startDay.minusDays(1)
                    }

                    mutableListOf<LocalDate>().apply {
                        var tempDay = startDay
                        while (tempDay.isBefore(lastDayOfMonth.plusDays(1)) || tempDay.dayOfWeek != DayOfWeek.SATURDAY) {
                            add(tempDay)
                            tempDay = tempDay.plusDays(1)
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInCalendar) { date ->
                        val isCurrentMonth =
                            date.month == currentMonth.month && date.year == currentMonth.year
                        val isSelected = date == selectedDate

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) DefaultPrimary else Color.Transparent)
                                .border(
                                    width = if (date == LocalDate.now()) 1.dp else 0.dp,
                                    color = DefaultPrimary,
                                    shape = CircleShape
                                )
                                .clickable(enabled = isCurrentMonth) { selectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSelected -> Color.White
                                    isCurrentMonth -> DefaultOnPrimary
                                    else -> DefaultOnPrimary.copy(alpha = 0.4f)
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultOnPrimary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = DefaultOnPrimary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            onDateSelected(selectedDate)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DefaultPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodDayToggle(
    isPeriodDay: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPeriodDay) DefaultPrimary.copy(alpha = 0.2f)
            else Color(0xFFFFF0F5).copy(alpha = 0.5f) // Light pink tint when off
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPeriodDay) DefaultPrimary
            else DefaultPrimary.copy(alpha = 0.3f) // Pink border when off
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Period Day",
                    style = MaterialTheme.typography.titleMedium,
                    color = DefaultOnPrimary
                )
                Text(
                    text = if (isPeriodDay) "Tracking period flow" else "Not a period day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = isPeriodDay,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DefaultPrimary,
                    uncheckedThumbColor = DefaultOnPrimary.copy(alpha = 0.5f),
                    uncheckedTrackColor = DefaultOnPrimary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun FlowIntensitySelector(
    selectedFlowIntensity: String?,
    onSelectionChanged: (String?) -> Unit
) {
    val intensities = listOf("Light", "Medium", "Heavy")
    val colors = listOf(
        DefaultPrimary.copy(alpha = 0.3f),
        DefaultPrimary.copy(alpha = 0.6f),
        DefaultPrimary.copy(alpha = 0.9f)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        intensities.forEachIndexed { index, intensity ->
            val isSelected = selectedFlowIntensity == intensity
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (index < intensities.size - 1) 8.dp else 0.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) colors[index]
                        else Color(0xFFFFF0F5).copy(alpha = 0.5f) // Light pink tint when off
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) DefaultPrimary
                        else DefaultPrimary.copy(alpha = 0.3f), // Pink border when off
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectionChanged(if (isSelected) null else intensity) }, // Toggle selection
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = intensity,
                    color = if (isSelected) Color.White
                    else DefaultOnPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold
                    else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NotesField(
    noteText: String,
    maxNoteLength: Int,
    onNoteChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = noteText,
            onValueChange = { newText ->
                if (newText.length <= maxNoteLength) {
                    onNoteChange(newText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp, max = 150.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DefaultPrimary,
                unfocusedBorderColor = DefaultPrimary.copy(alpha = 0.3f), // Pink border
                focusedTextColor = DefaultOnPrimary,
                unfocusedTextColor = DefaultOnPrimary,
                focusedContainerColor = Color(0xFFFFF0F5).copy(alpha = 0.5f), // Light pink container
                unfocusedContainerColor = Color(0xFFFFF0F5).copy(alpha = 0.3f) // Lighter pink container
            ),
            label = {
                Text(
                    "Add notes about your day...",
                    color = DefaultOnPrimary.copy(alpha = 0.6f)
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = false,
            trailingIcon = {
                Text(
                    text = "${noteText.length}/$maxNoteLength",
                    style = MaterialTheme.typography.labelSmall,
                    color = DefaultOnPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )
    }
}

@Composable
private fun SymptomTracker(
    selectedSymptoms: Set<String>,
    onSymptomToggle: (String, Boolean) -> Unit
) {
    val commonSymptoms = remember {
        listOf(
            "Cramps", "Headache", "Bloating", "Fatigue", "Mood Swings",
            "Acne", "Tender Breasts", "Backache", "Nausea", "Insomnia"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Symptoms",
            style = MaterialTheme.typography.titleMedium,
            color = DefaultOnPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonSymptoms.forEach { symptom ->
                val isSelected = symptom in selectedSymptoms
                Chip(
                    text = symptom,
                    isSelected = isSelected,
                    onClick = { onSymptomToggle(symptom, !isSelected) }
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        // Adjusted colors based on isSelected
        color = if (isSelected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.2f),
        border = BorderStroke(
            1.dp,
            if (isSelected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.5f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            // Adjusted text color based on isSelected
            color = if (isSelected) Color.White else DefaultPrimary
        )
    }
}


@Composable
private fun SaveCancelButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary.copy(alpha = 0.2f) // Pink cancel button
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Cancel",
                color = DefaultPrimary,
                style = MaterialTheme.typography.titleMedium
            ) // Pink text
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Composable
fun SettingCard(
    title: String,
    description: String,
    value: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.5f) // Matches aesthetic
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = DefaultOnPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = DefaultOnPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PeriodTrackerScreenPreview() {
    PeriodTrackerScreen(navController = rememberNavController())
}
