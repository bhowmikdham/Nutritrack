package com.fit2081.nutritrack.data.DAO

import androidx.room.*
import com.fit2081.nutritrack.data.Entity.CoachTips
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachtipsDAO {
    @Insert
    suspend fun insert(tip: CoachTips)

    @Query("SELECT * FROM coach_tips WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTipsForUser(userId: String): Flow<List<CoachTips>>

    @Query("DELETE FROM coach_tips WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}