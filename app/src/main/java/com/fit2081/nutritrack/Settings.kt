package com.fit2081.nutritrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.navigation.Screen

/**
     Settings Screen Implementation

     Comprehensive user settings and account management interface
     Provides user profile information display and account actions
     Implements secure logout functionality and navigation management

     Core Features:
     - User profile information display
     - Secure logout with proper session cleanup
     - Navigation to clinician login interface
     - Real-time profile data updates
     - Error handling and loading states

     Security Features:
     - Authenticated access only
     - Secure session management
     - Proper logout with complete state cleanup
     - Navigation security for unauthorized access

     UI Components:
     - Material 3 design system implementation
     - Responsive layout with proper spacing
     - Loading states and error handling
     - Accessible design with proper semantics

     Architecture Pattern:
     - MVVM with reactive state management
     - Repository pattern for data access
     - Dependency injection through manual DI
     - Proper lifecycle management

     Credit: Implementation follows Android Architecture Components guidelines
     Reference: https://developer.android.com/topic/architecture
    CREDIT: GIVEN TO GEN AI (CHAT GPT) FOR HELPING IN THE CODE FORMATION IN THIS FILE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    /**
         Dependency Injection Setup

         Manual dependency injection using remember for proper lifecycle management
         Database instance cached to prevent unnecessary recreations
         Repository provides abstracted access to authentication data
         ViewModel factory ensures proper dependency injection

         DI Benefits:
         - Testability through interface abstraction
         - Consistent data access patterns
         - Proper lifecycle management
         - Memory efficiency through caching
     */
    // Setup ViewModel with proper DI
    val db = remember { AppDatabase.getDatabase(context) }
    val authRepo = remember { AuthRepository(db.patientDAO()) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(authRepo)
    )

    val uiState by viewModel.uiState.collectAsState()

    /**
         Navigation Side Effect Management

         LaunchedEffect handles navigation as side effect
         Prevents navigation during active composition
         Ensures proper back stack management
         Clears entire navigation stack on logout

         Navigation Security:
         - Redirects unauthorized users to welcome screen
         - Clears sensitive data from navigation stack
         - Prevents back navigation to authenticated screens
         - Maintains proper app flow state
     */
    // Handle logout navigation
    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn && !uiState.isLoading) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    /**
         Error Handling Side Effects

         Automatic error clearing for better user experience
         LaunchedEffect ensures error handling doesn't interfere with composition
         Could be extended to show snackbars or other error UI
         Provides clean error state management
     */
    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar or handle the error as needed
            // For now, we'll just clear it after showing
            viewModel.clearError()
        }
    }

    /**
         Main Settings Scaffold Structure

         Material 3 Scaffold provides consistent app structure
         TopAppBar with refresh functionality for data updates
         Proper content padding management through innerPadding
         Clean white background for professional appearance

         Scaffold Benefits:
         - Consistent Material Design structure
         - Automatic layout management
         - Built-in support for floating action buttons
         - Proper coordinate system for animations
     */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshProfile() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        /**
             Loading State Management

             Centered loading indicator during data fetching
             Proper loading state prevents showing incomplete data
             Material 3 CircularProgressIndicator for consistency
             Full-screen loading overlay for clear user feedback
         */
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.isLoggedIn) {
            /**
                 Unauthorized Access Handling

                 Clear messaging for unauthenticated users
                 Prevents showing sensitive settings data
                 Graceful handling of session expiration
                 User-friendly messaging for login requirement
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Please log in to access settings",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            /**
                 Main Settings Content Layout

                 Organized settings interface with clear sections
                 Proper spacing and visual hierarchy
                 Consistent padding and margin management
                 Responsive layout for different screen sizes
             */
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                /**
                     Account Section Header

                     Clear section labeling with Material Design typography
                     Gray color for secondary importance
                     Consistent styling across section headers
                     Proper spacing for visual separation
                 */
                // ACCOUNT HEADER
                Text(
                    "ACCOUNT",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))

                /**
                     User Information Display Card

                     Comprehensive user profile information display
                     Material 3 Card component for consistent styling
                     Organized information rows with icons and labels
                     Fallback text for missing information

                     Information Categories:
                     - Personal name for identification
                     - Phone number for contact information
                     - User ID for system identification
                     - Proper fallback messaging for missing data
                    */
                // User Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        UserInfoRow(
                            icon = Icons.Default.Person,
                            label = "Name",
                            value = uiState.userName.ifEmpty { "Not provided" }
                        )

                        Spacer(Modifier.height(12.dp))

                        UserInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = uiState.phoneNumber.ifEmpty { "Not provided" }
                        )

                        Spacer(Modifier.height(12.dp))

                        UserInfoRow(
                            icon = Icons.Default.Badge,
                            label = "User ID",
                            value = uiState.currentUserId ?: "Unknown"
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                /**
                     Additional Settings Section

                     Secondary settings options with clear labeling
                     Consistent styling with account section
                     Proper visual hierarchy and spacing
                     Future-extensible design for additional options
                 */
                // OTHER SETTINGS HEADER
                Text(
                    "OTHER SETTINGS",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))

                /**
                     Logout Functionality

                     Secure logout with proper session cleanup
                     Clear visual identification with logout icon
                     ViewModel-handled logout logic for security
                     Proper state management during logout process
                 */
                // Logout Button
                SettingsCard(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = { viewModel.logout() }
                )

                Spacer(Modifier.height(8.dp))

                /**
                     Clinician Access Portal

                     Navigation to clinician login interface
                     Administrative access for healthcare providers
                     Secure navigation to privileged functionality
                     Clear visual identification with admin icon
                 */
                // Clinician Login Button
                SettingsCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Clinician Login",
                    onClick = { navController.navigate(Screen.ClinicianLogin.route) }
                )
            }
        }
    }
}

/**
     User Information Row Component

     Reusable component for displaying user profile information
     Consistent layout and styling across different information types
     Icon-based visual identification for each data category
     Proper typography hierarchy for labels and values

     Design Pattern:
     - Icon for visual identification
     - Label for field description
     - Value for actual user data
     - Consistent spacing and alignment

     Accessibility Features:
     - Icon content descriptions for screen readers
     - Proper text contrast for readability
     - Clear visual hierarchy through typography
     - Adequate touch targets for interaction

     Credit: Component design follows Material Design guidelines
     Reference: https://m3.material.io/components/cards/overview
 */
@Composable
private fun UserInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
     Settings Action Card Component

     Interactive card component for settings actions
     Material 3 Card with click handling for user interactions
     Consistent styling with chevron indicator for navigation
     Icon-based visual identification for each action

     Component Features:
     - Material 3 Card styling for consistency
     - Click handling for action execution
     - Icon identification for action type
     - Chevron indicator for navigation feedback
     - Proper spacing and touch targets

     Interaction Design:
     - Clear visual feedback on interaction
     - Consistent spacing and alignment
     - Professional appearance with rounded corners
     - Accessible design with proper semantics

     Use Cases:
     - Logout functionality
     - Navigation to other screens
     - Action execution with confirmation
     - Settings toggles and preferences

     Credit: Interactive card design follows Material Design guidelines
     Reference: https://m3.material.io/components/cards/overview
 */
@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(title, fontSize = 16.sp)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}