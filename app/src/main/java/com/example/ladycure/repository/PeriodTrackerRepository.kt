package com.example.ladycure.repository

import android.util.Log
import com.example.ladycure.screens.user.DailyPeriodData
import com.example.ladycure.screens.user.PeriodTrackerSettings
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

    // FirebaseAuth instance to get the current user's ID
    private val auth = FirebaseAuth.getInstance()

    // FirebaseFirestore instance, initialized with the "telecure" app name
    private val firestore = FirebaseFirestore.getInstance("telecure")

    // The base collection path for user-specific period tracker data
    private fun getUserPeriodTrackerCollection() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("periodTracker")

    // Date formatter for consistent date string representation in Firestore
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

            // Reference to the specific daily data document using the date as ID
            val docRef = getUserPeriodTrackerCollection()
                .document("dailyData") // Subcollection for daily entries
                .collection("entries") // Further subcollection for each day's entry
                .document(dailyData.date.format(dateFormatter))

            // Convert DailyPeriodData object to a HashMap for Firestore
            val dataToSave = hashMapOf(
                "isPeriodDay" to dailyData.isPeriodDay,
                "notes" to dailyData.notes,
                "moodEmoji" to dailyData.moodEmoji,
                "flowIntensity" to dailyData.flowIntensity, // Add flowIntensity
                "symptoms" to dailyData.symptoms // Add symptoms
            )

            // Use SetOptions.merge() to update existing fields or create the document if it doesn't exist
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
                // Convert Firestore document data back to DailyPeriodData object
                val isPeriodDay = documentSnapshot.getBoolean("isPeriodDay") ?: false
                val notes = documentSnapshot.getString("notes") ?: ""
                val moodEmoji = documentSnapshot.getString("moodEmoji")
                val flowIntensity = documentSnapshot.getString("flowIntensity") // Retrieve flowIntensity
                @Suppress("UNCHECKED_CAST") // Firestore returns List<Any> for arrays, cast as String
                val symptoms = documentSnapshot.get("symptoms") as? List<String> ?: emptyList() // Retrieve symptoms

                val dailyData = DailyPeriodData(date, isPeriodDay, notes, moodEmoji, flowIntensity, symptoms)
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

            // Get all documents in the 'entries' collection for the current user
            val querySnapshot = getUserPeriodTrackerCollection()
                .document("dailyData")
                .collection("entries")
                .get()
                .await()

            val dailyDataMap = mutableMapOf<LocalDate, DailyPeriodData>()
            for (document in querySnapshot.documents) {
                try {
                    val date = LocalDate.parse(document.id, dateFormatter)
                    // Filter documents to include only those within the requested month
                    if (!date.isBefore(startOfMonth) && !date.isAfter(endOfMonth)) {
                        val isPeriodDay = document.getBoolean("isPeriodDay") ?: false
                        val notes = document.getString("notes") ?: ""
                        val moodEmoji = document.getString("moodEmoji")
                        val flowIntensity = document.getString("flowIntensity") // Retrieve flowIntensity
                        @Suppress("UNCHECKED_CAST")
                        val symptoms = document.get("symptoms") as? List<String> ?: emptyList() // Retrieve symptoms
                        dailyDataMap[date] = DailyPeriodData(date, isPeriodDay, notes, moodEmoji, flowIntensity, symptoms)
                    }
                } catch (e: Exception) {
                    Log.w("PeriodTrackerRepository", "Error parsing daily data document ID: ${document.id}", e)
                }
            }
            Log.d("PeriodTrackerRepository", "Fetched daily period data for month ${month.month}: ${dailyDataMap.size} entries")
            Result.success(dailyDataMap)
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error fetching daily period data for month ${month.month}", e)
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

            // Convert PeriodTrackerSettings object to a HashMap for Firestore
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
                // Convert Firestore document data back to PeriodTrackerSettings object
                val averagePeriodLength = documentSnapshot.getLong("averagePeriodLength")?.toInt() ?: 5
                val averageCycleLength = documentSnapshot.getLong("averageCycleLength")?.toInt() ?: 28
                val lastPeriodStartDateString = documentSnapshot.getString("lastPeriodStartDate")
                val lastPeriodStartDate = lastPeriodStartDateString?.let { LocalDate.parse(it, dateFormatter) }

                val settings = PeriodTrackerSettings(averagePeriodLength, averageCycleLength, lastPeriodStartDate)
                Log.d("PeriodTrackerRepository", "Period tracker settings fetched: $settings")
                Result.success(settings)
            } else {
                Log.d("PeriodTrackerRepository", "No period tracker settings found, returning default.")
                // Return default settings if no document exists
                Result.success(PeriodTrackerSettings())
            }
        } catch (e: Exception) {
            Log.e("PeriodTrackerRepository", "Error fetching period tracker settings", e)
            Result.failure(e)
        }
    }
}
