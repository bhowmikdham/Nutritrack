package com.fit2081.nutritrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fit2081.nutritrack.data.Entity.Patient
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Entity.Foodintake
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.DAO.PatientHealthRecordsDAO
import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.DAO.CoachtipsDAO

/**
 * The Room database for NutriTrack, holding Patient, PatientHealthRecords,
 * FoodIntake, and CoachTips entities.
 */
@Database(
    entities = [
        Patient::class,
        PatientHealthRecords::class,
        Foodintake::class,
        CoachTips::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provides access to Patient table operations */
    abstract fun patientDAO(): PatientDAO

    /** Provides access to PatientHealthRecords table operations */
    abstract fun patientHealthRecordsDAO(): PatientHealthRecordsDAO

    /** Provides access to FoodIntake table operations */
    abstract fun foodIntakeDAO(): FoodIntakeDAO

    /** Provides access to CoachTips table operations */
    abstract fun coachTipsDAO(): CoachtipsDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retrieves the singleton instance of AppDatabase.
         * Creates it if not already created.
         * @param context Application context
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutritrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
