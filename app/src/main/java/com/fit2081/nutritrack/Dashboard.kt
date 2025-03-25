package com.fit2081.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fit2081.nutritrack.ui.theme.NutritrackTheme

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OutlinedTextFieldDropdownMenuExample(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OutlinedTextFieldDropdownMenuExample(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Wrap OutlinedTextField inside Box to make it clickable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true } // Clicking anywhere in the box will open the dropdown
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true, // Prevents manual text input
                label = { Text("Select an option") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // The DropdownMenu appears when expanded is true
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                text = { Text("Option 1") },
                onClick = {
                    selectedText = "Option 1"
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Option 2") },
                onClick = {
                    selectedText = "Option 2"
                    expanded = false
                }
            )
        }
    }
}
