package com.fit2081.nutritrack.data.Repo
import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.Entity.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository for authentication operations, directly injected into AuthViewModel.
 */
class AuthRepository @Inject constructor(
    private val patientDao: PatientDAO
) {
    /**
     * Stream of all user IDs for dropdowns or auto-complete.
     */
    val allUserIds: Flow<List<String>> = flow {
        emit(patientDao.getAllUserIds())
    }

    /**
     * Validates credentials by checking stored phone and password.
     */
    suspend fun login(userId: String, phone: String, password: String): Boolean {
        val patient: Patient? = patientDao.getById(userId)
        return (patient?.phoneNumber == phone && patient.password == password)
    }

    /**
     * Checks if the given userId exists.
     */
    suspend fun isUserRegistered(userId: String): Boolean =
        patientDao.getById(userId) != null

    /**
     * Checks if the given phone number is registered.
     */
    suspend fun isPhoneRegistered(phone: String): Boolean =
        patientDao.getByPhoneNumber(phone) != null
}
