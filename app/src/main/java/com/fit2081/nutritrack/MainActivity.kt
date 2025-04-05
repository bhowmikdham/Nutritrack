package com.fit2081.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.fit2081.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.nutritrack.ui.theme.White40
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerpadding ->
                    WelcomeScreen(modifier = Modifier.padding(innerpadding))
                }

                }
            }
        }
    }




@Preview(showBackground = true)
@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFC1FF72),
    ){
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
            painter = painterResource(id=R.drawable.main_logo),
            contentDescription = "Main logo",
            modifier =Modifier.size(400.dp),
            alignment = Alignment.Center,
                contentScale = ContentScale.FillBounds
        )
            Text("This app provides general health and nutrition information for educational purposes only. It is not intended as " +
                    "medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional " +
                    "before making any changes to your diet, exercise, or health regimen. " +
                    "Use this app at your own risk. " +
                    "If you’d like to an Accredited Practicing Dietitian (APD)," +
                    " please visit the Monash Nutrition/Dietetics Clinic (discounted rates for students):", color = Color(0xFF000000),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
            Text("https://www.monash.edu/medicine/scs/nu", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(40.dp))
            Button( shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F5F5)),
                onClick = {
                    context.startActivity(Intent(context, Login::class.java))
                },
            ) {Text("Login", color = Color(0xFF000000))}
            Spacer(modifier= Modifier.height(20.dp))
            Text("Designed By Bhowmik Dham(34337229)", textAlign = TextAlign.Center)
            }

    }

    }
