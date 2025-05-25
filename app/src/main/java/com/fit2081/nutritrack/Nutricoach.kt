package com.fit2081.nutritrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.fit2081.nutritrack.data.AppDatabase
import com.fit2081.nutritrack.data.AuthManager
import com.fit2081.nutritrack.data.Entity.CoachTips
import com.fit2081.nutritrack.data.Entity.PatientHealthRecords
import com.fit2081.nutritrack.data.Repo.CoachTipsRepository
import com.fit2081.nutritrack.data.Repo.HealthRecordsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
  NutriCoach Screen Implementation

  Comprehensive nutrition coaching interface that provides personalized dietary guidance
  Integrates AI-powered recommendations with fruit nutrition database
  Implements adaptive UI based on user's health assessment scores

  Core Features:
  - Dynamic fruit nutrition lookup with external API integration
  - AI-powered personalized dietary advice generation
  - Health score-based UI adaptation
  - Comprehensive tips history management
  - Real-time nutritional information display

  Architecture Pattern:
  - MVVM architecture with reactive state management
  - Repository pattern for data access abstraction
  - Dependency injection through remember and factory patterns
  - Reactive UI updates through StateFlow and collectAsState

  Credit: Implementation follows Android Architecture Components best practices
  Reference: https://developer.android.com/topic/architecture
 */
@Composable
fun NutricoachScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userId = AuthManager.currentUserId() ?: return

    /**
      Repository Setup with Dependency Injection

      Manual dependency injection using remember for proper lifecycle management
      Repositories provide abstracted data access to multiple data sources
      Database instance cached to prevent unnecessary recreations

      Repository Pattern Benefits:
      - Separation of data access logic from UI logic
      - Testability through interface abstraction
      - Consistent data access patterns across the app
      - Centralized caching and error handling
     */
    // Setup repositories and ViewModels
    val db = remember { AppDatabase.getDatabase(context) }
    val nutriCoachRepo = remember {
        CoachTipsRepository(
            db.coachTipsDAO(),
            healthRecordsDao = db.patientHealthRecordsDAO(),
            foodIntakeDao = db.foodIntakeDAO()
        )
    }
    val healthRepo = remember { HealthRecordsRepository(db.patientHealthRecordsDAO()) }

    /**
      ViewModel Integration with Factory Pattern

      Custom factory ensures proper dependency injection for ViewModel
      viewModel() delegate handles lifecycle management automatically
      Factory pattern enables testing with mock repositories
     */
    val viewModel: NutriCoachViewModel = viewModel(
        factory = NutriCoachViewModelFactory.create(nutriCoachRepo, userId)
    )

    /**
      Health Record Integration with Reactive Data Flow

      Complex data transformation pipeline using Flow operators
      filterNotNull ensures only valid records are processed
      map operator transforms list to single record for current user
      collectAsState provides reactive UI updates

      Flow Operations:
      1. Filter out null values from database queries
      2. Map list results to first (current) record
      3. Provide fallback for missing records
      4. React to database changes automatically
     */
    // Get user's health record to check fruit score
    val healthRecord by healthRepo.recordFor(userId)
        .map { it.firstOrNull() }
        .filterNotNull()
        .collectAsState(initial = null)

    val uiState by viewModel.uiState.collectAsState()
    val userTips by viewModel.userTips.collectAsState()

    /**
      Dynamic Fruit Score Calculation

      Gender-specific health scoring based on HEIFA (Healthy Eating Index for Australians)
      Different scoring algorithms for male and female dietary requirements
      Fallback to neutral score when health record unavailable

      HEIFA Scoring System:
      - Scale: 0-10 points for most food groups
      - Gender-specific recommendations based on Australian Dietary Guidelines
      - Optimal score: 10 points per category

     Credit: HEIFA scoring system implementation
     Reference: Australian Government Department of Health dietary guidelines
     */
    // Calculate fruit score
    val fruitScore = healthRecord?.let { record ->
        if (record.sex.equals("Male", ignoreCase = true)) {
            record.fruitHeifaScoreMale.toInt()
        } else {
            record.fruitHeifaScoreFemale.toInt()
        }
    } ?: 0

    /**
     Adaptive UI Logic Based on Health Assessment

     Dynamic content display based on user's nutritional assessment
     Shows improvement areas only when scores indicate need for intervention
     Optimal scores trigger congratulatory content instead of improvement suggestions
     */
    // Show fruit section only if score is not optimal (< 10)
    val showFruitSection = fruitScore < 10

    /**
     Main UI Layout with LazyColumn

     Efficient scrolling implementation using LazyColumn for memory optimization
     Items are composed only when visible, improving performance
     Consistent spacing and alignment throughout the interface

     Layout Structure:
     1. Title section with app branding
     2. Conditional fruit information or optimal score display
     3. AI advice generation and display
     4. Tips history access button

      Credit: LazyColumn implementation follows Compose performance guidelines
      Reference: https://developer.android.com/jetpack/compose/lists
     */
    // Main LazyColumn for the entire screen
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                "NutriCoach",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Fruit Section or Optimal Score Image
        item {
            if (showFruitSection) {
                FruitInformationCard(
                    viewModel = viewModel,
                    uiState = uiState
                )
            } else {
                OptimalScoreCard()
            }
        }

        // AI Section
        item {
            AIAdviceCard(
                viewModel = viewModel,
                uiState = uiState,
                fruitScore = fruitScore,
                healthRecord = healthRecord
            )
        }

        // Show All Tips Button
        item {
            Button(
                onClick = { viewModel.toggleShowAllTips() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                enabled = userTips.isNotEmpty()
            ) {
                Text(
                    text = if (userTips.isEmpty()) {
                        "No Tips Yet"
                    } else {
                        "Show All Tips (${userTips.size})"
                    }
                )
            }
        }
    }

    /**
     * Modal Dialog for Tips History

     Full-screen dialog displaying comprehensive tips history
     Conditional rendering based on UI state and data availability
     Proper dialog lifecycle management through compose state
     */
    // All Tips Dialog
    if (uiState.showAllTips && userTips.isNotEmpty()) {
        AllTipsDialog(
            tips = userTips,
            onDismiss = { viewModel.toggleShowAllTips() }
        )
    }
}

