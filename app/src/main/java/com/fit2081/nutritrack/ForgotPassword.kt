package com.fit2081.nutritrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.navigation.Screen

/**
    Forgot Password Screen with Multi-Step Flow

    Implements a secure password reset process with user verification
    Features step-by-step UI with progress indicators and comprehensive validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val authRepo = remember { AuthRepository(db.patientDAO()) }
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModelFactory(authRepo)
    )

    val uiState by viewModel.uiState.collectAsState()
    val userIds by viewModel.userIds.collectAsState()

    /**
        Auto-Navigation on Success

        Automatically redirects to login screen after successful password reset
        Provides user feedback during the transition period with countdown
     */
    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            // Auto-navigate to login after a delay
            kotlinx.coroutines.delay(3000) // 3 seconds
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.ForgotPassword.route) { inclusive = true }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    ) {
        // Top green header with image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(Color(0xFFC1FF72))
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding),
                contentDescription = "Forgot Password",
                modifier = Modifier.aspectRatio(0.8f),
                contentScale = ContentScale.Crop
            )
        }

        // White card with rounded top corners
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 280.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (uiState.step == ForgotPasswordStep.SET_NEW_PASSWORD) {
                            viewModel.goBack()
                        } else {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = when (uiState.step) {
                        ForgotPasswordStep.ENTER_DETAILS -> "Forgot Password"
                        ForgotPasswordStep.SET_NEW_PASSWORD -> "Reset Password"
                        ForgotPasswordStep.SUCCESS -> "Success!"
                        else -> "Reset Password"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            Spacer(Modifier.height(16.dp))

            // Step indicator
            StepIndicator(currentStep = uiState.step)

            Spacer(Modifier.height(24.dp))

            /**
                Dynamic Content Based on Current Step

                Renders appropriate UI content for each step of the password reset process
                Maintains state consistency across step transitions
             */
            when (uiState.step) {
                ForgotPasswordStep.ENTER_DETAILS -> {
                    EnterDetailsContent(
                        uiState = uiState,
                        userIds = userIds,
                        viewModel = viewModel
                    )
                }
                ForgotPasswordStep.SET_NEW_PASSWORD -> {
                    SetNewPasswordContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                ForgotPasswordStep.SUCCESS -> {
                    SuccessContent(navController = navController)
                }
                else -> { /* Handle other states if needed */ }
            }
        }
    }
}

/**
   Progress Step Indicator Component

   Visual progress indicator showing current step in the password reset flow
   Uses color coding to indicate completed, active, and pending steps
 */
@Composable
private fun StepIndicator(currentStep: ForgotPasswordStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(
            isActive = currentStep == ForgotPasswordStep.ENTER_DETAILS,
            isCompleted = currentStep != ForgotPasswordStep.ENTER_DETAILS
        )
        Divider(
            modifier = Modifier.width(24.dp),
            color = if (currentStep != ForgotPasswordStep.ENTER_DETAILS)
                Color(0xFFC1FF72) else Color.Gray
        )
        StepDot(
            isActive = currentStep == ForgotPasswordStep.SET_NEW_PASSWORD,
            isCompleted = currentStep == ForgotPasswordStep.SUCCESS
        )
        Divider(
            modifier = Modifier.width(24.dp),
            color = if (currentStep == ForgotPasswordStep.SUCCESS)
                Color(0xFFC1FF72) else Color.Gray
        )
        StepDot(
            isActive = currentStep == ForgotPasswordStep.SUCCESS,
            isCompleted = false
        )
    }
}

@Composable
private fun StepDot(isActive: Boolean, isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(
                color = when {
                    isCompleted -> Color(0xFFC1FF72)
                    isActive -> Color(0xFF137A44)
                    else -> Color.Gray
                },
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

/**
   User Details Verification Step

   Collects and validates user ID and phone number for identity verification
   Uses dropdown for user ID selection and phone number input validation
   Implements comprehensive security checks before allowing password reset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterDetailsContent(
    uiState: ForgotPasswordUiState,
    userIds: List<String>,
    viewModel: ForgotPasswordViewModel
) {
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter your User ID and phone number to verify your identity",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // User ID dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = uiState.userId,
                onValueChange = { viewModel.updateUserId(it) },
                label = { Text("User ID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.errorMessage != null,
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
                            viewModel.updateUserId(id)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Phone number field
        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.updatePhoneNumber(it) },
            label = { Text("Phone Number") },
            placeholder = { Text("Enter your registered phone number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Success message
        if (uiState.successMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.successMessage,
                color = Color(0xFF137A44),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        /**
           Identity Verification Button

           Triggers the verification process that checks:
           - User existence in system
           - Phone number registration
           - Correlation between user ID and phone number
         */
        Button(
            onClick = { viewModel.verifyUserDetails() },
            enabled = !uiState.isLoading && uiState.userId.isNotBlank() && uiState.phoneNumber.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC1FF72),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Text("Verify Details", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "We'll verify that this User ID and phone number match our records",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

/**
   New Password Creation Step

   Secure password creation interface with confirmation validation
   Includes password requirements display and real-time validation feedback
 */
@Composable
private fun SetNewPasswordContent(
    uiState: ForgotPasswordUiState,
    viewModel: ForgotPasswordViewModel
) {
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create a new password for your account",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // New password field
        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { viewModel.updateNewPassword(it) },
            label = { Text("New Password") },
            placeholder = { Text("Enter new password") },
            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                    Icon(
                        imageVector = if (showNewPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showNewPassword) "Hide password" else "Show password"
                    )
                }
            },
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(Modifier.height(16.dp))

        // Confirm password field
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text("Confirm New Password") },
            placeholder = { Text("Re-enter new password") },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                    )
                }
            },
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Error message
        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        /**
           Password Requirements Display

           Informational card showing password security requirements
           Helps users understand validation criteria before submission
         */
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Password Requirements:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "• At least 6 characters long",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "• Both passwords must match",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Reset password button
        Button(
            onClick = { viewModel.resetPassword() },
            enabled = !uiState.isLoading &&
                    uiState.newPassword.isNotBlank() &&
                    uiState.confirmPassword.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC1FF72),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
    Success Confirmation Screen

    Final step displaying successful password reset confirmation
    Provides navigation options and automatic redirect countdown
 */
@Composable
private fun SuccessContent(navController: NavHostController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF137A44)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Password Reset Successful!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF137A44),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Your password has been successfully reset. You can now login with your new password.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC1FF72),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Go to Login", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Redirecting to login in 3 seconds...",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}