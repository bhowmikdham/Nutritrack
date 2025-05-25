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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fit2081.nutritrack.navigation.Screen

// Activity for standalone use (if needed)
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
                // Note: This uses the simple version for Activity-based navigation
                LoginScreenSimple(
                    vm = vm,
                    onNavigate = { userId ->
                        val prefs = getSharedPreferences("prefs_$userId", MODE_PRIVATE)
                        val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                        val destClass = if (completed) MainActivity::class.java else Questionnaire::class.java
                        startActivity(Intent(this, destClass))
                        finish()
                    },
                    onRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgotPassword = {
                        // For Activity-based navigation, you'd create a ForgotPasswordActivity
                        android.widget.Toast.makeText(
                            this,
                            "Use the main app for Forgot Password feature",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

// Simple version for Activity-based navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenSimple(
    vm: AuthViewModel,
    onNavigate: (String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    // ViewModel state
    val userIds by vm.userIds.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val loginSuccess by vm.loginSuccess.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val notRegistered by vm.isNotRegistered.collectAsState()

    // Form state from ViewModel
    val selectedId by vm.selectedUserId.collectAsState()
    val passwordText by vm.password.collectAsState()
    val showPassword by vm.showPassword.collectAsState()
    val dropdownExpanded by vm.dropdownExpanded.collectAsState()

    // Navigate on success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess == true) {
            onNavigate(selectedId)
        }
    }

    LoginScreenContent(
        userIds = userIds,
        isLoading = isLoading,
        errorMessage = errorMessage,
        notRegistered = notRegistered,
        dropdownExpanded = dropdownExpanded,
        selectedId = selectedId,
        passwordText = passwordText,
        showPassword = showPassword,
        onDropdownExpandedChange = vm::updateDropdownExpanded,
        onSelectedIdChange = vm::updateSelectedUserId,
        onPasswordTextChange = vm::updatePassword,
        onShowPasswordChange = vm::updateShowPassword,
        onLogin = { vm.validateAndLogin() },
        onRegister = onRegister,
        onForgotPassword = onForgotPassword
    )
}

// Navigation Compose version (Main version to use)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    navController: NavHostController,
    onNavigate: (String) -> Unit,
    onRegister: () -> Unit
) {
    // ViewModel state
    val userIds by vm.userIds.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val loginSuccess by vm.loginSuccess.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val notRegistered by vm.isNotRegistered.collectAsState()

    // Form state from ViewModel
    val selectedId by vm.selectedUserId.collectAsState()
    val passwordText by vm.password.collectAsState()
    val showPassword by vm.showPassword.collectAsState()
    val dropdownExpanded by vm.dropdownExpanded.collectAsState()

    // Navigate on success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess == true) {
            onNavigate(selectedId)
        }
    }

    LoginScreenContent(
        userIds = userIds,
        isLoading = isLoading,
        errorMessage = errorMessage,
        notRegistered = notRegistered,
        dropdownExpanded = dropdownExpanded,
        selectedId = selectedId,
        passwordText = passwordText,
        showPassword = showPassword,
        onDropdownExpandedChange = vm::updateDropdownExpanded,
        onSelectedIdChange = vm::updateSelectedUserId,
        onPasswordTextChange = vm::updatePassword,
        onShowPasswordChange = vm::updateShowPassword,
        onLogin = { vm.validateAndLogin() },
        onRegister = onRegister,
        onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
    )
}

// Shared UI content with LazyColumn for proper scrolling and rotation handling
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    userIds: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    notRegistered: Boolean,
    dropdownExpanded: Boolean,
    selectedId: String,
    passwordText: String,
    showPassword: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onSelectedIdChange: (String) -> Unit,
    onPasswordTextChange: (String) -> Unit,
    onShowPasswordChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top green header with image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .background(Color(0xFFC1FF72))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding),
                        contentDescription = "Main logo",
                        modifier = Modifier.aspectRatio(0.7f),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // White card content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-120).dp) // Overlap with the image
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
                        onExpandedChange = onDropdownExpandedChange
                    ) {
                        OutlinedTextField(
                            value = selectedId,
                            onValueChange = onSelectedIdChange,
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
                            onDismissRequest = { onDropdownExpandedChange(false) }
                        ) {
                            userIds.forEach { id ->
                                DropdownMenuItem(
                                    text = { Text(id) },
                                    onClick = {
                                        onSelectedIdChange(id)
                                        onDropdownExpandedChange(false)
                                    }
                                )
                            }
                        }
                    }

                    // Error messages
                    if (errorMessage != null && !notRegistered) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                    if (notRegistered) {
                        TextButton(onClick = onRegister) {
                            Text(
                                "Please Complete Self Registration",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = onPasswordTextChange,
                        label = { Text("Password") },
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { onShowPasswordChange(!showPassword) }) {
                                Icon(
                                    imageVector = if (showPassword)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
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
                        onClick = onLogin,
                        enabled = !isLoading && selectedId.isNotBlank() && passwordText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black
                            )
                        } else {
                            Text("Continue", color = Color.Black, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Forgot Password link
                    TextButton(onClick = onForgotPassword) {
                        Text(
                            "Forgot Password?",
                            fontSize = 14.sp,
                            color = Color(0xFF137A44),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Haven't Registered Already?",
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
                        Text("Register", color = Color.Black, fontWeight = FontWeight.Medium)
                    }

                    // Extra padding to ensure nothing gets cut off
                    Spacer(Modifier.height(16.dp))
                }
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