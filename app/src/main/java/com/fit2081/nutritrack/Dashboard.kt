package com.fit2081.nutritrack

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.fit2081.nutritrack.ui.theme.White40

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritrackTheme {
                HomePage()

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomePage() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val user = sharedPrefs.getString( "user_id" , "")
    Scaffold (containerColor= Color.White){
        innerPadding->
        Column(
            modifier=Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text("Hello,", fontSize = 25.sp, fontWeight = FontWeight.Medium, color=Color.Gray)
            Text("$user", fontSize = 40.sp,color=Color.Black, fontWeight = FontWeight.ExtraBold)
            Image(
                painter = painterResource(id=R.drawable.plate),
                contentDescription = "plate",
                modifier =Modifier.size(360.dp),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillWidth
            )
           Row(

           ){Text("My Score", fontSize = 30.sp, fontWeight = FontWeight.Bold)
           Box (
               modifier = Modifier.weight(1f),
               contentAlignment = Alignment.CenterEnd
           ) {
               Button(modifier = Modifier
               .height(35.dp),
               onClick = {context.startActivity(Intent(context,Insights::class.java))},
                colors = ButtonDefaults.buttonColors(Color.White)
               ){Text("See all Scores", color = Color.Gray)}}}
            Row(

            ){
                Box (
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.greenarrow),
                        contentDescription = "Green Arrow",
                        modifier = Modifier.size(130.dp),
                    )
                }

            }
            Text("What is The Food Quality Score?", fontSize = 15.sp ,fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.", fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("This personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.", fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp)

        }
        }
}
