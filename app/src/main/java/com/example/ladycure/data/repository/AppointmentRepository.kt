package com.example.ladycure.data.repository

import com.example.ladycure.domain.model.Appointment
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

class AppointmentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


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
            val startTime = appointment.time

            val date = appointment.date
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
                "dateTime" to Timestamp(
                    Date.from(
                        LocalDateTime.of(newDate, newTime)
                            .atZone(ZoneId.systemDefault()).toInstant()
                    )
                )
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
}