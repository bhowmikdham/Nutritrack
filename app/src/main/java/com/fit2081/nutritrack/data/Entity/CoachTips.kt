package com.fit2081.nutritrack.data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coach_tips")
data class CoachTips(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val tip: String,
    val timestamp: Long = System.currentTimeMillis()
)