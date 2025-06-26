package com.example.ladycure.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository responsible for user-related operations in Firestore,
 * such as updating user profiles, fetching user data, and managing user fields.
 */
class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Updates the profile picture URL of the current user.
     *
     * @param imageUrl The new profile picture URL.
     * @return [Result] of the operation (success or failure).
     */
    suspend fun updateProfilePicture(imageUrl: String): Result<Unit> {
        return try {
            val currentUser =
                auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            firestore.collection("users").document(currentUser.uid)
                .update("profilePictureUrl", imageUrl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates fields of a user document by ID.
     *
     * @param userId The ID of the user document to update.
     * @param updatedData A map of fields and values to update.
     * @return [Result] indicating success or failure.
     */
    suspend fun updateUser(userId: String, updatedData: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user data", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a predefined set of fields from the user document, then updates it with new data.
     *
     * @param userId The ID of the user document to update.
     * @param updatedData The new data to set after deleting specific fields.
     * @return [Result] of the update operation.
     */
    suspend fun docToUserUpdate(
        userId: String,
        updatedData: Map<String, Any>
    ): Result<Unit> {
        return try {

            val documentRef = firestore.collection("users").document(userId)

            val fieldsToDelete = hashMapOf<String, Any>(
                "address" to FieldValue.delete(),
                "consultationPrice" to FieldValue.delete(),
                "experience" to FieldValue.delete(),
                "languages" to FieldValue.delete(),
                "phone" to FieldValue.delete(),
                "speciality" to FieldValue.delete(),
                "city" to FieldValue.delete(),
                "bio" to FieldValue.delete(),
                "rating" to FieldValue.delete(),
                "speciality" to FieldValue.delete(),
            )

            firestore.runTransaction { transaction ->
                transaction.get(documentRef)

                transaction.update(documentRef, fieldsToDelete)

                transaction.update(documentRef, updatedData)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error updating user data", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches all user documents from Firestore.
     *
     * @return [Result] containing a list of user data maps.
     */
    suspend fun getUsers(): Result<List<Map<String, Any>>> {
        return try {
            val querySnapshot = firestore.collection("users").get().await()
            val users = querySnapshot.documents.map { it.data?.plus("id" to it.id) ?: emptyMap() }
            Result.success(users)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching users", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves the role of the current authenticated user.
     *
     * @return [Result] with the role string or null, or an error.
     */
    suspend fun getUserRole(): Result<String?> = try {
        val user = auth.currentUser
        if (user != null) {
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.getString("role"))
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } else {
            Result.failure(Exception("User not logged in"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Failed to fetch user data: ${e.message}"))
    }

    /**
     * Retrieves the full data of the current authenticated user.
     *
     * @return [Result] with the user data map or null, or an error.
     */
    suspend fun getCurrentUserData(): Result<Map<String, Any>?> {
        val user = auth.currentUser
        user?.let {
            return try {
                val documentSnapshot = firestore.collection("users").document(it.uid).get().await()
                val data = documentSnapshot.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("User document does not exist"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch user data: ${e.message}"))
            }
        } ?: return Result.failure(Exception("User not logged in"))
    }

    /**
     * Gets the current user's document from Firestore.
     *
     * @return [Result] with user data map or null, or an error.
     */
    suspend fun getCurrentUser(): Result<Map<String, Any>?> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    /**
     * Updates selected user fields and returns the updated document.
     *
     * @param updatedData Map of user fields to update: name, surname, dob, phone.
     * @return [Result] containing the updated document or an error.
     */
    suspend fun updateUserData(updatedData: Map<String, String>): Result<Map<String, Any>?> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

            val updateMap = mutableMapOf<String, Any>()
            updatedData["name"]?.let { updateMap["name"] = it }
            updatedData["surname"]?.let { updateMap["surname"] = it }
            updatedData["dob"]?.let { updateMap["dob"] = it }
            updatedData["phone"]?.let { updateMap["phone"] = it }

            try {
                firestore.collection("users").document(user.uid)
                    .update(updateMap)
                    .await()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error updating user data", e)
                return Result.failure(e)
            }

            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.data)
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a single string field from the current user's Firestore document.
     *
     * @param fieldName The name of the field to fetch.
     * @return [Result] containing the field value or an error.
     */
    suspend fun getUserField(fieldName: String): Result<String?> {
        return try {
            val currentUser =
                auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            val document = firestore.collection("users").document(currentUser.uid).get().await()
            Result.success(document.getString(fieldName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}