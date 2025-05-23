package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.ui.theme.NutritrackTheme

/**
 * Activity hosting the registration screen.
 */
class RegisterActivity : ComponentActivity() {
    private val authRepo by lazy {
        AuthRepository(AppDatabase.getDatabase(this).patientDAO())
    }
    private val vm: RegisterViewModel by viewModels {
        RegisterViewModelFactory(authRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                RegisterScreen(
                    vm = vm,
                    onRegistered = { userId ->
                        // After successful registration, navigate to Login
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    vm: RegisterViewModel,
    onRegistered: (String) -> Unit,
    onBack: () -> Unit
) {
    // 1) ViewModel state
    val userIds            by vm.userIds.collectAsState()
    val isLoading          by vm.isLoading.collectAsState()
    val registerSuccess    by vm.registerSuccess.collectAsState()
    val errorMessage       by vm.errorMessage.collectAsState()

    // 2) Local form state
    var dropdownExpanded      by rememberSaveable { mutableStateOf(false) }
    var selectedId            by rememberSaveable { mutableStateOf("") }
    var phoneText             by rememberSaveable { mutableStateOf("") }
    var nameText              by rememberSaveable { mutableStateOf("") }
    var passwordText          by rememberSaveable { mutableStateOf("") }
    var confirmPasswordText   by rememberSaveable { mutableStateOf("") }
    var showPassword          by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword   by rememberSaveable { mutableStateOf(false) }

    // 3) React to success
    LaunchedEffect(registerSuccess) {
        if (registerSuccess == true) {
            onRegistered(selectedId)
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
                contentDescription = "Onboarding image",
                modifier = Modifier
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
        }

        // White card with rounded top corners
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Register & Join the Community",
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
                    label = { Text("My ID (Provided by Clinician)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    userIds.forEach { id ->
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
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Phone field
            OutlinedTextField(
                value = phoneText,
                onValueChange = { phoneText = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Name field
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = { Text("Password") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(16.dp))

            // Confirm password
            OutlinedTextField(
                value = confirmPasswordText,
                onValueChange = { confirmPasswordText = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(24.dp))

            // Register button
            Button(
                onClick = {
                    vm.register(
                        selectedId,
                        phoneText,
                        nameText,
                        passwordText,
                        confirmPasswordText
                    )
                },
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
                    Text("Register", color = Color.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Back to login
            TextButton(onClick = onBack) {
                Text("Back to Login", fontSize = 14.sp)
            }
        }
    }
}

/**
 * Factory for RegisterViewModel DI.
 */
class RegisterViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
