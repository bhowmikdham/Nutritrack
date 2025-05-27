package com.fit2081.nutritrack

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.nutritrack.Feature.FoodintakeState
import com.fit2081.nutritrack.Feature.QuestionnaireViewModel
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.Repo.IntakeRepository
import com.fit2081.nutritrack.navigation.Screen
import java.util.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fit2081.nutritrack.data.AuthManager.currentUserId

/**
 * Material Icons Mapping for Food Categories
 *
 * Implementation taken from Material Design Guidelines and adapted for NutriTrack
 * Help of ChatGPT for production of certain code was taken
 * Maps food categories to appropriate Material Design Icons for consistent UI
 */

/**
 * Enhanced Persona Selection with Animation
 *
 * Animation implementation adapted from Course Week 11 examples
 * Uses spring physics for smooth expand/collapse animations on persona cards
 */
@Composable
fun AnimatedPersonaSelectionSection(
    selectedPersona: String,
    onPersonaChange: (String) -> Unit
) {
    var expandedPersona by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Persona",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "People can be broadly classified into 6 different types based on their eating performance. Click on each picture below to find out the different types, and select the type that best fits you",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enhancedPersonaList) { persona ->
                    AnimatedPersonaCard(
                        persona = persona,
                        isSelected = selectedPersona == persona.name,
                        isExpanded = expandedPersona == persona.name,
                        onSelect = {
                            onPersonaChange(persona.name)
                            // Collapse when selecting
                            if (expandedPersona == persona.name) {
                                expandedPersona = null
                            }
                        },
                        onToggleExpand = {
                            expandedPersona = if (expandedPersona == persona.name) null else persona.name
                        }
                    )
                }
            }
        }
    }
}

/**
 * Animated Persona Card with Size Animation
 *
 * Implementation adapted from Week 11 SizeAnimationDemo
 * Demonstrates smooth size transitions using spring physics for card expansion
 * credit to:
 * https://developer.android.com/develop/ui/compose/animation/quick-guide
 * https://medium.com/@acceldia/jetpack-compose-creating-expandable-cards-with-content-9ea1eae09efe
 * Help of Chat Gpt was taken for figuring out the approach & code for certain following parts
 */
@Composable
private fun AnimatedPersonaCard(
    persona: EnhancedPersonaInfo,
    isSelected: Boolean,
    isExpanded: Boolean,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit
) {
    // Animate card width with spring physics (from Week 11 concepts)
    val cardWidth by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // Controls bounciness
            stiffness = Spring.StiffnessLow // Controls animation speed
        ),
        label = "cardWidthAnimation"
    )

    Card(
        modifier = Modifier
            .width(cardWidth)
            .animateContentSize( // Smooth content size transitions
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFFC1FF72)
                isExpanded -> Color(0xFFE8F5E8)
                else -> Color(0xFFF5F5F5)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFF137A44))
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected || isExpanded) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header section (always visible)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile image and name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        // Use Material Icon instead of Image
                        Box(
                            modifier = Modifier
                                .size(if (isExpanded) 50.dp else 40.dp)
                                .background(
                                    color = if (isSelected) Color(0xFFC1FF72) else Color(0xFFE8F5E8),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) Color(0xFF137A44) else Color.Gray,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = persona.name,
                                modifier = Modifier.size(if (isExpanded) 30.dp else 24.dp),
                                tint = if (isSelected) Color(0xFF137A44) else Color.Gray
                            )
                        }

                        // Selection indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color(0xFF137A44), CircleShape)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = persona.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF137A44) else Color.Black
                        )
                    }
                }

                // Expand/collapse indicator
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Name when not expanded
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = persona.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) Color(0xFF137A44) else Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Expanded content (description)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Description",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF137A44)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = persona.fullDescription,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select button
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF137A44) else Color(0xFFC1FF72),
                        contentColor = if (isSelected) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isSelected) "Selected" else "Select This",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Enhanced data class with Material Icons instead of image resources
