package com.fit2081.nutritrack.data.Repo

import com.fit2081.nutritrack.data.DAO.CoachtipsDAO
import com.fit2081.nutritrack.data.DAO.FoodIntakeDAO
import com.fit2081.nutritrack.data.DAO.PatientHealthRecordsDAO
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.model.Fruit
import com.fit2081.nutritrack.service.GenAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
//Credit : Help taken from GenAI to implement this
// Fruit API Service
interface FruitApiService {
    @GET("fruit/{name}")
    suspend fun getFruitInfo(@Path("name") name: String): Fruit
}

class CoachTipsRepository(
    private val coachTipsDao:CoachtipsDAO,
    private val healthRecordsDao: PatientHealthRecordsDAO,
    private val foodIntakeDao: FoodIntakeDAO
) {
    private val genAIService = GenAIService()

    private val fruitApi: FruitApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fruityvice.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FruitApiService::class.java)
    }

    fun getTipsByPatient(userId: String): Flow<List<CoachTips>> {
        return coachTipsDao.getTipsForUser(userId)
    }

    suspend fun getFruitInfo(fruitName: String): Result<Fruit> = withContext(Dispatchers.IO) {
        try {
            val fruit = fruitApi.getFruitInfo(fruitName.lowercase().trim())
            Result.success(fruit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateComprehensiveMotivationalTip(
        userId: String,
        targetArea: String = "general"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get health records
            val healthRecord = healthRecordsDao.getByUserId(userId).first().firstOrNull()
                ?: return@withContext Result.failure(Exception("No health record found"))

            // Get food intake preferences
            val foodIntake = foodIntakeDao.getResponse(userId).first()

            // Generate comprehensive tip using the enhanced prompt
            val result = genAIService.generateComprehensiveDietaryTip(
                healthRecord = healthRecord,
                foodIntake = foodIntake,
                targetArea = targetArea
            )

            result.fold(
                onSuccess = { tip ->
                    // Save the tip to database
                    val coachTip = CoachTips(
                        userId = userId,
                        tip = tip,
                        timestamp = System.currentTimeMillis()
                    )
                    coachTipsDao.insert(coachTip)
                    Result.success(tip)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Legacy method for backward compatibility
    suspend fun generateMotivationalTip(
        userId: String,
        userScore: Int,
        patientData: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = genAIService.generateFruitMotivationalTip(userScore, patientData)

            result.fold(
                onSuccess = { tip ->
                    val coachTip = CoachTips(
                        userId = userId,
                        tip = tip,
                        timestamp = System.currentTimeMillis()
                    )
                    coachTipsDao.insert(coachTip)
                    Result.success(tip)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}