package com.example.ladycure.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ladycure.R
import com.example.ladycure.ui.theme.DefaultBackground
import com.example.ladycure.ui.theme.DefaultOnPrimary
import com.example.ladycure.ui.theme.DefaultPrimary
import com.example.ladycure.ui.theme.LavenderBlush
import com.example.ladycure.ui.theme.rememberResponsiveDimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val dimens = rememberResponsiveDimens()
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
            modifier = Modifier.padding(bottom = dimens.h(16 / 914f))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.h(180 / 914f))
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
                                .fillMaxSize(0.9f)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDetailDialog(
    date: LocalDate,
    initialDailyData: DailyPeriodData,
    onSave: (DailyPeriodData) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .fillMaxHeight(0.80f)
                .clip(RoundedCornerShape(16.dp)),
            color = DefaultBackground,
            shadowElevation = 8.dp
        ) {
            DailyDetailContent(
                date = date,
                initialDailyData = initialDailyData,
                onSave = onSave,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
fun DailySummarySheet(
    date: LocalDate,
    dailyData: DailyPeriodData,
    onEdit: (LocalDate) -> Unit,
    onClose: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(dimens.w(16 / 411f))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.w(16 / 411f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = DefaultPrimary.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

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

            dailyData.moodEmoji?.let {
                Text(
                    text = "Mood: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DefaultOnPrimary
                )
            }

            if (dailyData.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${dailyData.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                )
            }

            if (dailyData.symptoms.isNotEmpty()) {
                Text(
                    text = "Symptoms: ${dailyData.symptoms.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DefaultOnPrimary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(dimens.h(16 / 914f)))

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
fun DailyDetailContent(
    date: LocalDate,
    initialDailyData: DailyPeriodData,
    onSave: (DailyPeriodData) -> Unit,
    onCancel: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    var dailyData by remember { mutableStateOf(initialDailyData) }
    var noteText by remember { mutableStateOf(initialDailyData.notes) }
    var selectedFlowIntensity by remember { mutableStateOf(initialDailyData.flowIntensity) }
    var selectedSymptoms by remember { mutableStateOf(initialDailyData.symptoms.toSet()) }

    val maxNoteLength = 200

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(0.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = LavenderBlush.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimens.w(24 / 411f), vertical = dimens.h(24 / 914f))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.h(16 / 914f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DefaultPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(dimens.w(32 / 411f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = DefaultPrimary.copy(alpha = 0.7f)
                    )
                }
            }

            PeriodDayToggle(
                isPeriodDay = dailyData.isPeriodDay,
                onToggle = {
                    dailyData = dailyData.copy(isPeriodDay = it)
                    if (!it) {
                        selectedFlowIntensity = null
                    }
                }
            )

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
                        selectedFlowIntensity = selectedFlowIntensity,
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

            NotesField(
                noteText = noteText,
                maxNoteLength = maxNoteLength,
                onNoteChange = { text ->
                    noteText = text
                    dailyData = dailyData.copy(notes = text)
                }
            )

            SymptomTracker(
                selectedSymptoms = selectedSymptoms,
                onSymptomToggle = { symptom, isSelected ->
                    selectedSymptoms = if (isSelected) {
                        selectedSymptoms + symptom
                    } else {
                        selectedSymptoms - symptom
                    }
                }
            )

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
fun PeriodDayToggle(
    isPeriodDay: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPeriodDay) DefaultPrimary.copy(alpha = 0.2f)
            else LavenderBlush.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPeriodDay) DefaultPrimary
            else DefaultPrimary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.w(16 / 411f), vertical = dimens.h(12 / 914f)),
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
fun FlowIntensitySelector(
    selectedFlowIntensity: String?,
    onSelectionChanged: (String?) -> Unit
) {
    val dimens = rememberResponsiveDimens()
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
                    .height(dimens.h(48 / 914f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) colors[index]
                        else LavenderBlush.copy(alpha = 0.5f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) DefaultPrimary
                        else DefaultPrimary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectionChanged(if (isSelected) null else intensity) },
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
fun NotesField(
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
                unfocusedBorderColor = DefaultPrimary.copy(alpha = 0.3f),
                focusedTextColor = DefaultOnPrimary,
                unfocusedTextColor = DefaultOnPrimary,
                focusedContainerColor = LavenderBlush.copy(alpha = 0.5f),
                unfocusedContainerColor = LavenderBlush.copy(alpha = 0.3f)
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
fun SymptomTracker(
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
fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val dimens = rememberResponsiveDimens()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.2f),
        border = BorderStroke(
            1.dp,
            if (isSelected) DefaultPrimary else DefaultPrimary.copy(alpha = 0.5f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = dimens.w(12 / 411f), vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else DefaultPrimary
        )
    }
}

@Composable
fun SaveCancelButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(dimens.h(50 / 914f)),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Cancel",
                color = DefaultPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(dimens.w(16 / 411f)))
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(dimens.h(50 / 914f)),
            colors = ButtonDefaults.buttonColors(
                containerColor = DefaultPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}
