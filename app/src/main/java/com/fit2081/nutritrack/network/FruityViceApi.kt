package com.fit2081.nutritrack.network

import com.fit2081.nutritrack.data.model.Fruit
import retrofit2.http.GET
import retrofit2.http.Path

interface FruityViceApi {
    @GET("api/fruit/{name}")
    suspend fun getFruitByName(@Path("name") name: String): Fruit

    @GET("api/fruit/all")
    suspend fun getAllFruits(): List<Fruit>
}