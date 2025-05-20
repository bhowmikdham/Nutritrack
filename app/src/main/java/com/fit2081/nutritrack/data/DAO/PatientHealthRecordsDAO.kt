package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords

/**
 * DAO for inserting or querying detailed patient health records.
 */
@Dao
interface PatientHealthRecordsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PatientHealthRecords)
    // TODO: add query methods as needed, e.g. getByUserId(), getAll(), etc.
}
