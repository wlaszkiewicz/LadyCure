package com.example.ladycure.domain.model

import java.time.LocalDate

data class DailyPeriodData(
    val date: LocalDate,
    var isPeriodDay: Boolean = false,
    var notes: String = "",
    var moodEmoji: String? = null,
    var flowIntensity: String? = null,
    var symptoms: List<String> = emptyList()
)

