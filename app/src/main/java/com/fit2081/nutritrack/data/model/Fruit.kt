package com.fit2081.nutritrack.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Fruit(
    @SerializedName("name") val name: String,
    @SerializedName("id") val id: Int,
    @SerializedName("family") val family: String,
    @SerializedName("order") val order: String,
    @SerializedName("genus") val genus: String,
    @SerializedName("nutritions") val nutritions: Nutritions
) : Serializable

data class Nutritions(
    @SerializedName("calories") val calories: Double,
    @SerializedName("fat") val fat: Double,
    @SerializedName("sugar") val sugar: Double,
    @SerializedName("carbohydrates") val carbohydrates: Double,
    @SerializedName("protein") val protein: Double
) : Serializable