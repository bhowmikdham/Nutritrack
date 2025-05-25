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

/**
 * Authentication Activity Implementation
 *
 * Main login activity that handles user authentication flow for the NutriTrack application
 * Implements Android Activity lifecycle patterns and Material 3 design system
 * Supports both Activity-based and Navigation Compose patterns for flexible integration
 *
 * Architecture Components Used:
 * - ViewModel pattern for state management
 * - Repository pattern for data access
 * - Dependency Injection through lazy initialization
 * - Factory pattern for ViewModel creation
 *
 * Credit: Implementation follows Android Architecture Components guidelines
 * Reference: https://developer.android.com/topic/architecture
 */
class Login : ComponentActivity() {
    /**
     * Repository Initialization with Lazy Loading
     *
     * Uses lazy initialization pattern to defer expensive database operations
     * until actually needed, improving app startup performance
     * Follows singleton pattern for repository instances
     */
    private val authRepo by lazy {
        AuthRepository(AppDatabase.getDatabase(this).patientDAO())
    }

    /**
     * ViewModel Integration with Factory Pattern
     *
     * Uses viewModels() delegate for automatic lifecycle management
     * Factory pattern ensures proper dependency injection for repositories
     * Provides automatic cleanup when Activity is destroyed
     */
    private val vm: AuthViewModel by viewModels { AuthViewModelFactory(authRepo) }

    /**
     * Activity Lifecycle Implementation
     *
     * Follows Android Activity lifecycle best practices
     * Enables edge-to-edge display for modern Android UI
     * Sets up Compose content with Material 3 theming
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                // Note: This uses the simple version for Activity-based navigation
                LoginScreenSimple(
                    vm = vm,
                    /**
                     * Navigation Logic with SharedPreferences
                     *
                     * Implements user-specific preference storage using MODE_PRIVATE
                     * Determines navigation destination based on questionnaire completion
                     * Follows Android security best practices for user data
                     */
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
                    /**
                     * Feature Availability Handling
                     *
                     * Graceful degradation for features not available in Activity-based navigation
                     * Provides user feedback through Toast messages
                     * Suggests alternative navigation paths
                     */
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

/**
 * Simplified Login Screen for Activity-Based Navigation
 *
 * Material 3 Composable implementation designed for Activity-based navigation flows
 * Handles authentication state management and user interactions
 * Provides seamless integration with legacy Activity navigation patterns
 *
 * State Management Pattern:
 * - Reactive state collection using collectAsState()
 * - Unidirectional data flow from ViewModel to UI
 * - Side effects handled through LaunchedEffect
 *
 * Credit: Implementation follows Jetpack Compose best practices
 * Reference: https://developer.android.com/jetpack/compose/state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenSimple(
    vm: AuthViewModel,
    onNavigate: (String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    /**
     * ViewModel State Collection
     *
     * Reactive state management using StateFlow and collectAsState()
     * Automatically recomposes UI when ViewModel state changes
     * Follows MVVM architecture pattern for separation of concerns
     */
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

    /**
     * Side Effect Navigation Handling
     *
     * LaunchedEffect for handling navigation as a side effect
     * Prevents navigation from being triggered on every recomposition
     * Ensures navigation only occurs when login is actually successful
     */
    // Navigate on success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess == true) {
            onNavigate(selectedId)
        }
    }

    /**
     * Shared UI Content Delegation
     *
     * Delegates to shared UI content component for consistency
     * Follows DRY principle by reusing common UI logic
     * Maintains separation between navigation and presentation logic
     */
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

/**
 * Navigation Compose Version (Main version to use)
 *
 * Primary login screen implementation designed for Navigation Compose
 * Provides full integration with modern Android navigation patterns
 * Supports advanced features like deep linking and navigation graphs
 *
 * Navigation Integration:
 * - NavHostController for programmatic navigation
 * - Type-safe navigation using Screen sealed class
 * - Proper back stack management
 *
 * Credit: Implementation follows Navigation Compose guidelines
 * Reference: https://developer.android.com/jetpack/compose/navigation
 */
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