/**
  Fruit Information Card Component

 Interactive fruit nutrition lookup interface with search functionality
 Integrates with external fruit nutrition API for real-time data
 Provides comprehensive nutritional information display

  Features:
  - Real-time fruit search with API integration
  - Comprehensive nutritional data display
  - Loading and error state management
  - Responsive layout with proper scrolling

 API Integration:
  - External fruit nutrition database
  - Error handling for network issues
  - Loading states for better UX
  - Search result caching through ViewModel

 Credit: Search functionality implementation follows Android networking best practices
 Reference: https://developer.android.com/training/volley
 */
@Composable
private fun FruitInformationCard(
    viewModel: NutriCoachViewModel,
    uiState: NutriCoachUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Fruit Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * Search Input with State Management
             *
             * Remember pattern for local state management with ViewModel integration
             * Keyboard actions provide seamless search experience
             * IME (Input Method Editor) integration for better mobile UX
             *
             * State Management:
             * - Local state for immediate UI responsiveness
             * - ViewModel state for search persistence
             * - Proper state restoration on configuration changes
             */
            // Search field
            var searchText by remember(uiState.lastSearchQuery) {
                mutableStateOf(uiState.lastSearchQuery)
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Fruit Name") },
                placeholder = { Text("banana") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.searchFruit(searchText) },
                        enabled = searchText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchText.isNotBlank()) {
                            viewModel.searchFruit(searchText)
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
              Results Display with Multiple State Handling

              Comprehensive state management for loading, error, success, and empty states
              LazyColumn for efficient display of nutritional data
              Proper error messaging for user guidance

              State Patterns:
              - Loading: CircularProgressIndicator with proper positioning
              - Error: User-friendly error messages with retry guidance
              - Success: Structured nutritional data display
              - Empty: Instructional placeholder text
             */
            // Results in a LazyColumn
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    uiState.isLoadingFruit -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                    uiState.fruitError != null -> {
                        Text(
                            text = uiState.fruitError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    uiState.currentFruit != null -> {
                        val fruit = uiState.currentFruit
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = fruit.name.replaceFirstChar { it.uppercase() },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            /**
                             * Nutritional Data Structure
                             *
                             * Organized display of comprehensive nutritional information
                             * Structured data mapping for consistent presentation
                             * Proper units and formatting for professional appearance
                             */
                            val nutritionData = listOf(
                                "Family" to fruit.family,
                                "Calories" to "${fruit.nutritions.calories}",
                                "Fat" to "${fruit.nutritions.fat} g",
                                "Sugar" to "${fruit.nutritions.sugar} g",
                                "Carbohydrates" to "${fruit.nutritions.carbohydrates} g",
                                "Protein" to "${fruit.nutritions.protein} g"
                            )

                            items(nutritionData) { (label, value) ->
                                NutritionRow(label, value)
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "Search for a fruit to see its nutritional information",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
  Optimal Score Congratulatory Card

  Dynamic content display for users with optimal nutritional scores
  Uses random image generation for visual appeal and engagement
  Celebrates user achievement with positive reinforcement

  UX Design:
 - Positive visual feedback for achievement
  - Random images prevent interface staleness
  - Clear messaging about optimal status
  - Consistent card styling with rest of app

  Credit: Random image integration using Picsum API
  Reference: https://picsum.photos/
 */
@Composable
private fun OptimalScoreCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://picsum.photos/400/300?random=${System.currentTimeMillis()}",
                contentDescription = "Healthy lifestyle",
                modifier = Modifier.fillMaxSize()
            )
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Congratulations! Your fruit intake is optimal!",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
  AI Advice Card Component

  Advanced AI-powered dietary advice generation interface
  Integrates user health data for personalized recommendations
  Provides comprehensive tip generation and display functionality

  AI Integration Features:
  - Health record analysis for personalized advice
  - Fruit score consideration in recommendations
  - Comprehensive tip generation based on user profile
  - Loading states and error handling for AI operations

 * Data Processing:
  - User health metrics analysis
  - Dietary pattern recognition
  - Personalized recommendation algorithms
  - Historical tip tracking and storage

  Credit: AI integration follows machine learning best practices
  Reference: https://developer.android.com/ml
 API integration was done using Android Documentation  & Medium Article: https://medium.com/@bhoomigadhiya/integrating-googles-gemini-into-the-android-app-520508975c2e
 */
@Composable
private fun AIAdviceCard(
    viewModel: NutriCoachViewModel,
    uiState: NutriCoachUiState,
    fruitScore: Int,
    healthRecord: PatientHealthRecords?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Personalized Dietary Advice (AI)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
              AI Advice Generation Button

             Triggers comprehensive analysis of user health data
             Passes complete health profile for personalized recommendations
             Loading state prevents multiple simultaneous requests
             Disabled state during processing for better UX
             */
            // Generate button
            Button(
                onClick = {
                    viewModel.generateMotivationalTip(fruitScore, healthRecord.toString())
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                enabled = !uiState.isLoadingAI
            ) {
                if (uiState.isLoadingAI) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Get Personalized Dietary Advice")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            /**
              AI Response Display Area

             Multi-state display area for AI-generated content
             Handles loading, success, error, and empty states
             Proper formatting and styling for generated content

             Content States:
              - Generated tip: Display formatted advice text
              - Error state: User-friendly error messages
              - Empty state: Instructional placeholder
              - Loading state: Progress indicator
             */
            // Display current tip
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.currentAITip.isNotEmpty() -> {
                            Text(
                                text = uiState.currentAITip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.aiError != null -> {
                            Text(
                                text = uiState.aiError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                        else -> {
                            Text(
                                text = "Click the button above to get personalized dietary advice based on your complete health profile",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
  All Tips History Dialog

 Comprehensive modal dialog for displaying complete tips history
 Full-height dialog with scrollable content for large datasets
  Proper dialog lifecycle management and user interaction handling

   Features:
   - Full-screen modal presentation
   - Scrollable tips history with timestamps
   - Individual tip cards with metadata
   - Proper dismiss handling and navigation

   Dialog Design:
   - Material 3 dialog styling
   - Rounded corners for modern appearance
   - Consistent card styling throughout
   - Proper spacing and typography hierarchy

   Credit: Dialog implementation follows Material Design guidelines
   Reference: https://m3.material.io/components/dialogs/overview
 */
@Composable
private fun AllTipsDialog(
    tips: List<CoachTips>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                /**
                   Dialog Header with Tips Count

                   Clear header section with title and count information
                   Provides context about the amount of historical data
                   Consistent typography and spacing
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Dietary Tips History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${tips.size} tips",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                /**
                   Scrollable Tips History List

                   LazyColumn for efficient rendering of large tip datasets
                   Individual cards for each tip with metadata
                   Proper spacing and visual hierarchy
                   Timestamp formatting for historical context
                 */
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tips) { tip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = tip.tip,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generated: ${formatTimestamp(tip.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                /**
                   Dialog Action Button

                   Clear dismissal action for dialog closure
                   Consistent button styling with app theme
                   Proper touch target sizing and accessibility
                 */
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
   Nutrition Data Row Component

   Reusable component for displaying nutritional information pairs
   Consistent formatting for label-value relationships
   Proper alignment and spacing for professional appearance

   Design Pattern:
   - Label-value pair presentation
   - Consistent typography hierarchy
   - Proper spacing and alignment
   - Reusable across different nutritional contexts
 */
@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

/**
   Timestamp Formatting Utility

   Consistent date and time formatting throughout the application
   Locale-aware formatting for international compatibility
   Human-readable format for better user experience

   Formatting Pattern:
   - Month abbreviation for compact display
   - Day and year for complete context
   - 24-hour time format for precision
   - Locale-specific formatting

   Credit: Date formatting follows Android internationalization guidelines
   Reference: https://developer.android.com/guide/topics/resources/localization
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}