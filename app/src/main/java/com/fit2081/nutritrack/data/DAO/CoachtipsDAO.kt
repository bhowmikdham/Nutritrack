package com.fit2081.nutritrack.data.DAO
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fit2081.nutritrack.data.Entity.CoachTips
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachtipsDAO {
    @Insert
    suspend fun insertTip(tip: CoachTips)

    @Query("SELECT * FROM nutri_coach_tip WHERE patientId = :pid ORDER BY createdAt DESC")
    fun getTipsByPatient(pid: String): Flow<List<CoachTips>>
}
