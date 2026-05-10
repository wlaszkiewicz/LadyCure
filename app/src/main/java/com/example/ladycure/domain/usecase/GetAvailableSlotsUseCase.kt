package com.example.ladycure.domain.usecase

import com.example.ladycure.domain.model.Doctor
import com.example.ladycure.domain.model.DoctorAvailability
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAvailableSlotsUseCase @Inject constructor() {

    operator fun invoke(
        date: LocalDate,
        availabilities: List<DoctorAvailability>,
        appointmentDuration: Int
    ): List<String> {
        val today = LocalDate.now()
        val currentTimeWithBuffer = LocalTime.now().plusMinutes(30)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        val requiredSlots = appointmentDuration / 15

        val validDoctorIds = getDoctorsWithEnoughSlots(date, appointmentDuration, availabilities)
        val availableStartSlots = mutableListOf<LocalTime>()

        for (doctorId in validDoctorIds) {
            val doctorSlots = availabilities
                .filter { it.doctorId == doctorId && it.date == date }
                .flatMap { it.availableSlots }
                .sorted()

            for (i in 0..(doctorSlots.size - requiredSlots)) {
                val startSlot = doctorSlots[i]

                if (date == today && !startSlot.isAfter(currentTimeWithBuffer)) continue

                var hasConsecutive = true
                for (j in 1 until requiredSlots) {
                    if (doctorSlots.getOrNull(i + j) != startSlot.plusMinutes((15 * j).toLong())) {
                        hasConsecutive = false
                        break
                    }
                }

                if (hasConsecutive && startSlot !in availableStartSlots) {
                    availableStartSlots.add(startSlot)
                }
            }
        }

        return availableStartSlots.sorted().map { it.format(timeFormatter) }
    }

    fun getAvailableDoctors(
        doctors: List<Doctor>,
        date: LocalDate,
        timeSlot: LocalTime,
        availabilities: List<DoctorAvailability>,
        appointmentDuration: Int
    ): List<Doctor> {
        val requiredSlots = appointmentDuration / 15

        val availableDoctorIds = availabilities
            .filter { it.date == date && timeSlot in it.availableSlots }
            .filter { availability ->
                val doctorSlots = availabilities
                    .filter { it.doctorId == availability.doctorId && it.date == date }
                    .flatMap { it.availableSlots }
                    .sorted()

                val startIndex = doctorSlots.indexOf(timeSlot)
                startIndex != -1 &&
                        startIndex + requiredSlots <= doctorSlots.size &&
                        (1 until requiredSlots).all { i ->
                            doctorSlots[startIndex + i] == timeSlot.plusMinutes((15 * i).toLong())
                        }
            }
            .map { it.doctorId }
            .toSet()

        return doctors.filter { it.id in availableDoctorIds }
    }

    private fun getDoctorsWithEnoughSlots(
        date: LocalDate,
        appointmentDuration: Int,
        availabilities: List<DoctorAvailability>
    ): Set<String> {
        val requiredSlots = appointmentDuration / 15
        val validDoctorIds = mutableSetOf<String>()

        availabilities
            .filter { it.date == date }
            .groupBy { it.doctorId }
            .forEach { (doctorId, doctorAvailabilities) ->
                val allSlots = doctorAvailabilities.flatMap { it.availableSlots }.sorted()

                for (i in 0..(allSlots.size - requiredSlots)) {
                    val startSlot = allSlots[i]
                    val hasConsecutive = (1 until requiredSlots).all { j ->
                        allSlots.getOrNull(i + j) == startSlot.plusMinutes((15 * j).toLong())
                    }
                    if (hasConsecutive) {
                        validDoctorIds.add(doctorId)
                        break
                    }
                }
            }

        return validDoctorIds
    }
}
