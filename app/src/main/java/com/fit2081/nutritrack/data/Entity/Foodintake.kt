package com.fit2081.nutritrack.data.Entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_intake")
data class Foodintake(
    @PrimaryKey val patientId: String,
    val fruits: Boolean,
    val vegetables: Boolean,
    val grains: Boolean,
    val redMeat: Boolean,
    val seafood: Boolean,
    val poultry: Boolean,
    val fish: Boolean,
    val eggs: Boolean,
    val nutsSeeds: Boolean,
    val persona: String,
    val biggestMealTime: String,
    val sleepTime: String,
    val wakeTime: String,
    val timestamp: Long
)