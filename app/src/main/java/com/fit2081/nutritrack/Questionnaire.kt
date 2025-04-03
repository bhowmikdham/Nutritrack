package com.fit2081.nutritrack

import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.OutlinedTextField
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

fun timePickerFun(context: android.content.Context,mTime: MutableState<String>): TimePickerDialog {
    // Create a Calendar instance and retrieve current hour and minute
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    // Return the TimePickerDialog
    return TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            // Update the mutable state with the chosen time
            mTime.value = "$selectedHour:$selectedMinute"
        },
        mHour,
        mMinute,
        false // 'false' means 12-hour format; set 'true' for 24-hour format
    )
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
    var saveDialog by remember { mutableStateOf(false) }

    val biggestMealTime = remember { mutableStateOf("00:00") }
    val SleepTime = remember { mutableStateOf("00:00") }
    val WakeTime = remember { mutableStateOf("00:00") }

    var selectedPersona by remember { mutableStateOf("") }
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
            HorizontalDivider(thickness = 2.dp)
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
                            onClick = { showIndulgentDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Mindful Eater", fontSize = 11.sp,color = Color.Black)
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            onClick = { showAdventurousDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)

                        ) {
                            Text("Welness Striver", fontSize = 11.sp,color = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Second row: first button right-aligned, second centered, third left-aligned.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box() {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            onClick = { showComfortDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Balance Seeker", fontSize = 11.sp,color = Color.Black)
                        }
                    }
                    Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            onClick = { showBalancedDialog = true },
                            shape = RoundedCornerShape(9.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Health Procastinator", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                    Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            onClick = { showPickyDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Text("Food Carefree", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }
            }


            // Discrete AlertDialog for Healthy Eater
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
                        Text("I’m passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.", textAlign = TextAlign.Center) }},
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
                        Text("I’m health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.", textAlign = TextAlign.Center) }},
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
                    title = { Text("Welness Striver", fontWeight = FontWeight.Bold) },
                    text = { Column(
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
                        Text("I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go.", textAlign = TextAlign.Center) }},
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
                        Text("I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.", textAlign = TextAlign.Center) }},
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
                    title = { Text("Health Procastinator", fontWeight = FontWeight.Bold) },
                    text = { Column(
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
                        Text("I’m contemplating healthy eating but it’s not a priority for me right now. I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.", textAlign = TextAlign.Center) }},
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
                        Text("I’m not bothered about healthy eating. I don’t really see the point and I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat.", textAlign = TextAlign.Center) }},
                    confirmButton = {
                        Button(onClick = { showPickyDialog = false }) {
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
                    "Picky Eater"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                    )
                    {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Text(selectedPersona)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                        {
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
                Spacer(modifier = Modifier.height(15.dp))
                Text("Timings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start){
                    Text("What time of day approx. do you normally eat your biggest meal?",fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.End){
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context,mTime = biggestMealTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = biggestMealTime.value,color = Color.Black)
                            }
                        }

                    }



                }
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally){
                    Text("What time of day approx. do you normally eat your biggest meal?", fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.End){
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context,mTime = SleepTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = SleepTime.value, color = Color.Black)
                            }
                        }

                    }




                }
                Row {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start){
                        Text("What time of day approx. do you normally eat your biggest meal?", fontSize = 13.sp)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End){
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC1FF72)),
                            shape = RoundedCornerShape(8.dp),
                            onClick = { timePickerFun(context,mTime = WakeTime).show() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Calendar Icon",
                                    tint = Color(0xFF137A44))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = WakeTime.value,color = Color.Black)
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
                    onClick = { saveDialog = true}

                ){Text("Review And Continue",fontSize = 16.sp, fontWeight = FontWeight.Bold)

                }
            if (saveDialog) {
                AlertDialog(
                    onDismissRequest = { showHealthyDialog = false },
                    title = { Text("Below Is The Information You Entered", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Categories You Can Eat")
                            if(fruits){
                                Text("Fruits")
                            }
                            if(vegetables){
                                Text("Vegetables")
                            }
                            if(grains){
                                Text("Grains")
                            }
                            if(redMeat){
                                Text("Red Meat")
                            }
                            if(seafood){
                                Text("Seafood")
                            }
                            if(poultry) {
                                Text("Poultry")
                            }
                            if(fish){
                                Text("Fish")
                            }
                            if(eggs){
                                Text("Eggs")
                            }
                            if(nutsSeeds){
                                Text("Nuts/Seeds")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Persona You Selected",fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(selectedPersona)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Timings You Selected",fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Biggest Meal Time: ${biggestMealTime.value}")
                            Text("Sleep Time: ${SleepTime.value}")
                            Text("Wake Time: ${WakeTime.value}")
                        }},
                    confirmButton = {
                        Button(onClick = { }) {
                            Text("Save")
                        }
                    }
                )
            }
            }
        }}



