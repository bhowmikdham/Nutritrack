package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.Entity.Foodintake
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for reading and writing consolidated questionnaire responses.
 */
@Singleton
class IntakeRepository @Inject constructor(
    private val dao: FoodIntakeDAO
) {
    /**
     * Stream of the saved response for a patient, or null if none exists.
     */
    fun getResponse(patientId: String): Flow<Foodintake?> =
        dao.getResponse(patientId)

    /**
     * Inserts or updates the questionnaire response for the patient.
     */
    suspend fun upsertResponse(response: Foodintake) {
        dao.upsert(response)
    }

    /**
     * Deletes the stored response for the patient.
     */
    suspend fun deleteResponse(patientId: String) {
        dao.deleteResponse(patientId)
    }
}
