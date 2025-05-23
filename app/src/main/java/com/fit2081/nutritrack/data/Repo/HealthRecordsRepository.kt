package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.PatientHealthRecordsDAO
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import kotlinx.coroutines.flow.*

class HealthRecordsRepository(
    private val dao: PatientHealthRecordsDAO
) {
    /** Stream the full record for the given userId (null if missing) */
    fun recordFor(userId: String): Flow<PatientHealthRecords?> =
        dao.getByUserId(userId)

    /**
     * Stream any one Float-valued score (e.g., sex not included here—just numeric fields).
     * Pass in a selector like `it::vegetablesHeifaScoreMale` or a lambda.
     */
    fun scoreFor(
        userId: String,
        selector: (PatientHealthRecords) -> Float
    ): Flow<Float?> =
        recordFor(userId)
            .map { it?.let(selector) }
}
