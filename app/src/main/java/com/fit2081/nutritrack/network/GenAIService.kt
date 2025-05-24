package com.fit2081.nutritrack.service

import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Entity.Foodintake
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenAIService {
    companion object {
        private const val GEMINI_API_KEY = "AIzaSyCrWOHQ1HxqjmT0MzEnigs0DUcZCKW8fG4"
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = GEMINI_API_KEY
    )

    /**
     * Generate a comprehensive dietary improvement tip based on patient's complete profile
     */
    suspend fun generateComprehensiveDietaryTip(
        healthRecord: PatientHealthRecords,
        foodIntake: Foodintake?,
        targetArea: String = "general"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildComprehensivePrompt(healthRecord, foodIntake, targetArea)
            val response = generativeModel.generateContent(prompt)
            val tip = response.text?.trim() ?: "Keep working on improving your nutrition!"
            Result.success(tip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildComprehensivePrompt(
        record: PatientHealthRecords,
        intake: Foodintake?,
        targetArea: String
    ): String {
        val sex = record.sex
        val scores = extractScores(record, sex)

        return buildString {
            append("You are a nutrition coach providing personalized dietary advice.\n\n")

            // Patient profile
            append("Patient Profile:\n")
            append("- Sex: $sex\n")
            append("- Total HEIFA Score: ${scores.total}/100\n\n")

            // Dietary preferences
            if (intake != null) {
                append("Dietary Preferences & Restrictions:\n")
                val canEat = mutableListOf<String>()
                if (intake.fruits) canEat.add("fruits")
                if (intake.vegetables) canEat.add("vegetables")
                if (intake.grains) canEat.add("grains")
                if (intake.redMeat) canEat.add("red meat")
                if (intake.seafood) canEat.add("seafood")
                if (intake.poultry) canEat.add("poultry")
                if (intake.fish) canEat.add("fish")
                if (intake.eggs) canEat.add("eggs")
                if (intake.nutsSeeds) canEat.add("nuts and seeds")
                append("- Can eat: ${canEat.joinToString(", ")}\n")
                append("- Persona: ${intake.persona}\n")
                append("- Biggest meal time: ${intake.biggestMealTime}\n")
                append("- Sleep schedule: ${intake.sleepTime} - ${intake.wakeTime}\n\n")
            }

            // Nutritional scores breakdown
            append("Nutritional Scores (out of 10 unless specified):\n")
            append("- Vegetables: ${scores.vegetables}\n")
            append("- Fruits: ${scores.fruits}\n")
            append("- Whole Grains: ${scores.wholeGrains}\n")
            append("- Dairy: ${scores.dairy}\n")
            append("- Protein: ${scores.protein}\n")
            append("- Water: ${scores.water}/5\n")
            append("- Sugar: ${scores.sugar}\n")
            append("- Saturated Fat: ${scores.saturatedFat}\n")
            append("- Sodium: ${scores.sodium}\n\n")

            // Identify areas for improvement
            val lowScores = identifyLowScores(scores)
            append("Areas needing improvement: ${lowScores.joinToString(", ")}\n\n")

            // Specific request
            when (targetArea) {
                "fruit" -> append("Focus on: Improving fruit intake (current score: ${scores.fruits}/10)\n")
                "vegetable" -> append("Focus on: Increasing vegetable consumption (current score: ${scores.vegetables}/10)\n")
                "general" -> append("Focus on: Overall dietary improvement\n")
            }

            append("\nGenerate a personalized, actionable tip (50-75 words) that:\n")
            append("1. Addresses the lowest scoring areas\n")
            append("2. Respects dietary preferences and restrictions\n")
            append("3. Considers their meal timing and lifestyle\n")
            append("4. Provides specific, practical suggestions\n")
            append("5. Is encouraging and motivating\n")
            append("6. Includes easy-to-implement changes")
        }
    }

    private fun extractScores(record: PatientHealthRecords, sex: String): NutrientScores {
        return if (sex.equals("Male", ignoreCase = true)) {
            NutrientScores(
                total = record.heifaTotalScoreMale.toInt(),
                vegetables = record.vegetablesHeifaScoreMale.toInt(),
                fruits = record.fruitHeifaScoreMale.toInt(),
                wholeGrains = record.wholeGrainsHeifaScoreMale.toInt(),
                dairy = record.dairyAndAlternativesHeifaScoreMale.toInt(),
                protein = record.meatAndAlternativesHeifaScoreMale.toInt(),
                water = record.waterHeifaScoreMale.toInt(),
                sugar = record.sugarHeifaScoreMale.toInt(),
                saturatedFat = record.saturatedFatHeifaScoreMale.toInt(),
                sodium = record.sodiumHeifaScoreMale.toInt()
            )
        } else {
            NutrientScores(
                total = record.heifaTotalScoreFemale.toInt(),
                vegetables = record.vegetablesHeifaScoreFemale.toInt(),
                fruits = record.fruitHeifaScoreFemale.toInt(),
                wholeGrains = record.wholeGrainsHeifaScoreFemale.toInt(),
                dairy = record.dairyAndAlternativesHeifaScoreFemale.toInt(),
                protein = record.meatAndAlternativesHeifaScoreFemale.toInt(),
                water = record.waterHeifaScoreFemale.toInt(),
                sugar = record.sugarHeifaScoreFemale.toInt(),
                saturatedFat = record.saturatedFatHeifaScoreFemale.toInt(),
                sodium = record.sodiumHeifaScoreFemale.toInt()
            )
        }
    }

    private fun identifyLowScores(scores: NutrientScores): List<String> {
        val areas = mutableListOf<String>()
        if (scores.vegetables < 7) areas.add("vegetables")
        if (scores.fruits < 7) areas.add("fruits")
        if (scores.wholeGrains < 7) areas.add("whole grains")
        if (scores.dairy < 7) areas.add("dairy")
        if (scores.protein < 7) areas.add("protein")
        if (scores.water < 4) areas.add("water intake")
        if (scores.sugar < 7) areas.add("sugar reduction")
        if (scores.saturatedFat < 7) areas.add("saturated fat")
        if (scores.sodium < 7) areas.add("sodium reduction")
        return areas
    }

    /**
     * Original method for backward compatibility
     */
    suspend fun generateFruitMotivationalTip(
        fruitScore: Int,
        additionalContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Generate a short, encouraging message (max 50 words) ")
                append("to help someone improve their fruit intake. ")
                append("Their current fruit score is $fruitScore out of 10. ")
                if (additionalContext.isNotEmpty()) {
                    append("Additional context: $additionalContext. ")
                }
                append("Be friendly, specific, and motivating. ")
                append("Include practical tips like specific fruits to try or easy ways to add them to meals.")
            }

            val response = generativeModel.generateContent(prompt)
            val tip = response.text?.trim() ?: "Keep up the great work with your nutrition!"
            Result.success(tip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin insights generation remains the same
    suspend fun generateDataInsights(
        maleAvgScore: Float,
        femaleAvgScore: Float,
        additionalData: String = ""
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildString {
                append("Based on the following health data:\n")
                append("- Average HEIFA score for male users: $maleAvgScore\n")
                append("- Average HEIFA score for female users: $femaleAvgScore\n")
                if (additionalData.isNotEmpty()) {
                    append("Additional data: $additionalData\n")
                }
                append("\nGenerate exactly 3 interesting insights or patterns from this data. ")
                append("Each insight should be concise (max 30 words) and actionable. ")
                append("Format: Return each insight on a new line, numbered 1., 2., 3.")
            }

            val response = generativeModel.generateContent(prompt)
            val insights = response.text
                ?.split("\n")
                ?.filter { it.trim().isNotEmpty() }
                ?.map { it.removePrefix("1.").removePrefix("2.").removePrefix("3.").trim() }
                ?: listOf(
                    "Unable to generate insights at this time",
                    "Please check your data and try again",
                    "Ensure you have sufficient data for analysis"
                )

            Result.success(insights.take(3))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Helper data class for scores
private data class NutrientScores(
    val total: Int,
    val vegetables: Int,
    val fruits: Int,
    val wholeGrains: Int,
    val dairy: Int,
    val protein: Int,
    val water: Int,
    val sugar: Int,
    val saturatedFat: Int,
    val sodium: Int
)