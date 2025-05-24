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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Setup ViewModel with proper DI
    val db = remember { AppDatabase.getDatabase(context) }
    val authRepo = remember { AuthRepository(db.patientDAO()) }
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(authRepo)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Handle logout navigation
    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn && !uiState.isLoading) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Handle errors
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You can show a snackbar or handle the error as needed
            // For now, we'll just clear it after showing
            viewModel.clearError()
        }
    }

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
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // ACCOUNT HEADER
                Text(
                    "ACCOUNT",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))

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

                // OTHER SETTINGS HEADER
                Text(
                    "OTHER SETTINGS",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(16.dp))

                // Logout Button
                SettingsCard(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = { viewModel.logout() }
                )

                Spacer(Modifier.height(8.dp))

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