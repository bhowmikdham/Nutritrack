package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.PatientHealthRecordsDAO
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import kotlinx.coroutines.flow.*

class HealthRecordsRepository(
    private val dao: PatientHealthRecordsDAO
) {
    /** Stream the full record for the given userId (null if missing) */
    fun recordFor(userId: String): Flow<List<PatientHealthRecords?>> =
        dao.getByUserId(userId)

    /** Stream all records */
    fun allRecords(): Flow<List<PatientHealthRecords?>> =
        dao.getAllRecords()

}

