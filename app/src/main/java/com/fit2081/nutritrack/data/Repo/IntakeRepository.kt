package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.Entity.Foodintake
import kotlinx.coroutines.flow.Flow

class IntakeRepository(
    private val dao: FoodIntakeDAO
) {
    fun getResponse(patientId: String): Flow<Foodintake?> =
        dao.getResponse(patientId)

    suspend fun upsertResponse(response: Foodintake) {
        dao.upsert(response)
    }

    suspend fun deleteResponse(patientId: String) {
        dao.deleteResponse(patientId)
    }
}