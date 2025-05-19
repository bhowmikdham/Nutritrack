package com.fit2081.nutritrack.data.Entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutri_coach_tip")
data class CoachTips(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: String,
    val tip: String,
    val createdAt: Long = System.currentTimeMillis()
)