/**
 * Shared UI Content with LazyColumn for proper scrolling and rotation handling
 *
 * Core UI implementation that provides the visual login interface
 * Uses LazyColumn for proper scrolling behavior and orientation changes
 * Implements Material 3 design system with custom branding colors
 *
 * UI Architecture:
 * - LazyColumn for efficient scrolling and memory management
 * - Material 3 components for consistent design language
 * - Custom color scheme matching app branding
 * - Responsive layout that adapts to different screen sizes
 *
 * Accessibility Features:
 * - Proper content descriptions for screen readers
 * - Keyboard navigation support
 * - High contrast color combinations
 * - Touch target sizing following Material guidelines
 *
 * Credit: UI implementation follows Material Design 3 guidelines
 * Reference: https://m3.material.io/
 */
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
    /**
     * Surface Container with Custom Branding
     *
     * Uses custom color scheme that matches NutriTrack branding
     * Provides full-screen container with proper background color
     * Follows Material 3 color system guidelines
     */
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    ) {
        /**
         * LazyColumn Implementation for Scrollable Content
         *
         * LazyColumn provides efficient scrolling and handles orientation changes
         * Items are only composed when visible, improving performance
         * Proper spacing and alignment for professional appearance
         *
         * Credit: LazyColumn implementation follows Compose documentation
         * Reference: https://developer.android.com/jetpack/compose/lists
         */
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /**
             * Top Green Header with Branding Image
             *
             * Visual branding section that establishes app identity
             * Uses custom green color from NutriTrack brand guidelines
             * AspectRatio ensures consistent image scaling across devices
             */
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

            /**
             * White Card Content with Rounded Corners
             *
             * Main content area with Material 3 design elements
             * Negative offset creates visual overlap with header image
             * Rounded corners provide modern, friendly appearance
             * Proper padding ensures comfortable touch targets
             */
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
                    /**
                     * Title Section
                     *
                     * Clear, bold title that establishes screen purpose
                     * Typography follows Material 3 design tokens
                     * Proper spacing creates visual hierarchy
                     */
                    Text(
                        text = "Log in",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(24.dp))

                    /**
                     * User ID Dropdown Implementation
                     *
                     * ExposedDropdownMenuBox provides Material 3 dropdown functionality
                     * Integrates with pre-seeded user IDs from clinician setup
                     * Includes proper error states and validation feedback
                     * menuAnchor() ensures proper dropdown positioning
                     *
                     * Accessibility:
                     * - Screen reader support through contentDescription
                     * - Keyboard navigation compatibility
                     * - Error state announcements
                     *
                     * Credit: Dropdown implementation follows Material 3 specifications
                     * Reference: https://m3.material.io/components/menus/overview
                     */
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

                    /**
                     * Error Handling and User Feedback
                     *
                     * Comprehensive error display system with multiple error types
                     * Different handling for authentication errors vs registration status
                     * Material 3 error colors for consistent visual feedback
                     * Interactive elements for user guidance and recovery
                     */
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

                    /**
                     * Password Field with Visibility Toggle
                     *
                     * Secure password input with show/hide functionality
                     * Material 3 design with proper error states
                     * IconButton provides accessibility for password visibility
                     * PasswordVisualTransformation ensures security by default
                     *
                     * Security Features:
                     * - Password masking by default
                     * - Optional visibility for user verification
                     * - Proper keyboard type for password input
                     * - No password value logging or persistence
                     *
                     * Credit: Password field follows Android security guidelines
                     * Reference: https://developer.android.com/training/articles/keystore
                     */
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

                    /**
                     * Continue Button with Loading States
                     *
                     * Primary action button with comprehensive state management
                     * Loading indicator prevents double-submission
                     * Disabled state when form is incomplete
                     * Custom colors matching app branding
                     *
                     * Button States:
                     * - Enabled: Both fields filled and not loading
                     * - Disabled: Missing required fields
                     * - Loading: Authentication in progress
                     *
                     * UX Considerations:
                     * - Clear visual feedback for all states
                     * - Prevents accidental double-taps
                     * - Consistent sizing for touch targets
                     */
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

                    /**
                     * Secondary Actions Section
                     *
                     Additional user actions for account recovery and registration
                     TextButton styling for secondary importance
                     Clear hierarchy between primary and secondary actions
                     Text Button Usage Taken from Android Documentations
                     */
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

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
  ViewModel Factory for Dependency Injection

 Factory pattern implementation for proper ViewModel instantiation
 Enables dependency injection while maintaining ViewModel lifecycle
 Ensures type safety and proper error handling for unknown ViewModels

 Architecture Benefits:
 - Separates object creation from business logic
 - Enables testing with mock repositories
 - Maintains proper ViewModel lifecycle management
 - Supports dependency injection frameworks

 Credit: Factory pattern follows Android Architecture Components guidelines
 Reference: https://developer.android.com/topic/libraries/architecture/viewmodel
 */
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