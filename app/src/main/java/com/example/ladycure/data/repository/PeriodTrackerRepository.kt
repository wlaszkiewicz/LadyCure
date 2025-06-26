package com.example.ladycure.data.repository

import android.util.Log
import com.example.ladycure.presentation.home.DailyPeriodData
import com.example.ladycure.presentation.home.PeriodTrackerSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository class for handling period tracking data in Firestore.
 * This class provides methods to save and retrieve daily period data and user settings.
 */
class PeriodTrackerRepository {

    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()

    private fun getUserPeriodTrackerCollection() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("periodTracker")

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Saves or updates daily period data for a specific date.
     * The date of the daily data will be used as the document ID.
     *
     * @param dailyData The [DailyPeriodData] object to save.
     * @return A [Result] indicating success or failure.
     */
    suspend fun saveDailyPeriodData(dailyData: DailyPeriodData): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val docRef = getUserPeriodTrackerCollection()
                .document("dailyData")
                .collection("entries")
                .document(dailyData.date.format(dateFormatter))

            val dataToSave = hashMapOf(
                "isPeriodDay" to dailyData.isPeriodDay,
                "notes" to dailyData.notes,
                "moodEmoji" to dailyData.moodEmoji,
                "flowIntensity" to dailyData.flowIntensity,
                "symptoms" to dailyData.symptoms
            )

            docRef.set(dataToSave, SetOptions.merge()).await()
            Log.d("PeriodTrackerRepository", "Daily period data saved for ${dailyData.date}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error saving daily period data", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves daily period data for a specific date.
     *
     * @param date The [LocalDate] for which to retrieve data.
     * @return A [Result] containing the [DailyPeriodData] if successful, or an error.
     */
    suspend fun getDailyPeriodData(date: LocalDate): Result<DailyPeriodData> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val docRef = getUserPeriodTrackerCollection()
                .document("dailyData")
                .collection("entries")
                .document(date.format(dateFormatter))

            val documentSnapshot = docRef.get().await()

            if (documentSnapshot.exists()) {
                val isPeriodDay = documentSnapshot.getBoolean("isPeriodDay") ?: false
                val notes = documentSnapshot.getString("notes") ?: ""
                val moodEmoji = documentSnapshot.getString("moodEmoji")
                val flowIntensity =
                    documentSnapshot.getString("flowIntensity")

                @Suppress("UNCHECKED_CAST")
                val symptoms = documentSnapshot.get("symptoms") as? List<String>
                    ?: emptyList()

                val dailyData =
                    DailyPeriodData(date, isPeriodDay, notes, moodEmoji, flowIntensity, symptoms)
                Log.d("PeriodTrackerRepository", "Daily period data fetched for $date: $dailyData")
                Result.success(dailyData)
            } else {
                Log.d("PeriodTrackerRepository", "No daily period data found for $date")
                Result.failure(Exception("No data found for this date"))
            }
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error fetching daily period data for $date", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves all daily period data entries for a given month.
     *
     * @param month A [LocalDate] representing any day within the target month.
     * @return A [Result] containing a map of [LocalDate] to [DailyPeriodData] if successful, or an error.
     */
    suspend fun getDailyPeriodDataForMonth(month: LocalDate): Result<Map<LocalDate, DailyPeriodData>> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val startOfMonth = month.withDayOfMonth(1)
            val endOfMonth = month.withDayOfMonth(month.lengthOfMonth())

            val querySnapshot = getUserPeriodTrackerCollection()
                .document("dailyData")
                .collection("entries")
                .get()
                .await()

            val dailyDataMap = mutableMapOf<LocalDate, DailyPeriodData>()
            for (document in querySnapshot.documents) {
                try {
                    val date = LocalDate.parse(document.id, dateFormatter)
                    if (!date.isBefore(startOfMonth) && !date.isAfter(endOfMonth)) {
                        val isPeriodDay = document.getBoolean("isPeriodDay") ?: false
                        val notes = document.getString("notes") ?: ""
                        val moodEmoji = document.getString("moodEmoji")
                        val flowIntensity =
                            document.getString("flowIntensity")

                        @Suppress("UNCHECKED_CAST")
                        val symptoms = document.get("symptoms") as? List<String>
                            ?: emptyList()
                        dailyDataMap[date] = DailyPeriodData(
                            date,
                            isPeriodDay,
                            notes,
                            moodEmoji,
                            flowIntensity,
                            symptoms
                        )
                    }
                } catch (e: Exception) {
                    Log.w(
                        "PeriodTrackerRepository",
                        "Error parsing daily data document ID: ${document.id}",
                        e
                    )
                }
            }
            Log.d(
                "PeriodTrackerRepository",
                "Fetched daily period data for month ${month.month}: ${dailyDataMap.size} entries"
            )
            Result.success(dailyDataMap)
        } catch (e: Exception) {
            Log.e(
                "PeriodTrackerRepository",
                "Error fetching daily period data for month ${month.month}",
                e
            )
            Result.failure(e)
        }
    }


    /**
     * Saves or updates the user's period tracker settings.
     * This will be stored in a single document named "settings".
     *
     * @param settings The [PeriodTrackerSettings] object to save.
     * @return A [Result] indicating success or failure.
     */
    suspend fun savePeriodTrackerSettings(settings: PeriodTrackerSettings): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val docRef = getUserPeriodTrackerCollection().document("settings")

            val dataToSave = hashMapOf(
                "averagePeriodLength" to settings.averagePeriodLength,
                "averageCycleLength" to settings.averageCycleLength,
                "lastPeriodStartDate" to settings.lastPeriodStartDate?.format(dateFormatter)
            )

            docRef.set(dataToSave).await()
            Log.d("PeriodTrackerRepository", "Period tracker settings saved.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error saving period tracker settings", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves the user's period tracker settings.
     *
     * @return A [Result] containing the [PeriodTrackerSettings] if successful, or an error.
     */
    suspend fun getPeriodTrackerSettings(): Result<PeriodTrackerSettings> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val docRef = getUserPeriodTrackerCollection().document("settings")
            val documentSnapshot = docRef.get().await()

            if (documentSnapshot.exists()) {
                val averagePeriodLength =
                    documentSnapshot.getLong("averagePeriodLength")?.toInt() ?: 5
                val averageCycleLength =
                    documentSnapshot.getLong("averageCycleLength")?.toInt() ?: 28
                val lastPeriodStartDateString = documentSnapshot.getString("lastPeriodStartDate")
                val lastPeriodStartDate =
                    lastPeriodStartDateString?.let { LocalDate.parse(it, dateFormatter) }

                val settings = PeriodTrackerSettings(
                    averagePeriodLength,
                    averageCycleLength,
                    lastPeriodStartDate
                )
                Log.d("PeriodTrackerRepository", "Period tracker settings fetched: $settings")
                Result.success(settings)
            } else {
                Log.d(
                    "PeriodTrackerRepository",
                    "No period tracker settings found, returning default."
                )
                Result.success(PeriodTrackerSettings())
            }
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error fetching period tracker settings", e)
            Result.failure(e)
        }
    }
}
