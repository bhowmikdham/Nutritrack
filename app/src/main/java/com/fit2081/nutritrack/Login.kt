package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.AuthRepository
import com.fit2081.nutritrack.AuthViewModel
import com.fit2081.nutritrack.LoginResult
import com.fit2081.nutritrack.navigation.Screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.fit2081.nutritrack.ui.theme.NutritrackTheme

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual DI setup
        val db = AppDatabase.getDatabase(this)
        val authRepo = AuthRepository(db.patientDAO())
        val vm = AuthViewModel(authRepo)

        setContent {
            NutritrackTheme {
                LoginScreen(vm = vm) { userId ->
                    // Navigate based on questionnaire completion
                    val prefs = getSharedPreferences("prefs_$userId", MODE_PRIVATE)
                    val completed = prefs.getBoolean("QuestionnaireCompleted", false)
                    val destClass = if (completed) Dashboard::class.java else Questionnaire::class.java
                    startActivity(Intent(this, destClass))
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel,
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
                    Toast.makeText(context, "Please complete your self-registration first", Toast.LENGTH_LONG).show()
                }
                LoginResult.InvalidCredentials -> {
                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color(0xFFC1FF72))){
        Image(
                painter = painterResource(id = R.drawable.onboarding),
                contentDescription = "Main logo",
                modifier = Modifier
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
        )}
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
                    value = vm.userId,
                    onValueChange = vm::onUserIdChange,
                    label = { Text("My ID (Provided by your Clinician)") },
                    isError = vm.userIdError,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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
                Text(
                    text = "Invalid User ID",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(Modifier.height(16.dp))

            // Phone number field
            OutlinedTextField(
                value = vm.phone,
                onValueChange = vm::onPhoneChange,
                label = { Text("Phone number") },
                isError = vm.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (vm.phoneError) {
                Text(
                    text = "Phone Number Not Registered",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "This app is only for pre-registered users. Please have your ID and phone number handy before continuing.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = vm::onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Continue", color = Color.Black)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { context.startActivity(Intent(context, Register::class.java)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Register", color = Color.Black)
            }
        }
    }
}
