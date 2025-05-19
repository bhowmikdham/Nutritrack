package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.Patient

@Dao
interface PatientDAO {
    @Query("SELECT userId FROM patient")
    suspend fun getAllUserIds(): List<String>

    @Query("SELECT * FROM patient WHERE userId = :id")
    suspend fun getById(id: String): Patient?

    @Query("SELECT * FROM patient WHERE phoneNumber = :phone")
    suspend fun getByPhoneNumber(phone: String): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)
}