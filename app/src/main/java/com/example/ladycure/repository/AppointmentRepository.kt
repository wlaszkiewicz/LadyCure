package com.example.ladycure.repository

import com.example.ladycure.data.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AppointmentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance("telecure")


    suspend fun bookAppointment(
        appointment: Appointment
    ): Result<String> {
        try {
            val appointmentData =
                appointment.toMap(appointment).filterKeys { it != "appointmentId" }

            val documentReference =
                firestore.collection("appointments").add(appointmentData).await()
            val appointmentId = documentReference.id

            // make the timeslot unavailable

            val doctorId = appointment.doctorId
            val date = appointment.date
            val startTime = appointment.time
            val endTime =
                startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            // Fetch the current available slots
            val docRef = firestore.collection("users")
                .document(doctorId)
                .collection("availability")
                .document(date.toString())

            val availabilitySnapshot = docRef.get().await()
            if (availabilitySnapshot.exists()) {
                val availableSlots =
                    availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                val updatedAvailableSlots = availableSlots.filterNot { slot ->
                    val slotTime = LocalTime.parse(
                        slot,
                        DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                    )
                    (slotTime.isAfter(startTime) && slotTime.isBefore(endTime)) || slotTime == startTime
                }
                docRef.update("availableSlots", updatedAvailableSlots).await()
            } else {
                return Result.failure(Exception("Availability document does not exist"))
            }

            return Result.success(appointmentId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getAppointmentById(id: String): Result<Appointment> {
        return try {
            val document = firestore.collection("appointments").document(id).get().await()
            if (document.exists()) {
                val appointment = Appointment.fromMap(
                    document.data?.plus("appointmentId" to id) ?: mapOf("appointmentId" to id)
                )
                Result.success(appointment)
            } else {
                Result.failure(Exception("Appointment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(role: String): Result<List<Appointment>> {
        return try {
            val id = if (role == "doctor") {
                "doctorId"
            } else {
                "patientId"
            }
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val querySnapshot = firestore.collection("appointments")
                .whereEqualTo(id, userId)
                .get()
                .await()

            val appointments = querySnapshot.documents.map { doc ->
                Appointment.fromMap(
                    doc.data?.plus("appointmentId" to doc.id) ?: mapOf("appointmentId" to doc.id)
                )
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentComment(appointmentId: String, comment: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("comments", comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val appointmentRef = firestore.collection("appointments").document(appointmentId)
                val appointmentSnapshot = transaction.get(appointmentRef)

                if (!appointmentSnapshot.exists()) {
                    throw Exception("Appointment not found")
                }

                val appointment = Appointment.fromMap(
                    appointmentSnapshot.data?.plus("appointmentId" to appointmentId)
                        ?: throw Exception("Invalid appointment data")
                )

                val doctorId = appointment.doctorId
                val date = appointment.date
                val startTime = appointment.time
                val endTime =
                    startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

                val availableSlots = mutableSetOf<LocalTime>()
                var currentTime = startTime
                while (currentTime.isBefore(endTime)) {
                    availableSlots.add(currentTime)
                    currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
                }

                val docRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(date.toString())

                val availabilitySnapshot = transaction.get(docRef)
                if (availabilitySnapshot.exists()) {
                    val currentAvailableSlots =
                        availabilitySnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedAvailableSlots = (currentAvailableSlots + availableSlots.map {
                        it.format(
                            DateTimeFormatter.ofPattern(
                                "h:mm a",
                                java.util.Locale.US
                            )
                        )
                    }).distinct().sortedBy {
                        LocalTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                    }
                    transaction.update(docRef, "availableSlots", updatedAvailableSlots)
                } else {
                    throw Exception("Availability document does not exist")
                }

                transaction.delete(appointmentRef)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rescheduleAppointment(
        appointmentId: String,
        newTime: LocalTime,
        newDate: LocalDate
    ): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            val appointmentSnapshot = appointmentRef.get().await()

            if (!appointmentSnapshot.exists()) {
                return Result.failure(Exception("Appointment not found"))
            }

            val appointment = Appointment.fromMap(
                appointmentSnapshot.data?.plus("appointmentId" to appointmentId)
                    ?: throw Exception("Invalid appointment data")
            )

            val doctorId = appointment.doctorId
            val oldDate = appointment.date
            val oldStartTime = appointment.time
            val oldEndTime =
                oldStartTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            val newEndTime =
                newTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)

            // Update the appointment with the new date and time
            val updatedAppointmentData = mapOf(
                "date" to newDate.format(
                    DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd",
                        java.util.Locale.US
                    )
                ),
                "time" to newTime.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
            )

            appointmentRef.update(updatedAppointmentData).await()

            // get 15 minute slots that we can add back to the doctor's availability
            val againAvailableSlots = mutableSetOf<LocalTime>()
            var currentTime = oldStartTime
            while (currentTime.isBefore(oldEndTime)) {
                againAvailableSlots.add(currentTime)
                currentTime = currentTime.plus(15, ChronoUnit.MINUTES)
            }


            firestore.runTransaction { transaction ->

                val oldDateRef = firestore.collection("users")
                    .document(doctorId)
                    .collection("availability")
                    .document(oldDate.toString())
                val oldDateSnapshot = transaction.get(oldDateRef)

                val newDateRef = if (oldDate != newDate) {
                    firestore.collection("users")
                        .document(doctorId)
                        .collection("availability")
                        .document(newDate.toString())
                } else {
                    oldDateRef
                }
                val newDateSnapshot =
                    if (oldDate != newDate) transaction.get(newDateRef) else oldDateSnapshot

                if (!oldDateSnapshot.exists() || !newDateSnapshot.exists()) {
                    throw Exception("Availability document does not exist")
                }

                val doctorStartTime = oldDateSnapshot.getString("startTime")?.let {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                } ?: LocalTime.MIN
                val doctorEndTime = oldDateSnapshot.getString("endTime")?.let {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                } ?: LocalTime.MAX

                val filteredSlots = againAvailableSlots.filter { slot ->
                    (slot.isAfter(doctorStartTime) && slot.isBefore(doctorEndTime)) || slot == doctorStartTime
                }

                val currentAvailableSlots =
                    oldDateSnapshot.get("availableSlots") as? List<String> ?: emptyList()

                var updatedAvailableSlots = (currentAvailableSlots + filteredSlots.map {
                    it.format(DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                }).distinct()

                if (oldDate == newDate) {
                    updatedAvailableSlots = updatedAvailableSlots.filterNot { slot ->
                        val slotTime = LocalTime.parse(
                            slot,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                        (slotTime.isAfter(newTime) && slotTime.isBefore(newEndTime)) || slotTime == newTime
                    }
                }

                updatedAvailableSlots = updatedAvailableSlots.sortedBy {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US))
                }

                transaction.update(oldDateRef, "availableSlots", updatedAvailableSlots)

                if (oldDate != newDate) {
                    val newAvailableSlots =
                        newDateSnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedNewAvailableSlots = newAvailableSlots.filterNot { slot ->
                        val slotTime = LocalTime.parse(
                            slot,
                            DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
                        )
                        (slotTime.isAfter(newTime) && slotTime.isBefore(newEndTime)) || slotTime == newTime
                    }
                    transaction.update(newDateRef, "availableSlots", updatedNewAvailableSlots)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getDoctorsFromAppointments(): Result<List<String>> {
        return try {
            val appointmentsCollection = firestore.collection("appointments")
            val snapshot = appointmentsCollection.get().await()
            val doctorNames = snapshot.documents.mapNotNull { document ->
                document.getString("doctorName")
            }
            Result.success(doctorNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPatientsFromAppointments(): Result<List<String>> {
        return try {
            val appointmentsCollection = firestore.collection("appointments")
            val snapshot = appointmentsCollection.get().await()
            val patientNames = snapshot.documents.mapNotNull { document ->
                document.getString("patientName")
            }
            Result.success(patientNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}