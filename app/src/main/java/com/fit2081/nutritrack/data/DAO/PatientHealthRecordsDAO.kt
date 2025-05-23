package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import kotlinx.coroutines.flow.Flow

/**
 * DAO for inserting or querying detailed patient health records.
 */
@Dao
interface PatientHealthRecordsDAO {

    @Query("SELECT * FROM patient_health_record WHERE userId = :id")
    fun getByUserId(id: String): Flow<PatientHealthRecords?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PatientHealthRecords)
}
