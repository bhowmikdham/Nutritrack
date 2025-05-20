package com.fit2081.nutritrack.di
import android.content.Context
import androidx.room.Room
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.DAO.CoachtipsDAO
import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.DAO.PatientDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "nutritrack_db").build()

    @Provides
    fun providePatientDao(db: AppDatabase): PatientDAO = db.patientDAO()

    @Provides
    fun provideFoodIntakeDao(db: AppDatabase): FoodIntakeDAO = db.intakeDAO()

    @Provides
    fun provideCoachTipsDao(db: AppDatabase): CoachtipsDAO = db.tipDAO()
}
