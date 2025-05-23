package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fit2081.nutritrack.navigation.Screen

class Login : ComponentActivity() {
    private val authRepo by lazy {
        AuthRepository(AppDatabase.getDatabase(this).patientDAO())
    }
    private val vm: AuthViewModel by viewModels { AuthViewModelFactory(authRepo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                LoginScreen(
                    vm = vm,
                    onNavigate = { userId ->
                        val prefs = getSharedPreferences("prefs_$userId", MODE_PRIVATE)
                        val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                        val destClass = if (completed) Screen.Dashboard::class.java else Questionnaire::class.java
                        startActivity(Intent(this, destClass))
                        finish()
                    },
                    onRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onNavigate: (String) -> Unit,
    onRegister: () -> Unit
) {
    // 1) ViewModel state
    val userIds               by vm.userIds.collectAsState()
    val isLoading             by vm.isLoading.collectAsState()
    val loginSuccess          by vm.loginSuccess.collectAsState()
    val errorMessage          by vm.errorMessage.collectAsState()
    val notRegistered         by vm.isNotRegistered.collectAsState()

    // 2) Local, rotation-safe form state
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedId       by rememberSaveable { mutableStateOf("") }
    var passwordText     by rememberSaveable { mutableStateOf("") }
    var showPassword     by rememberSaveable { mutableStateOf(false) }

    // 3) Navigate on success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess == true) {
            onNavigate(selectedId)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    ) {
        // Top green header with image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color(0xFFC1FF72))
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding),
                contentDescription = "Main logo",
                modifier = Modifier
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
        }

        // White card with rounded top corners
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 380.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Log in",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(24.dp))

            // User ID dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedId,
                    onValueChange = { selectedId = it },
                    label = { Text("My ID (Provided by your Clinician)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null && !notRegistered,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    userIds
                        .forEach { id ->
                            DropdownMenuItem(
                                text = { Text(id) },
                                onClick = {
                                    selectedId = id
                                    dropdownExpanded = false
                                }
                            )
                        }
                }
            }
            if (errorMessage != null && !notRegistered) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            if (notRegistered) {
                TextButton(onClick = onRegister) {
                    Text("Please Complete Self Registration", fontSize = 12.sp ,color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = { Text("Password") },
                visualTransformation = if (showPassword)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = errorMessage != null && !notRegistered,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(24.dp))

            // Continue button
            Button(
                onClick = { vm.validateAndLogin(selectedId, passwordText) },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Continue", color = Color.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Haven't Register Already?",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )

            // Register button
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F5F5))
            ) {
                Text("Register", color = Color.Black)
            }
        }
    }
}

// ViewModel factory for DI
class AuthViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
