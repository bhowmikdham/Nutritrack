package com.fit2081.nutritrack.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.DAO.CoachtipsDAO
import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.Entity.Foodintake
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.Entity.Patient

/**
 * Main Room database for NutriTrack.
 * Provides DAOs for all local entities and a singleton access point.
 */
@Database(
    entities = [
        Patient::class,
        Foodintake::class,
        CoachTips::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDAO(): PatientDAO
    abstract fun intakeDAO(): FoodIntakeDAO
    abstract fun tipDAO(): CoachtipsDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton AppDatabase instance, creating it if necessary.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutritrack_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
