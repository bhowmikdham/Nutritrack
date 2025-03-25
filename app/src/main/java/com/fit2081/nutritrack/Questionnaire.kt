package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    Scaffold(
        containerColor = Color(0xFFF8F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Food Intake Questionnaire", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFC1FF72)),
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
            Text("Tick all the food categories you can eat", fontSize = 16.sp, fontWeight = FontWeight.Bold)

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
                            modifier=Modifier.size(20.dp),
                            checked= fruits,
                            onCheckedChange = { fruits = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fruits")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier = Modifier.size(20.dp),
                            checked= vegetables,
                            onCheckedChange = { vegetables = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vegetables")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier=Modifier.size(20.dp),
                            checked= grains,
                            onCheckedChange = { grains = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
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
                            modifier=Modifier.size(20.dp),
                            checked= redMeat,
                            onCheckedChange = { redMeat = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Red Meat")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier=Modifier.size(20.dp),
                            checked= seafood,
                            onCheckedChange = { seafood = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seafood")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier=Modifier.size(20.dp),
                            checked= poultry,
                            onCheckedChange = { poultry = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
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
                            modifier=Modifier.size(20.dp),
                            checked= fish,
                            onCheckedChange = { fish = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fish")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier=Modifier.size(20.dp),
                            checked= eggs,
                            onCheckedChange = { eggs = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eggs")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            modifier=Modifier.size(20.dp),
                            checked= nutsSeeds,
                            onCheckedChange = { nutsSeeds = it },
                            colors = CheckboxDefaults.colors( checkedColor = Color(0xFF545454)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuts/Seeds")
                    }
                }
            }
        }
    }
}
