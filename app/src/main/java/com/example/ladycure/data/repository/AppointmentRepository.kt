package com.example.ladycure.data.repository

import com.example.ladycure.domain.model.Appointment
import com.example.ladycure.domain.model.AppointmentSummary
import com.example.ladycure.presentation.chat.ChatParticipantInfo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

/**
 * Repository class responsible for managing appointment-related operations,
 * including booking, updating, canceling, and retrieving appointments.
 *
 * Uses Firebase Firestore for data persistence and FirebaseAuth for authentication.
 */
class AppointmentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Books a new appointment and updates the doctor's availability.
     *
     * @param appointment The appointment to book.
     * @return [Result] with the appointment ID if successful, or an error.
     */
    suspend fun bookAppointment(
        appointment: Appointment
    ): Result<String> {
        try {
            val appointmentData =
                appointment.toMap(appointment).filterKeys { it != "appointmentId" }

            val documentReference =
                firestore.collection("appointments").add(appointmentData).await()
            val appointmentId = documentReference.id


            val doctorId = appointment.doctorId
            val startTime = appointment.time

            val date = appointment.date
            val endTime =
                startTime.plus(appointment.type.durationInMinutes.toLong(), ChronoUnit.MINUTES)


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
                        DateTimeFormatter.ofPattern("h:mm a", Locale.US)
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

    /**
     * Retrieves an appointment by its ID.
     *
     * @param id The appointment ID.
     * @return [Result] with the appointment data or an error.
     */
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

    /**
     * Retrieves all appointments for the current user based on their role.
     *
     * @param role The role of the user, either "doctor" or "patient".
     * @return [Result] with a list of [Appointment] or an error.
     */
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

    /**
     * Updates the status of a specific appointment.
     *
     * @param appointmentId The ID of the appointment.
     * @param status The new status.
     * @return [Result] of the operation.
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the comment field of a specific appointment.
     *
     * @param appointmentId The ID of the appointment.
     * @param comment The comment text.
     * @return [Result] of the operation.
     */
    suspend fun updateAppointmentComment(appointmentId: String, comment: String): Result<Unit> {
        return try {
            val appointmentRef = firestore.collection("appointments").document(appointmentId)
            appointmentRef.update("comments", comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Cancels an appointment and restores the doctor's availability.
     *
     * @param appointmentId The ID of the appointment to cancel.
     * @return [Result] of the operation.
     */
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
                val startTime = appointment.time

                val date = appointment.date
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
                                Locale.US
                            )
                        )
                    }).distinct().sortedBy {
                        LocalTime.parse(
                            it,
                            DateTimeFormatter.ofPattern("h:mm a", Locale.US)
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


    /**
     * Reschedules an appointment by updating its time and date.
     * Also adjusts doctor availability accordingly.
     *
     * @param appointmentId The ID of the appointment to reschedule.
     * @param newTime The new time for the appointment.
     * @param newDate The new date for the appointment.
     * @return [Result] of the operation.
     */
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


            val updatedAppointmentData = mapOf(
                "dateTime" to Timestamp(
                    Date.from(
                        LocalDateTime.of(newDate, newTime)
                            .atZone(ZoneId.systemDefault()).toInstant()
                    )
                )
            )

            appointmentRef.update(updatedAppointmentData).await()

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
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", Locale.US))
                } ?: LocalTime.MIN
                val doctorEndTime = oldDateSnapshot.getString("endTime")?.let {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", Locale.US))
                } ?: LocalTime.MAX

                val filteredSlots = againAvailableSlots.filter { slot ->
                    (slot.isAfter(doctorStartTime) && slot.isBefore(doctorEndTime)) || slot == doctorStartTime
                }

                val currentAvailableSlots =
                    oldDateSnapshot.get("availableSlots") as? List<String> ?: emptyList()

                var updatedAvailableSlots = (currentAvailableSlots + filteredSlots.map {
                    it.format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
                }).distinct()

                if (oldDate == newDate) {
                    updatedAvailableSlots = updatedAvailableSlots.filterNot { slot ->
                        val slotTime = LocalTime.parse(
                            slot,
                            DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                        )
                        (slotTime.isAfter(newTime) && slotTime.isBefore(newEndTime)) || slotTime == newTime
                    }
                }

                updatedAvailableSlots = updatedAvailableSlots.sortedBy {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("h:mm a", Locale.US))
                }

                transaction.update(oldDateRef, "availableSlots", updatedAvailableSlots)

                if (oldDate != newDate) {
                    val newAvailableSlots =
                        newDateSnapshot.get("availableSlots") as? List<String> ?: emptyList()
                    val updatedNewAvailableSlots = newAvailableSlots.filterNot { slot ->
                        val slotTime = LocalTime.parse(
                            slot,
                            DateTimeFormatter.ofPattern("h:mm a", Locale.US)
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

    /**
     * Retrieves a list of patients with whom the current doctor has appointments.
     *
     * @return [Result] with a list of [ChatParticipantInfo] or an error.
     */
    suspend fun getPatientsFromAppointmentsWithUids(): Result<List<ChatParticipantInfo>> {
        return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val querySnapshot = firestore.collection("appointments")
                .whereEqualTo("doctorId", currentUserId)
                .get()
                .await()

            val patientUids =
                querySnapshot.documents.mapNotNull { it.getString("patientId") }.distinct()
            val patientsInfo = mutableListOf<ChatParticipantInfo>()

            for (uid in patientUids) {
                val userDoc = firestore.collection("users").document(uid).get().await()
                val name = userDoc.getString("name") ?: ""
                val surname = userDoc.getString("surname") ?: ""

                if (name.isNotBlank() || surname.isNotBlank()) {
                    patientsInfo.add(ChatParticipantInfo(userDoc.id, "$name $surname".trim()))
                } else if (userDoc.exists()) {
                    patientsInfo.add(ChatParticipantInfo(userDoc.id, "Unknown User"))
                }
            }
            Result.success(patientsInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a list of doctors with whom the current patient has appointments.
     *
     * @return [Result] with a list of [ChatParticipantInfo] or an error.
     */
    suspend fun getDoctorsFromAppointmentsWithUids(): Result<List<ChatParticipantInfo>> {
        return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val querySnapshot = firestore.collection("appointments")
                .whereEqualTo("patientId", currentUserId)
                .get()
                .await()

            val doctorUids =
                querySnapshot.documents.mapNotNull { it.getString("doctorId") }.distinct()
            val doctorsInfo = mutableListOf<ChatParticipantInfo>()

            for (uid in doctorUids) {
                val userDoc = firestore.collection("users").document(uid).get().await()

                val name = userDoc.getString("name") ?: ""
                val surname = userDoc.getString("surname") ?: ""

                if (name.isNotBlank() || surname.isNotBlank()) {
                    doctorsInfo.add(ChatParticipantInfo(userDoc.id, "$name $surname".trim()))
                } else if (userDoc.exists()) {
                    doctorsInfo.add(ChatParticipantInfo(userDoc.id, "Unknown Doctor"))
                }
            }
            Result.success(doctorsInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves participants with whom the user has active chat sessions,
     * including last message, unread count, and timestamps.
     *
     * @return [Result] with a list of [ChatParticipantInfo] or an error.
     */
    suspend fun getActiveChatParticipants(): Result<List<ChatParticipantInfo>> {
        return try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val snapshot = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()

            val participants = mutableListOf<ChatParticipantInfo>()

            for (chatDoc in snapshot.documents) {
                val participantsList = chatDoc.get("participants") as? List<String> ?: continue
                val otherUserId = participantsList.first { it != currentUserId }

                val userDoc = firestore.collection("users").document(otherUserId).get().await()
                val userData = userDoc.data ?: continue

                val lastMessageDoc = firestore.collection("chats")
                    .document(chatDoc.id)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                val lastMessage = lastMessageDoc?.getString("text")
                val lastMessageSenderId = lastMessageDoc?.getString("sender")

                var lastMessageSenderName: String? = null
                lastMessageSenderId?.let { senderId ->
                    if (senderId == currentUserId) {
                        lastMessageSenderName = "You"
                    } else {
                        val senderDoc =
                            firestore.collection("users").document(senderId).get().await()
                        val senderData = senderDoc.data
                        lastMessageSenderName =
                            "${senderData?.get("name")} ${senderData?.get("surname")}"
                    }
                }

                val unreadCount = firestore.collection("chats")
                    .document(chatDoc.id)
                    .collection("messages")
                    .whereEqualTo("isRead", false)
                    .whereEqualTo("sender", otherUserId)
                    .get()
                    .await()
                    .size()

                participants.add(
                    ChatParticipantInfo(
                        uid = otherUserId,
                        fullName = "${userData["name"]} ${userData["surname"]}",
                        specialty = userData["speciality"] as? String,
                        //isOnline = userData["isOnline"] as? Boolean ?: false,
                        lastSeen = userData["lastSeen"] as? Long,
                        lastMessage = lastMessage,
                        lastMessageSender = if (lastMessageSenderId == currentUserId) "Ty" else "${userData["name"]} ${userData["surname"]}",
                        lastMessageTime = lastMessageDoc?.getTimestamp("timestamp")?.seconds?.times(
                            1000
                        ),
                        unreadCount = unreadCount
                    )
                )
            }

            Result.success(participants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves appointment summaries for the current user.
     *
     * @param role The role of the user ("doctor" or "patient").
     * @return [Result] with a list of [AppointmentSummary] or an error.
     */
    suspend fun getAppointmentSummaries(role: String): Result<List<AppointmentSummary>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("appointmentSummaries")
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val summaries = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { AppointmentSummary.fromMap(it, doc.id) }
            }

            Result.success(summaries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves upcoming appointment summaries for the current user.
     *
     * @return [Result] with a list of [AppointmentSummary] or an error.
     */
    suspend fun getUpcomingAppointmentsSummaries(): Result<List<AppointmentSummary>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("appointmentSummaries")
                .document("upcoming")
                .collection("items")
                .get()
                .await()

            val summaries = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { AppointmentSummary.fromMap(it, doc.id) }
            }

            Result.success(summaries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves appointment summaries for a specific month.
     *
     * @param monthKey The Firestore document ID representing the month (e.g., "2025-06").
     * @return [Result] with a list of [AppointmentSummary] or an error.
     */
    suspend fun getMonthlyAppointmentSummaries(
        monthKey: String
    ): Result<List<AppointmentSummary>> {
        return try {
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val summariesRef = firestore.collection("users")
                .document(userId)
                .collection("appointmentSummaries")
                .document(monthKey)
                .collection("items")

            val snapshot = summariesRef.get().await()
            val summaries = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { AppointmentSummary.fromMap(it, doc.id) }
            }
            Result.success(summaries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}