data class EnhancedPersonaInfo(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val shortDescription: String,
    val fullDescription: String
)

/**
 * Persona Data Configuration
 *
 * Static data defining the 6 personality types for eating habits
 * Uses Material Design Icons for consistent visual representation
 *
 */
private val enhancedPersonaList = listOf(
    EnhancedPersonaInfo(
        name = "Health Devotee",
        icon = Icons.Filled.FitnessCenter,
        shortDescription = "I'm passionate about healthy eating",
        fullDescription = "I'm passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy."
    ),
    EnhancedPersonaInfo(
        name = "Mindful Eater",
        icon = Icons.Filled.SelfImprovement,
        shortDescription = "Mindful eating helps you focus",
        fullDescription = "I'm health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media."
    ),
    EnhancedPersonaInfo(
        name = "Wellness Striver",
        icon = Icons.Filled.TrendingUp,
        shortDescription = "I aspire to be healthy",
        fullDescription = "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I've tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I'll give it a go."
    ),
    EnhancedPersonaInfo(
        name = "Balanced Seeker",
        icon = Icons.Filled.Balance,
        shortDescription = "I try to live balanced",
        fullDescription = "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn't have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips."
    ),
    EnhancedPersonaInfo(
        name = "Health Procrastinator",
        icon = Icons.Filled.Schedule,
        shortDescription = "I'm contemplating healthy eating",
        fullDescription = "I'm contemplating healthy eating but it's not a priority for me right now. I know the basics about what it means to be healthy, but it doesn't seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life."
    ),
    EnhancedPersonaInfo(
        name = "Food Carefree",
        icon = Icons.Filled.LocalDining,
        shortDescription = "I'm not bothered about healthy eating",
        fullDescription = "I'm not bothered about healthy eating. I don't really see the point and I don't think about it. I don't really notice healthy eating tips or recipes and I don't care what I eat."
    )
)

/**
 * Main Questionnaire Activity
 *
 * Implements Android Activity lifecycle and Material 3 design patterns
 * Handles user authentication and navigation flow for the questionnaire process
 * credit : https://medium.com/androiddevelopers/dependency-injection-in-compose-a2db897e6f11
 */
class Questionnaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual DI following Android Architecture
        val db = AppDatabase.getDatabase(this)
        val intakeRepo = IntakeRepository(db.foodIntakeDAO())
        val vm = QuestionnaireViewModel(intakeRepo)

        /**
         * User Authentication Check
         *
         * Verifies current user session before proceeding with questionnaire
         * Redirects to login if no valid user session found
         */
        val patientId = currentUserId() ?: run {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                QuestionnaireScreen(
                    patientId = patientId,
                    navController = navController,
                    vm = vm,
                    onComplete = {
                        // Navigate to dashboard after completion
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

/**
 * Main Questionnaire Screen Composable
 *
 * Depicts the entire questionnaire flow with Material 3 design
 * Manages state collection and dialog presentations for user interactions
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    patientId: String,
    navController: NavHostController,
    vm: QuestionnaireViewModel,
    onComplete: () -> Unit
) {
    //use of chat GPT was taken to adapt to the state management
    val context = LocalContext.current
    LaunchedEffect(patientId) { vm.loadResponse(patientId) }
    val state by vm.state.collectAsState()

    var showCancel by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }

    /**
     * Discard Changes Alert Dialog
     *
     * Material 3 AlertDialog implementation for confirming questionnaire abandonment
     * Provides user with clear options to either discard changes or continue editing
     */
    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            title = { Text("Discard Changes?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to discard all your changes?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancel = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    /**
     * Summary Review Dialog
     *
     * Final review step before submitting questionnaire data
     * Allows user to review all selections and either confirm or go back to edit
     */
    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = { Text("Review Your Answers", fontWeight = FontWeight.Bold) },
            text = { SummaryContent(state) },
            confirmButton = {
                TextButton(onClick = {
                    showSummary = false
                    vm.saveResponse(patientId)
                    onComplete()
                }) {
                    Text("Confirm & Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummary = false }) {
                    Text("Edit")
                }
            }
        )
    }

    // Main content with Material 3 styling
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Intake Questionnaire", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { showCancel = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC1FF72),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F5F5)
    ) { paddingValues ->
        /**
         *
         * Main implementation of everything in the Questionnaire
         */
        //help of GenAi was taken to incorporate LazyColumns, Help of LAB material showcasing LazyColumns was also taken to ensure a proper implementation
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Instructions card
            item { InstructionsCard() }

            // Food categories section
            item { FoodCategoriesSection(state, vm::onFoodToggle) }

            // Persona selection section
            item { AnimatedPersonaSelectionSection(state.persona, vm::onPersonaChange) }

            // Timing section
            item { TimingSection(vm, state) }

            // Continue button
            item { ContinueButton(state, context, showSummary = { showSummary = true }) }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun InstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Please fill all the food categories you can eat",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Food Categories Selection Section
 *
 * Implements Material 3 design for food category selection
 * Uses grid layout with visual feedback for selected categories
 * Credit :
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodCategoriesSection(
    state: FoodintakeState,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Food Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val foodCategories = listOf(
                FoodCategory("vegetables", "Vegetables", Icons.Filled.Grass, state.vegetables),
                FoodCategory("redMeat", "Red Meat", Icons.Filled.Restaurant, state.redMeat),
                FoodCategory("fish", "Fish", Icons.Filled.Sailing, state.fish),
                FoodCategory("fruits", "Fruits", Icons.Filled.ChildCare, state.fruits),
                FoodCategory("seafood", "Seafood", Icons.Filled.WaterDrop, state.seafood),
                FoodCategory("eggs", "Eggs", Icons.Filled.Circle, state.eggs),
                FoodCategory("grains", "Grains", Icons.Filled.Grain, state.grains),
                FoodCategory("poultry", "Poultry", Icons.Filled.Pets, state.poultry),
                FoodCategory("nutsSeeds", "Nuts & Seeds", Icons.Filled.Nature, state.nutsSeeds)
            )

            // Grid layout with 3 columns
            foodCategories.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { category ->
                        FoodCategoryItem(
                            category = category,
                            onToggle = { onToggle(category.key, !category.isSelected) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining slots if row is not complete
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 *
 *
 * Implementaiton of the following was Generated Using GenAi by Prompt engineering and providing the approach to the problem ,
 * further adapted by physical coding.
 *
 */
@Composable
private fun FoodCategoryItem(
    category: FoodCategory,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(4.dp)
            .clickable { onToggle() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = if (category.isSelected) Color(0xFFC1FF72) else Color(0xFFF5F5F5),
                    shape = CircleShape
                )
                .border(
                    width = if (category.isSelected) 2.dp else 1.dp,
                    color = if (category.isSelected) Color(0xFF137A44) else Color.Gray,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.label,
                modifier = Modifier.size(32.dp),
                tint = if (category.isSelected) Color(0xFF137A44) else Color.Gray
            )

            // Checkmark overlay when selected
            if (category.isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF137A44), CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = category.label,
            fontSize = 12.sp,
            fontWeight = if (category.isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = if (category.isSelected) Color(0xFF137A44) else Color.Black
        )
    }
}

//OLD APPROACH
//private fun PersonaSelectionSection(
//    selectedPersona: String,
//    onPersonaChange: (String) -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Your Persona",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            Text(
//                text = "People can be broadly classified into 6 different types based on their eating performance. Click on each picture below to find out the different types, and select the type that best fits you",
//                fontSize = 12.sp,
//                color = Color.Gray,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            LazyRow(
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(personaList) { persona ->
//                    PersonaCard(
//                        persona = persona,
//                        isSelected = selectedPersona == persona.name,
//                        onSelect = { onPersonaChange(persona.name) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//private fun PersonaCard(
//    persona: PersonaInfo,
//    isSelected: Boolean,
//    onSelect: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .width(120.dp)
//            .clickable { onSelect() },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) Color(0xFFC1FF72) else Color(0xFFF5F5F5)
//        ),
//        border = if (isSelected) {
//            BorderStroke(2.dp, Color(0xFF137A44))
//        } else null,
//        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.padding(12.dp)
//        ) {
//            Image(
//                painter = painterResource(id = persona.imageRes),
//                contentDescription = persona.name,
//                modifier = Modifier
//                    .size(60.dp)
//                    .clip(CircleShape)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = persona.name,
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Medium,
//                textAlign = TextAlign.Center,
//                color = if (isSelected) Color(0xFF137A44) else Color.Black
//            )
//        }
//    }
//}

/**
 * Timing Configuration Section
 *
 * Implements time picker dialogs with comprehensive validation
 * Provides real-time feedback for time conflicts and validation rules
 *
 *These define the rules that are needed for a proper time picking
 * Help of GenAi was taken for figuring out the approach
 */
@Composable
private fun TimingSection(
    vm: QuestionnaireViewModel,
    state: FoodintakeState
) {
    val context = LocalContext.current

    // Validate times and get error message
    val validationResult = remember(state.biggestMealTime, state.sleepTime, state.wakeTime) {
        validateTimes(state.biggestMealTime, state.sleepTime, state.wakeTime)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Timings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Validation rules explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Time Rules:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF137A44)
                    )
                    Text(
                        text = "• All times must be different",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "• Sleep duration must be at least 4 hours",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "• Meal time cannot be during sleep hours",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val timeFields = listOf(
                TimeField("biggest_meal_time", "Biggest Meal Time", state.biggestMealTime),
                TimeField("sleep_time", "Sleep Time", state.sleepTime),
                TimeField("wake_time", "Wake Time", state.wakeTime)
            )

            timeFields.forEach { field ->
                TimePickerRow(
                    field = field,
                    onTimeSelected = { time -> vm.onTimeChange(field.key, time) },
                    context = context,
                    hasError = !validationResult.isValid
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            /**
             * Real-time Validation Feedback
             *
             * Displays validation errors or success messages based on current time configuration
             * Provides immediate feedback to help users understand and correct time conflicts
             */
            if (!validationResult.isValid && validationResult.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validationResult.errorMessage,
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (validationResult.isValid && state.biggestMealTime.isNotEmpty() &&
                state.sleepTime.isNotEmpty() && state.wakeTime.isNotEmpty()) {
                // Show success message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = Color(0xFF137A44),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All times are valid!",
                            fontSize = 12.sp,
                            color = Color(0xFF137A44),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Time Picker Row Component
 *
 * Individual time selection component with Android TimePickerDialog integration
 * Provides visual error states and clear time display with picker functionality
 */
@Composable
private fun TimePickerRow(
    field: TimeField,
    onTimeSelected: (String) -> Unit,
    context: android.content.Context,
    hasError: Boolean = false
) {
    val calendar = Calendar.getInstance()

    /**
     * Time Picker Dialog Handler
     *
     * Launches Android's native TimePickerDialog when user taps on time field
     * Handles time formatting and callback to parent component
     * Taken from A1 , and modified it a bit to adapt the current file
     */
    fun showTimePicker() {
        val parts = field.value.split(':')
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, h, m -> onTimeSelected("%02d:%02d".format(h, m)) },
            hour,
            minute,
            true
        ).show()
    }
   // taken from Gen AI
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasError) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
        ),
        border = if (hasError) {
            BorderStroke(1.dp, Color(0xFFD32F2F))
        } else {
            BorderStroke(1.dp, Color.Gray)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = field.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasError) Color(0xFFD32F2F) else Color.Black
                )
                if (field.value.isNotEmpty()) {
                    Text(
                        text = "Current: ${field.value}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasError && field.value.isNotEmpty()) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Invalid time",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = if (field.value.isNotEmpty()) field.value else "00:00",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        hasError && field.value.isNotEmpty() -> Color(0xFFD32F2F)
                        field.value.isNotEmpty() -> Color.Black
                        else -> Color.Gray
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "Select time",
                    tint = if (hasError) Color(0xFFD32F2F) else Color.Gray
                )
            }
        }
    }
}

/**
 * Continue Button with Comprehensive Validation
 *
 * Handles form submission with multiple validation checks
 * Provides specific error messages for different validation failures
 * When validation passes, shows summary dialog for final review
 */
@Composable
private fun ContinueButton(
    state: FoodintakeState,
    context: android.content.Context,
    showSummary: () -> Unit
) {
    val isFormValid = isFormValid(state)
    val timeValidation = validateTimes(state.biggestMealTime, state.sleepTime, state.wakeTime)

    // Determine what's missing for better error messages
    val anyFood = listOf(
        state.fruits, state.vegetables, state.grains,
        state.redMeat, state.seafood, state.poultry,
        state.fish, state.eggs, state.nutsSeeds
    ).any { it }

    Button(
        onClick = {
            when {
                !anyFood -> {
                    Toast.makeText(context, "Please select at least one food category", Toast.LENGTH_SHORT).show()
                }
                state.persona.isEmpty() -> {
                    Toast.makeText(context, "Please select your persona", Toast.LENGTH_SHORT).show()
                }
                state.biggestMealTime.isEmpty() || state.sleepTime.isEmpty() || state.wakeTime.isEmpty() -> {
                    Toast.makeText(context, "Please set all time fields", Toast.LENGTH_SHORT).show()
                }
                !timeValidation.isValid -> {
                    Toast.makeText(context, timeValidation.errorMessage ?: "Invalid time configuration", Toast.LENGTH_LONG).show()
                }
                else -> {
                    showSummary()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFormValid) Color(0xFF137A44) else Color.Gray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isFormValid) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                "Review And Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryContent(state: FoodintakeState) {
    Column {
        Text("Selected Categories:", fontWeight = FontWeight.Bold)
        val selectedFoods = listOf(
            "Fruits" to state.fruits,
            "Vegetables" to state.vegetables,
            "Grains" to state.grains,
            "Red Meat" to state.redMeat,
            "Seafood" to state.seafood,
            "Poultry" to state.poultry,
            "Fish" to state.fish,
            "Eggs" to state.eggs,
            "Nuts & Seeds" to state.nutsSeeds
        ).filter { it.second }.map { it.first }

        Text(selectedFoods.joinToString(", "))

        Spacer(Modifier.height(8.dp))
        Text("Persona:", fontWeight = FontWeight.Bold)
        Text(state.persona)

        Spacer(Modifier.height(8.dp))
        Text("Timings:", fontWeight = FontWeight.Bold)
        Text("Biggest Meal: ${state.biggestMealTime}")
        Text("Sleep: ${state.sleepTime}")
        Text("Wake: ${state.wakeTime}")
    }
}

// Data classes for better organization following Kotlin best practices
data class FoodCategory(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSelected: Boolean
)

data class PersonaInfo(
    val name: String,
    val imageRes: Int,
    val description: String
)

data class TimeField(
    val key: String,
    val label: String,
    val value: String
)
/**
// Pictures Mapped to the Pictures ,we are not using it right now but we can actually implement this by just removing the animation cards and folllowing the old approach
private val personaList = listOf(
    PersonaInfo("Health Devotee", R.drawable.persona_1, "I'm passionate about healthy eating"),
    PersonaInfo("Mindful Eater", R.drawable.persona_2, "Mindful eating helps you focus"),
    PersonaInfo("Wellness Striver", R.drawable.persona_3, "I aspire to be healthy"),
    PersonaInfo("Balanced Seeker", R.drawable.persona_4, "I try to live balanced"),
    PersonaInfo("Health Procrastinator", R.drawable.persona_5, "I'm contemplating healthy eating"),
    PersonaInfo("Food Carefree", R.drawable.persona_6, "I'm not bothered about healthy eating")
)
**/

/**
 * Form Validation Logic
 *
 * Comprehensive validation function that checks all required form fields
 * Ensures at least one food category is selected, persona is chosen, and all times are valid
 */
private fun isFormValid(state: FoodintakeState): Boolean {
    val anyFood = listOf(
        state.fruits, state.vegetables, state.grains,
        state.redMeat, state.seafood, state.poultry,
        state.fish, state.eggs, state.nutsSeeds
    ).any { it }

    val timesValid = validateTimes(
        biggestMealTime = state.biggestMealTime,
        sleepTime = state.sleepTime,
        wakeTime = state.wakeTime
    ).isValid

    return anyFood && state.persona.isNotEmpty() &&
            state.biggestMealTime.isNotEmpty() &&
            state.sleepTime.isNotEmpty() &&
            state.wakeTime.isNotEmpty() &&
            timesValid
}

/**
 * Time Validation System
 *
 * Custom validation logic for time constraints in the questionnaire
 * Implements business rules for meal timing, sleep duration, and conflict detection
 */
data class TimeValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Main Time Validation Function
 *
 * Validates time inputs according to business rules:
 * 1. All times must be different
 * 2. Sleep duration must be at least 4 hours
 * 3. Meal time cannot occur during sleep hours
 */
private fun validateTimes(
    biggestMealTime: String,
    sleepTime: String,
    wakeTime: String
): TimeValidationResult {
    if (biggestMealTime.isEmpty() || sleepTime.isEmpty() || wakeTime.isEmpty()) {
        return TimeValidationResult(false, "All times must be set")
    }

    try {
        // Convert times to minutes for easier comparison
        val mealMinutes = timeToMinutes(biggestMealTime)
        val sleepMinutes = timeToMinutes(sleepTime)
        val wakeMinutes = timeToMinutes(wakeTime)

        // Rule 1: No duplicate times
        if (mealMinutes == sleepMinutes || mealMinutes == wakeMinutes || sleepMinutes == wakeMinutes) {
            return TimeValidationResult(false, "All times must be different")
        }

        // Rule 2: Sleep and wake time must have at least 4 hours difference
        val sleepDuration = calculateSleepDuration(sleepMinutes, wakeMinutes)
        if (sleepDuration < 240) { // 4 hours = 240 minutes
            return TimeValidationResult(false, "Sleep duration must be at least 4 hours")
        }

        // Rule 3: Biggest meal time cannot be during sleep period
        if (isTimeDuringSleep(mealMinutes, sleepMinutes, wakeMinutes)) {
            return TimeValidationResult(false, "Meal time cannot be during sleep hours")
        }

        return TimeValidationResult(true)

    } catch (e: Exception) {
        return TimeValidationResult(false, "Invalid time format")
    }
}

/**
 * Time Utility Functions
 *
 * Helper functions for time calculations and validations
 * Handles conversion between time formats and business logic calculations
 */

// Converting HH:MM time string to minutes since midnight
private fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    val hours = parts[0].toInt()
    val minutes = parts[1].toInt()
    return hours * 60 + minutes
}

// Calculate sleep duration considering overnight sleep
private fun calculateSleepDuration(sleepMinutes: Int, wakeMinutes: Int): Int {
    return if (wakeMinutes > sleepMinutes) {
        //We need ot make sure of two cases here
        // Same day sleep
        wakeMinutes - sleepMinutes
    } else {
        // Overnight sleep
        (24 * 60 - sleepMinutes) + wakeMinutes
    }
}

// Check if meal time falls during sleep period
private fun isTimeDuringSleep(mealMinutes: Int, sleepMinutes: Int, wakeMinutes: Int): Boolean {
    return if (wakeMinutes > sleepMinutes) {
        // Same day sleep - meal should not be between sleep and wake
        mealMinutes > sleepMinutes && mealMinutes < wakeMinutes
    } else {
        // Overnight sleep - meal should not be after sleep OR before wake
        mealMinutes > sleepMinutes || mealMinutes < wakeMinutes
    }
}