package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.fit2081.nutritrack.ui.theme.NutritrackTheme


class Questionnaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                FoodIntakeQuestionnaire()
            }
        }
    }
}








@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
    fun FoodIntakeQuestionnaire() {
    val context = LocalContext.current
    // Declare your checkbox states directly here:
    var fruits by remember { mutableStateOf(false) }
    var vegetables by remember { mutableStateOf(false) }
    var grains by remember { mutableStateOf(false) }
    var redMeat by remember { mutableStateOf(false) }
    var seafood by remember { mutableStateOf(false) }
    var poultry by remember { mutableStateOf(false) }
    var fish by remember { mutableStateOf(false) }
    var eggs by remember { mutableStateOf(false) }
    var nutsSeeds by remember { mutableStateOf(false) }

    var showHealthyDialog by remember { mutableStateOf(false) }
    var showIndulgentDialog by remember { mutableStateOf(false) }
    var showAdventurousDialog by remember { mutableStateOf(false) }
    var showComfortDialog by remember { mutableStateOf(false) }
    var showBalancedDialog by remember { mutableStateOf(false) }
    var showPickyDialog by remember { mutableStateOf(false) }

    val biggestMealTime = remember { mutableStateOf("") }
    val SleepTime = remember { mutableStateOf("") }
    val WakeTime = remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF8F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Food Intake Questionnaire", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(
                        0xFFC1FF72
                    )
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        // Navigate back to Login
                        context.startActivity(Intent(context, Login::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Login"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            Text(
                "Tick all the food categories you can eat",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // First Column: Fruits, Vegetables, Grains
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = fruits,
                            onCheckedChange = { fruits = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fruits")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = vegetables,
                            onCheckedChange = { vegetables = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vegetables")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = grains,
                            onCheckedChange = { grains = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Grains")
                    }
                }
                // Second Column: Red Meat, Seafood, Poultry
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = redMeat,
                            onCheckedChange = { redMeat = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Red Meat")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = seafood,
                            onCheckedChange = { seafood = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seafood")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = poultry,
                            onCheckedChange = { poultry = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Poultry")
                    }
                }
                // Third Column: Fish, Eggs, Nuts/Seeds
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = fish,
                            onCheckedChange = { fish = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fish")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = eggs,
                            onCheckedChange = { eggs = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF545454))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eggs")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked = nutsSeeds,
                            onCheckedChange = { nutsSeeds = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuts/Seeds")
                    }
                }
            }
            //Persona Implementation
            //My Approach
            /**
             *Text - Persona Heading ("Your Persona")
             *subText - explaing persona (small font size)
             * space
             * state for the modals
             * make a list for iteraiton(persona headings mapped to their descriptions)
             * make a general modal loop , which loops on the list of modals
             */

            Text("Your Persona", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                "People can be broadly classified into 6 different types based on their eating preferences. Click on each button below to find out the different types, and select the type that best fits you!",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            //State


            //List for iteration (persona headings mapped to their description)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // First row: first button right-aligned, second centered, third left-aligned.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box() {
                        Button(
                            onClick = { showHealthyDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Health Devotee", fontSize = 12.sp)
                        }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { showIndulgentDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Indulgent Eater", fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            onClick = { showAdventurousDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)

                        ) {
                            Text("Adventurous Eater", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Second row: first button right-aligned, second centered, third left-aligned.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box() {
                        Button(
                            onClick = { showComfortDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Balance Seeker", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { showBalancedDialog = true },
                            shape = RoundedCornerShape(9.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Health Procastinator", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { showPickyDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Picky Eater", fontSize = 12.sp)
                        }
                    }
                }
            }


            // Discrete AlertDialog for Healthy Eater
            if (showHealthyDialog) {
                AlertDialog(
                    onDismissRequest = { showHealthyDialog = false },
                    title = { Text("Healthy Eater", fontWeight = FontWeight.Bold) },
                    text = { Text("You prioritize nutrition, whole foods, and balanced meals.") },
                    confirmButton = {
                        Button(onClick = { showHealthyDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            // Discrete AlertDialog for Indulgent Eater
            if (showIndulgentDialog) {
                AlertDialog(
                    onDismissRequest = { showIndulgentDialog = false },
                    title = { Text("Indulgent Eater", fontWeight = FontWeight.Bold) },
                    text = { Text("You enjy occasional treats and desserts, and love to indulge.") },
                    confirmButton = {
                        Button(onClick = { showIndulgentDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            // Discrete AlertDialog for Adventurous Eater
            if (showAdventurousDialog) {
                AlertDialog(
                    onDismissRequest = { showAdventurousDialog = false },
                    title = { Text("Adventurous Eater", fontWeight = FontWeight.Bold) },
                    text = { Text("You love trying exotic cuisines and bold flavors.") },
                    confirmButton = {
                        Button(onClick = { showAdventurousDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            // Discrete AlertDialog for Comfort Food Lover
            if (showComfortDialog) {
                AlertDialog(
                    onDismissRequest = { showComfortDialog = false },
                    title = { Text("Comfort Food Lover", fontWeight = FontWeight.Bold) },
                    text = { Text("You find comfort in familiar and soothing dishes.") },
                    confirmButton = {
                        Button(onClick = { showComfortDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            // Discrete AlertDialog for Balanced Eater
            if (showBalancedDialog) {
                AlertDialog(
                    onDismissRequest = { showBalancedDialog = false },
                    title = { Text("Balanced Eater", fontWeight = FontWeight.Bold) },
                    text = { Text("You maintain a balanced diet with a mix of different food groups.") },
                    confirmButton = {
                        Button(onClick = { showBalancedDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            // Discrete AlertDialog for Picky Eater
            if (showPickyDialog) {
                AlertDialog(
                    onDismissRequest = { showPickyDialog = false },
                    title = { Text("Picky Eater", fontWeight = FontWeight.Bold) },
                    text = { Text("You are selective about your meals and have specific tastes.") },
                    confirmButton = {
                        Button(onClick = { showPickyDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                "Which persona suits you the best?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                var expanded by remember { mutableStateOf(false) }
                var selectedPersona by remember { mutableStateOf("") }
                val personas = listOf(
                    "Healthy Devotee",
                    "Mindful Eater",
                    "Wellness Striver",
                    "Balanced Seeker",
                    "Health Procrastinator",
                    "Picky Eater"
                )
                OutlinedTextField(
                        value = selectedPersona,
                        onValueChange = { },
                        readOnly = true,
                        shape = RoundedCornerShape(20.dp),
                        label = { Text("Choose your Persona Type") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Drop Down Menu"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable{expanded = true}
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        personas.forEach { persona ->
                            DropdownMenuItem(
                                text = { Text(persona) },
                                onClick = {
                                    selectedPersona = persona
                                    expanded = false
                                }
                            )
                        }
                    }

                }
            Spacer(modifier = Modifier.height(15.dp))
            Text("Timings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start){
                Text("What time of day approx. do you normally eat your biggest meal?")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start){

                }



            }


            Button(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    context.startActivity(Intent(context,Dashboard::class.java))
                }


            ){Text("Save And Continue")

            }
            }

    }}



