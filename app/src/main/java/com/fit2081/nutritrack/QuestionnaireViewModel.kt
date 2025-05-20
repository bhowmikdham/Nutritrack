package com.fit2081.nutritrack.QuestionnaireViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.nutritrack.data.Entity.Foodintake
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state representing the full questionnaire response for a patient.
 */
data class FoodintakeState(
    val fruits: Boolean = false,
    val vegetables: Boolean = false,
    val grains: Boolean = false,
    val redMeat: Boolean = false,
    val seafood: Boolean = false,
    val poultry: Boolean = false,
    val fish: Boolean = false,
    val eggs: Boolean = false,
    val nutsSeeds: Boolean = false,
    val persona: String = "",
    val biggestMealTime: String = "",
    val sleepTime: String = "",
    val wakeTime: String = "",
    val timestamp: Long = 0L
)

/**
 * ViewModel for loading, saving, and deleting a patient's questionnaire response.
 */
@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val repository: IntakeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FoodintakeState())
    val state: StateFlow<FoodintakeState> = _state.asStateFlow()

    /**
     * Loads the saved response for the given patient, if any.
     */
    fun loadResponse(patientId: String) {
        viewModelScope.launch {
            repository.getResponse(patientId).collectLatest { resp ->
                if (resp != null) {
                    _state.value = FoodintakeState(
                        fruits = resp.fruits,
                        vegetables = resp.vegetables,
                        grains = resp.grains,
                        redMeat = resp.redMeat,
                        seafood = resp.seafood,
                        poultry = resp.poultry,
                        fish = resp.fish,
                        eggs = resp.eggs,
                        nutsSeeds = resp.nutsSeeds,
                        persona = resp.persona,
                        biggestMealTime = resp.biggestMealTime,
                        sleepTime = resp.sleepTime,
                        wakeTime = resp.wakeTime,
                        timestamp = resp.timestamp
                    )
                } else {
                    _state.value = FoodintakeState()
                }
            }
        }
    }

    /**
     * Saves or updates the current state as the patient's questionnaire response.
     */
    fun saveResponse(patientId: String) {
        viewModelScope.launch {
            val s = _state.value
            val now = System.currentTimeMillis()
            repository.upsertResponse(
                Foodintake(
                    patientId = patientId,
                    fruits = s.fruits,
                    vegetables = s.vegetables,
                    grains = s.grains,
                    redMeat = s.redMeat,
                    seafood = s.seafood,
                    poultry = s.poultry,
                    fish = s.fish,
                    eggs = s.eggs,
                    nutsSeeds = s.nutsSeeds,
                    persona = s.persona,
                    biggestMealTime = s.biggestMealTime,
                    sleepTime = s.sleepTime,
                    wakeTime = s.wakeTime,
                    timestamp = now
                )
            )
        }
    }

    /**
     * Deletes the stored response and resets UI state.
     */
    fun deleteResponse(patientId: String) {
        viewModelScope.launch {
            repository.deleteResponse(patientId)
            _state.value = FoodintakeState()
        }
    }
    /** Update the selected eating persona. */
    fun onPersonaChange(persona: String) = _state.update { it.copy(persona = persona) }

    /** Toggle a food category on or off. */
    fun onFoodToggle(key: String, value: Boolean) = _state.update { s ->
        when (key) {
            "fruits"       -> s.copy(fruits     = value)
            "vegetables"   -> s.copy(vegetables = value)
            "grains"       -> s.copy(grains     = value)
            "red_meat"     -> s.copy(redMeat    = value)
            "seafood"      -> s.copy(seafood   = value)
            "poultry"      -> s.copy(poultry   = value)
            "fish"         -> s.copy(fish      = value)
            "eggs"         -> s.copy(eggs      = value)
            "nuts_seeds"   -> s.copy(nutsSeeds = value)
            else           -> s
        }
    }

    /** Update one of the time fields: biggest meal, sleep, or wake. */
    fun onTimeChange(field: String, time: String) = _state.update { s ->
        when (field) {
            "biggest_meal_time" -> s.copy(biggestMealTime = time)
            "sleep_time"        -> s.copy(sleepTime        = time)
            "wake_time"         -> s.copy(wakeTime         = time)
            else                -> s
        }
    }



}