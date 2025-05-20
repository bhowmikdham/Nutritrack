package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.Foodintake
import kotlinx.coroutines.flow.Flow

/**
 * DAO for consolidated questionnaire responses stored in food_intake table.
 */
@Dao
interface FoodIntakeDAO {
    /**
     * Inserts or updates the full set of questionnaire fields for a patient.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(response: Foodintake)

    /**
     * Streams the saved response for the given patient, or null if none.
     */
    @Query("SELECT * FROM food_intake WHERE patientId = :patientId")
    fun getResponse(patientId: String): Flow<Foodintake?>

    /**
     * Deletes the stored response for the given patient.
     */
    @Query("DELETE FROM food_intake WHERE patientId = :patientId")
    suspend fun deleteResponse(patientId: String)
}