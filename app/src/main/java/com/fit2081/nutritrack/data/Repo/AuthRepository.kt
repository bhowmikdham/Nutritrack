package com.fit2081.nutritrack.data.Repo
import com.fit2081.nutritrack.data.DAO.PatientDAO
import com.fit2081.nutritrack.data.Entity.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class UserProfile(
    val userId: String,
    val name: String,
    val phoneNumber: String
)

class AuthRepository(
    private val patientDao: PatientDAO
) {
    val allUserIds: Flow<List<String>> = flow {
        emit(patientDao.getAllUserIds())
    }

    suspend fun login(userId: String, phone: String, password: String): Boolean {
        val patient: Patient? = patientDao.getById(userId)
        return (patient?.phoneNumber == phone && patient.password == password)
    }

    suspend fun isUserRegistered(userId: String): Boolean =
        patientDao.getById(userId) != null

    suspend fun isPhoneRegistered(phone: String): Boolean =
        patientDao.getByPhoneNumber(phone) != null

    suspend fun isSelfRegistered(userId: String): Boolean {
        val patient = patientDao.getById(userId)
        return patient?.let { it.name.isNotBlank() && it.password.isNotBlank() } ?: false
    }

    suspend fun getByPasswordAndUserID(userId: String, password: String): Patient? {
        return patientDao.getByPasswordAndUserID(userId, password)
    }

    suspend fun getUsername(userId: String): String {
        return patientDao.getUsername(userId)
    }

    suspend fun registerUser(
        userId: String,
        phone: String,
        name: String,
        password: String
    ): Boolean {
        val rows = patientDao.registerUser(userId, phone, name, password)
        return rows == 1
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val patient: Patient? = patientDao.getById(userId)
        return patient?.let { UserProfile(it.userId, it.name, it.phoneNumber) }
    }

}
