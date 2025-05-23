package com.fit2081.nutritrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.fit2081.nutritrack.data.AuthManager

@Composable
fun SettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userId = AuthManager.currentUserId() ?: return

    Column(
        modifier            = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Account ID: $userId", fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                AuthManager.signOut()
                navController.navigate("welcome") {
                    popUpTo(0)
                }
            },
            shape  = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72))
        ) {
            Text("Log Out", fontSize = 16.sp)
        }
    }
}
