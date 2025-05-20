package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fit2081.nutritrack.AuthViewModel
import com.fit2081.nutritrack.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import com.fit2081.nutritrack.navigation.Screen

/**
 * Activity that hosts the LoginScreen composable.
 */
@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen(onSuccess = { userId ->
                // Navigate based on questionnaire completion
                val prefs = getSharedPreferences("prefs_$userId", MODE_PRIVATE)
                val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                val destination = if (completed) {
                    Screen.Dashboard.createRoute(userId)
                } else {
                    Screen.Questionnaire.createRoute(userId)
                }
                // Launch the appropriate Activity or navigate in host
                startActivity(Intent(this, Class.forName(
                    when {
                        destination.startsWith("dashboard/") -> com.fit2081.nutritrack.Dashboard::class.java.name
                        else -> com.fit2081.nutritrack.Questionnaire.QuestionnaireActivity::class.java.name
                    }
                )))
            })
        }
    }
}

/**
 * Composable for login UI, handling user input and navigation via onSuccess callback.
 * @param onSuccess Called with the userId when login succeeds and registration is complete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel = hiltViewModel(),
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val userIds by vm.userIds.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    // React to login results
    LaunchedEffect(vm.loginResult) {
        vm.loginResult.collect { result ->
            when (result) {
                LoginResult.Success -> {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                    onSuccess(vm.userId)
                }
                LoginResult.IncompleteRegistration -> {
                    Toast.makeText(
                        context,
                        "Please complete your self-registration first",
                        Toast.LENGTH_LONG
                    ).show()
                }
                LoginResult.InvalidCredentials -> {
                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // User ID dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = vm.userId,
                    onValueChange = vm::onUserIdChange,
                    label = { Text("User ID") },
                    isError = vm.userIdError,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    userIds.forEach { id ->
                        DropdownMenuItem(
                            text = { Text(id) },
                            onClick = {
                                vm.onUserIdChange(id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (vm.userIdError) {
                Text("Unknown user ID", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone number field
            OutlinedTextField(
                value = vm.phone,
                onValueChange = vm::onPhoneChange,
                label = { Text("Phone number") },
                isError = vm.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (vm.phoneError) {
                Text("Phone not registered", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = vm.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = vm::onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue")
            }
        }
    }
}
