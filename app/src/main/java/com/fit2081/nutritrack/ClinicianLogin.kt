package com.fit2081.nutritrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianLoginScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var passphrase by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val validPassphrase = "dollar-entry-apples"

    fun handleLogin() {
        isLoading = true
        if (passphrase == validPassphrase) {
            showError = false
            navController.navigate(Screen.ClinicianDashboard.route) {
                popUpTo(Screen.ClinicianLogin.route) { inclusive = true }
            }
        } else {
            showError = true
        }
        isLoading = false
    }

    Surface(
        modifier = modifier.fillMaxSize(),
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
                contentDescription = "Clinician login",
                modifier = Modifier.aspectRatio(0.7f),
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
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Text(
                text = "Clinician Access",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Enter your authorized passphrase to access the clinician dashboard",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Passphrase field
            OutlinedTextField(
                value = passphrase,
                onValueChange = {
                    passphrase = it
                    showError = false
                },
                label = { Text("Passphrase") },
                placeholder = { Text("Enter authorized passphrase") },
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
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                isError = showError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (showError) {
                Text(
                    text = "Invalid passphrase. Please contact your administrator.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Login button
            Button(
                onClick = { handleLogin() },
                enabled = !isLoading && passphrase.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC1FF72),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        "Access Dashboard",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "For authorized clinical staff only",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}