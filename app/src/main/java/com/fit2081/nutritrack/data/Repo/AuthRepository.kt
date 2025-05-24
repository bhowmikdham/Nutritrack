package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.Entity.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class UserProfile(
    val userId: String,
    val name: String,
    val phoneNumber: String
)

class AuthRepository(
    private val patientDao: PatientDAO
) {
    // Flow for reactive user IDs - following your pattern
    val allUserIds: Flow<List<String>> = flow {
        emit(patientDao.getAllUserIds())
    }

    /**
     * Login with userId, phone, and password validation
     */
    suspend fun login(userId: String, phone: String, password: String): Boolean {
        val patient: Patient? = patientDao.getById(userId)
        return (patient?.phoneNumber == phone && patient.password == password)
    }

    /**
     * Check if user ID exists in database (uses your existsByUserId method)
     */
    suspend fun isUserRegistered(userId: String): Boolean =
        patientDao.existsByUserId(userId)

    /**
     * Check if phone number exists in database (uses your existsByPhone method)
     */
    suspend fun isPhoneRegistered(phone: String): Boolean =
        patientDao.existsByPhone(phone)

    /**
     * Check if user has completed self-registration (has name & password)
     */
    suspend fun isSelfRegistered(userId: String): Boolean {
        val patient = patientDao.getById(userId)
        return patient?.let { it.name.isNotBlank() && it.password.isNotBlank() } ?: false
    }

    /**
     * Get patient by password and user ID for login validation
     * (uses your getByPasswordAndUserID method)
     */
    suspend fun getByPasswordAndUserID(userId: String, password: String): Patient? {
        return patientDao.getByPasswordAndUserID(userId, password)
    }

    /**
     * Get username for display purposes (uses your getUsername method)
     */
    suspend fun getUsername(userId: String): String {
        return patientDao.getUsername(userId)
    }

    /**
     * Register a user (uses your registerUser method that returns Int)
     * Following your pattern of checking if rows affected == 1
     */
    suspend fun registerUser(
        userId: String,
        phone: String,
        name: String,
        password: String
    ): Boolean {
        val rows = patientDao.registerUser(userId, phone, name, password)
        return rows == 1
    }

    /**
     * Reset password after verifying user ID and phone number match
     * (uses your updatePasswordByUserIdAndPhone method)
     */
    suspend fun resetPassword(userId: String, phone: String, newPassword: String): Boolean {
        val rows = patientDao.updatePasswordByUserIdAndPhone(userId, phone, newPassword)
        return rows == 1
    }

    /**
     * Get user profile information - following your UserProfile data class pattern
     * (uses your getById method)
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        val patient: Patient? = patientDao.getById(userId)
        return patient?.let {
            UserProfile(
                userId = it.userId,
                name = it.name,
                phoneNumber = it.phoneNumber
            )
        }
    }

    /**
     * Verify user identity with ID and phone (for forgot password flow)
     * (uses your existsByUserIdAndPhone method)
     */
    suspend fun verifyUserIdentity(userId: String, phone: String): Boolean {
        return patientDao.existsByUserIdAndPhone(userId, phone)
    }

    /**
     * Get user by user ID and phone (uses your getByUserIdAndPhone method)
     */
    suspend fun getUserByIdAndPhone(userId: String, phone: String): Patient? {
        return patientDao.getByUserIdAndPhone(userId, phone)
    }

    /**
     * Check if user has completed food intake questionnaire
     * Placeholder - would integrate with FoodIntakeDAO when available
     */
    suspend fun hasFoodIntakeRecords(userId: String): Boolean {
        // This would typically check the food intake table
        // Return false for now as placeholder
        return false
    }
}