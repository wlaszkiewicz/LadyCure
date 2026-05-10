package com.example.ladycure.domain.model

import java.time.LocalDate

data class PeriodTrackerSettings(
    val averagePeriodLength: Int = 5,
    val averageCycleLength: Int = 28,
    val lastPeriodStartDate: LocalDate? = null
)