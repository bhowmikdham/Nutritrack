package com.fit2081.nutritrack.data.Entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient")
data class Patient(
    @PrimaryKey val userId: String,
    val phoneNumber: String,
    val password: String,           // new field for login security (hashed)
    val name: String,
    val sex: String,
    val heifaScore: Int
)