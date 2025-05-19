package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.Foodintake
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodIntakeDAO {
    @Insert
    suspend fun insert(intake: Foodintake)

    @Query("SELECT * FROM food_intake WHERE patientId = :pid ORDER BY timestamp DESC")
    fun getByPatient(pid: String): Flow<List<Foodintake>>
}
