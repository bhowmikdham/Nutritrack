package com.fit2081.nutritrack.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.Patient

@Dao
interface PatientDAO {
    @Query("SELECT userId FROM patient order by CAST(userId AS INTEGER)")
    suspend fun getAllUserIds(): List<String>

    @Query("SELECT * FROM patient WHERE userId = :id")
    suspend fun getById(id: String): Patient?

    @Query("SELECT * FROM patient WHERE phoneNumber = :phone")
    suspend fun getByPhoneNumber(phone: String): Patient?

    @Query("SELECT * FROM patient WHERE userId = :userId AND password = :password")
    suspend fun getByPasswordAndUserID(userId: String, password: String): Patient?

    @Query("""
  UPDATE Patient
    SET name       = :name,
        password   = :password
  WHERE userId      = :userId
    AND phoneNumber = :phone
""")
    suspend fun registerUser(
        userId: String,
        phone:  String,
        name:   String,
        password: String
    ): Int

    @Query("SELECT name FROM patient WHERE userId = :userId")
    suspend fun getUsername(userId: String): String

    @Query("SELECT EXISTS(SELECT 1 FROM patient WHERE userId = :userId)")
    suspend fun existsByUserId(userId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM patient WHERE phoneNumber = :phone)")
    suspend fun existsByPhone(phone: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM patient WHERE userId = :userId AND phoneNumber = :phone)")
    suspend fun existsByUserIdAndPhone(userId: String, phone: String): Boolean

    @Query("UPDATE patient SET password = :newPassword WHERE userId = :userId AND phoneNumber = :phone")
    suspend fun updatePasswordByUserIdAndPhone(userId: String, phone: String, newPassword: String): Int

    @Query("SELECT * FROM patient WHERE userId = :userId AND phoneNumber = :phone")
    suspend fun getByUserIdAndPhone(userId: String, phone: String): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient): Long
}