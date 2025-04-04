package com.fit2081.nutritrack

import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/**
 *
 *Time Picker Function , function taken from the lab and then modified accordingly
 *
 * Took help of chat gpt and read android documentation to get the data in two digit format
 *
 */
fun timePickerFun(context: android.content.Context, mTime: MutableState<String>): TimePickerDialog {
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    return TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            mTime.value = String.format("%02d:%02d", selectedHour, selectedMinute)// string %02d is used here to always fromat the hours:minutes in two digit format

        },
        mHour,
        mMinute,
        false // 'false' means 12-hour format; set 'true' for 24-hour format
    )
}

/**
 *
 * Main Composable for the Questionnaire
 *
 */
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodIntakeQuestionnaire() {
    val context = LocalContext.current
    var Checkbox_Error by remember { mutableStateOf(false) }
    var Persona_Error by remember { mutableStateOf(false) }

    /**
     *
     *Shared Preferences and Data Retrieval
     *
     */
    val sharedPrefs_global = context.getSharedPreferences("my_prefs", android.content.Context.MODE_PRIVATE)
    //we do this so that we dont collide with diff users and each user have its own shared perf data stored when their questionnaire is loaded or edited
    val userId = sharedPrefs_global.getString("user_id", "") ?: "default"
    val sharedPrefs = context.getSharedPreferences("my_prefs_$userId", android.content.Context.MODE_PRIVATE)
    var fruits by remember { mutableStateOf(sharedPrefs.getBoolean("fruits", false)) }
    var vegetables by remember { mutableStateOf(sharedPrefs.getBoolean("vegetables", false)) }
    var grains by remember { mutableStateOf(sharedPrefs.getBoolean("grains", false)) }
    var redMeat by remember { mutableStateOf(sharedPrefs.getBoolean("red_meat", false)) }
    var seafood by remember { mutableStateOf(sharedPrefs.getBoolean("seafood", false)) }
    var poultry by remember { mutableStateOf(sharedPrefs.getBoolean("poultry", false)) }
    var fish by remember { mutableStateOf(sharedPrefs.getBoolean("fish", false)) }
    var eggs by remember { mutableStateOf(sharedPrefs.getBoolean("eggs", false)) }
    var nutsSeeds by remember { mutableStateOf(sharedPrefs.getBoolean("nuts_seeds", false)) }

    /**
     *
     *Var Initialisation for the AlertDialogs for saving their states
     *
     */
    var showHealthyDialog by remember { mutableStateOf(false) }
    var showMindfulDialog by remember { mutableStateOf(false) }
    var showWellnessDialog by remember { mutableStateOf(false) }
    var showBalancedDialog by remember { mutableStateOf(false) }
    var showProcastinatorDialog by remember { mutableStateOf(false) }
    var showCarefreeDialog by remember { mutableStateOf(false) }
    var saveDialog by remember { mutableStateOf(false) }

    /**
     *
     *Initialize time states from SharedPreferences
     *
     *
     *
     */
    val biggestMealTime = remember {
        mutableStateOf(sharedPrefs.getString("biggest_meal_time", "") ?: "")
    }
    val SleepTime = remember {
        mutableStateOf(sharedPrefs.getString("sleep_time", "") ?: "")
    }
    val WakeTime = remember {
        mutableStateOf(sharedPrefs.getString("wake_time", "") ?: "")
    }

    // Initialize selected persona from SharedPreferences
    var selectedPersona by remember {
        mutableStateOf(sharedPrefs.getString("selected_persona", "") ?: "")
    }
    /**
     *
     * Following is our Composable Code for the Questionnaire
     *
     */
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
                //this is initialised inside the top Bar
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Tick all the food categories you can eat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                /**
                 *
                 *Thinking
                 *One Row and three Coulmns in it and 3 rows in that, each row which would have
                 * checkboxes with their labels on the right
                 *
                 * Some of the code below you see is taken from the Lab and then modified accordingly
                 */
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
                        //Fruits
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
                        //Vegetables
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
                        //Grains
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
                        //Red Meat
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
                        //Seafood
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
                        //Poultry
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
                        //Fish
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
                        //Eggs
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
                        //Nutts and Seeds
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
                if (Checkbox_Error){
                    Text("Please select at leaset One", color = Color.Red)}
                /**
                 *
                 * Persona Implementation
                 *
                 */
                HorizontalDivider(thickness = 2.dp)// taken from : https://developer.android.com/develop/ui/compose/components/divider
                Text("Your Persona", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "People can be broadly classified into 6 different types based on their eating preferences. Click on each button below to find out the different types, and select the type that best fits you!",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Made use of Box for easy alignment
                    //implementation of the Box https://www.youtube.com/watch?v=rw80qs6ErWQ + chatGpt
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box() {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showHealthyDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Health Devotee", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showMindfulDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Mindful Eater", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showWellnessDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Welness Striver", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Second row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box() {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showBalancedDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Balance Seeker", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showProcastinatorDialog = true },
                                shape = RoundedCornerShape(9.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Health Procastinator", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                                onClick = { showCarefreeDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Text("Food Carefree", fontSize = 11.sp, color = Color.Black)
                            }
                        }
                    }
                }
                /**
                 *
                 * Alert Dialog Implementation
                 *
                 * Code taken from Applied Class and Modified accordingly
                 *
                 */
                // AlertDialog for Healthy Eater
                if (showHealthyDialog) {
                    AlertDialog(
                        onDismissRequest = { showHealthyDialog = false },
                        title = { Text("Healthy Eater", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_1),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I’m passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showHealthyDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                // AlertDialog for Mindful Eater
                if (showMindfulDialog) {
                    AlertDialog(
                        onDismissRequest = { showMindfulDialog = false },
                        title = { Text("Mindful Eater", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_2),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I’m health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showMindfulDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                // AlertDialog for Wellness Striver
                if (showWellnessDialog) {
                    AlertDialog(
                        onDismissRequest = { showWellnessDialog = false },
                        title = { Text("Welness Striver", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_3),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showWellnessDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                // AlertDialog for Balance Seeker
                if (showBalancedDialog) {
                    AlertDialog(
                        onDismissRequest = { showBalancedDialog = false },
                        title = { Text("Balance Seeker", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_4),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showBalancedDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                // AlertDialog for Health Procrastinator
                if (showProcastinatorDialog) {
                    AlertDialog(
                        onDismissRequest = { showProcastinatorDialog = false },
                        title = { Text("Health Procastinator", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_5),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I’m contemplating healthy eating but it’s not a priority for me right now. I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showProcastinatorDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                // AlertDialog for Food Carefree
                if (showCarefreeDialog) {
                    AlertDialog(
                        onDismissRequest = { showCarefreeDialog = false },
                        title = { Text("Food Carefree", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.persona_6),
                                    contentDescription = "Balance Seeker",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("I’m not bothered about healthy eating. I don’t really see the point and I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat.", textAlign = TextAlign.Center)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showCarefreeDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.padding(3.dp))
                HorizontalDivider(thickness = 2.dp)// taken from https://developer.android.com/develop/ui/compose/components/divider
                Spacer(modifier = Modifier.padding(3.dp))
                Text(
                    "Which persona suits you the best?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border( //discovered this function on the Android Documentation
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val personas = listOf(
                        "Healthy Devotee",
                        "Mindful Eater",
                        "Wellness Striver",
                        "Balanced Seeker",
                        "Health Procrastinator",
                        "Food Carefree"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {//Code for DropDown menu taken from applied and Modified accordingly
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedPersona)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
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
                }
                if (Persona_Error){
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Please select a Persona", color = Color.Red)}
                /**
                 *
                 * Timings Implementation
                 *
                 */
                Spacer(modifier = Modifier.height(15.dp))
                Text("Timings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                //taken help from ChatGpt to help me position things well and use of .show()
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("What time of day approx. do you normally eat your biggest meal?", fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context, mTime = biggestMealTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (biggestMealTime.value.isEmpty()) {
                                        "00:00"
                                    } else {
                                        biggestMealTime.value
                                    }, color = Color.Black
                                )
                            }
                        }
                    }
                }
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("What time of day approx. do you normally eat your biggest meal?", fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context, mTime = SleepTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (SleepTime.value.isEmpty()) {
                                        "00:00"
                                    } else {
                                        SleepTime.value
                                    }, color = Color.Black
                                )
                            }
                        }
                    }
                }
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("What time of day approx. do you normally eat your biggest meal?", fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context, mTime = WakeTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (WakeTime.value.isEmpty()) {
                                        "00:00"
                                    } else {
                                        WakeTime.value
                                    }, color = Color.Black
                                )
                            }
                        }
                    }
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A44)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = { saveDialog = true }
                ) {
                    Text("Review And Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                //Creating a dialog box for confirmation of the information entered
                if (saveDialog) {
                    AlertDialog(
                        onDismissRequest = { saveDialog = false },
                        title = { Text("Below Is The Information You Entered", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("Categories You Can Eat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                if (fruits) {
                                    Text("Fruits")
                                }
                                if (vegetables) {
                                    Text("Vegetables")
                                }
                                if (grains) {
                                    Text("Grains")
                                }
                                if (redMeat) {
                                    Text("Red Meat")
                                }
                                if (seafood) {
                                    Text("Seafood")
                                }
                                if (poultry) {
                                    Text("Poultry")
                                }
                                if (fish) {
                                    Text("Fish")
                                }
                                if (eggs) {
                                    Text("Eggs")
                                }
                                if (nutsSeeds) {
                                    Text("Nuts/Seeds")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Persona You Selected", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(selectedPersona)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Timings You Selected", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Biggest Meal Time: ${biggestMealTime.value}")
                                Text("Sleep Time: ${SleepTime.value}")
                                Text("Wake Time: ${WakeTime.value}")
                            }
                        },
                        /**
                         *
                         *When button is pressed the data gets saved into the shred preference of that particular logged in user
                         * and the user is directed towards the Home screen
                         *
                         */
                        confirmButton = {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A44)),
                                onClick = {
                                    val sharedPrefs = context.getSharedPreferences(
                                        "my_prefs_$userId",
                                        android.content.Context.MODE_PRIVATE
                                    )
                                    with(sharedPrefs.edit()) {
                                        putBoolean("fruits", fruits)
                                        putBoolean("vegetables", vegetables)
                                        putBoolean("grains", grains)
                                        putBoolean("red_meat", redMeat)
                                        putBoolean("seafood", seafood)
                                        putBoolean("poultry", poultry)
                                        putBoolean("fish", fish)
                                        putBoolean("eggs", eggs)
                                        putBoolean("nuts_seeds", nutsSeeds)
                                        putString("selected_persona", selectedPersona)
                                        putString("biggest_meal_time", biggestMealTime.value)
                                        putString("sleep_time", SleepTime.value)
                                        putString("wake_time", WakeTime.value)
                                        apply()
                                    }
                                    // Dismiss the dialog
                                    saveDialog = false
                                    // VALIDATION THAT THE USER HAS FILLED ALL THE REQUIRED FIELDS
                                    if ((fruits || vegetables || grains || redMeat || seafood || poultry || fish || eggs || nutsSeeds) && selectedPersona.isNotEmpty() && biggestMealTime.value.isNotEmpty() && SleepTime.value.isNotEmpty() && WakeTime.value.isNotEmpty()) {
                                        val sharedPrefs = context.getSharedPreferences(
                                            "my_prefs_$userId",
                                            android.content.Context.MODE_PRIVATE
                                        )
                                        with(sharedPrefs.edit()){
                                            putBoolean("Redirect", true)
                                        }
                                        context.startActivity(Intent(context, Dashboard::class.java))
                                    }
                                    if (!(fruits || vegetables || grains || redMeat || seafood || poultry || fish || eggs || nutsSeeds)){
                                        Checkbox_Error=true
                                        Toast.makeText(context, "Please Fill the given Categories", Toast.LENGTH_LONG).show()
                                    }
                                    else if (!(selectedPersona.isNotEmpty())){
                                        Persona_Error=true
                                        println(selectedPersona)
                                        Toast.makeText(context, "Please Fill the given Categories", Toast.LENGTH_LONG).show()
                                    }
                                    else if (!(biggestMealTime.value.isNotEmpty())){
                                        Toast.makeText(context, "Please Fill the given Categories", Toast.LENGTH_LONG).show()

                                    }
                                    else if (!(SleepTime.value.isNotEmpty())) {
                                        Toast.makeText(
                                            context,
                                            "Please Fill the given Categories",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    else if (!(WakeTime.value.isNotEmpty())) {
                                        Toast.makeText(
                                            context,
                                            "Please Fill the given Categories",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    )
                }
            }
        }
    }
}
