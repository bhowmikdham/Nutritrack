package com.fit2081.nutritrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                    LoginScreen()

                }
            }
        }
    }

/**
 * Validates the user by reading the CSV (users.csv) from the assets folder.
 * The CSV has a header, with:
 * Column 0: PhoneNumber
 * Column 1: User_ID
 */
fun validateUser(context: Context, userId: String, phoneNumber: String): Boolean {
    try {
        context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
            var isFirstLine = true
            lines.forEach { line ->
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false
                    return@forEach
                }
                val tokens = line.split(",")
                if (tokens.size >= 2) {
                    val csvPhoneNumber = tokens[0].trim()
                    val csvUserId = tokens[1].trim()
                    if (csvUserId == userId && csvPhoneNumber == phoneNumber) {
                        return true
                    }
                }
            }
        }
    } catch (e: Exception) {

    }
    return false
}
fun getUser_ID(context : Context):List<String> {
    val user_id = mutableListOf<String>()
    try {
        context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val delimeter = line.split(",")
                if (delimeter.size >=2) {
                    user_id.add(delimeter[1].trim())}
            }
            }
    } catch (e:Exception) {}
    return user_id

}
//INPUT VALIDATION:This is the function for validating Phone Number
fun isPhoneRegistered(context: Context, phoneNumber: String): Boolean {
    return try {
        context.assets.open("accounts.csv").bufferedReader().useLines { lines ->
            // Skip the header row
            lines.drop(1).any { line ->
                val tokens = line.split(",")
                // Compare the phone number in column 0
                tokens.isNotEmpty() && tokens[0].trim() == phoneNumber
            }
        }
    } catch (e: Exception) {
        // Log or handle error
        false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val userIds = remember { getUser_ID(context) }
    var userIdError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF8F5F5)
    )
    {
        //Box(modifier = Modifier.fillMaxSize()) {
            // Background color covering the top section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust this height to match the top background in the sample image
                    .background(Color(0xFFC1FF72)) // Change this to your desired color
            )

            // The main login container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp) // Adjust to create space from the top background
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)) // Rounded corners at the top
                    .background(Color.White) // White background for the login form
                    .padding(horizontal = 16.dp, vertical = 24.dp), // Padding inside the form
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Login Title
                Text(
                    text = "Log in",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Username Field
                val userIds = remember { getUser_ID(context) }
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        readOnly = true,
                        label = { Text("My ID (Provided by your Clinician)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userIds.forEach { id ->
                            DropdownMenuItem(
                                text = { Text(id) },
                                onClick = {
                                    userId = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                if (userIdError) {
                    Text(
                        text = "Invalid User ID",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = phoneNumber.isNotEmpty() && !isPhoneRegistered(context, phoneNumber)//we call the function here for input validation using the isphoneregistered function
                                    },
                    label = { Text("Phone number") },
                    isError = phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (phoneError) {
                    Text(
                        text = "Phone Number Not Registered",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Information Text
                Text(
                    text = "This app is only for pre-registered users. Please have your ID and phone number handy before continuing.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Continue Button
                Button(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)), // Purple color
                    onClick = {
                        if (validateUser(context, userId, phoneNumber)) {
                            //we are storing the login details in the shared preferences
                            val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                            with(sharedPrefs.edit()) {
                                putString("user_id", userId)
                                putString("phone_number", phoneNumber)
                                apply()
                            }
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
                            context.startActivity(Intent(context,Questionnaire::class.java))
                        } else {
                            Toast.makeText(context, "Incorrect Credentials", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Continue",color = Color(0xFF000000))
                }
            }
        }
    }