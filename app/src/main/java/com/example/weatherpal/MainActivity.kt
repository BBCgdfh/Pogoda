package com.example.weatherpal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var weather by remember { mutableStateOf<Weather?>(null) }
    var forced by remember { mutableStateOf<Sky?>(null) } // тестовый выбор погоды
    var playing by remember { mutableStateOf(false) }

    // TODO: брать координаты из GPS. Пока Москва.
    LaunchedEffect(Unit) {
        weather = try { loadWeather(55.75, 37.62) } catch (e: Exception) { null }
    }

    val sky = forced ?: weather?.sky ?: Sky.CLEAR
    val colors = when (sky) {
        Sky.SNOW -> listOf(Color(0xFF1B2A6B), Color(0xFF3F51B5))
        Sky.RAIN, Sky.STORM -> listOf(Color(0xFF263238), Color(0xFF546E7A))
        Sky.CLEAR -> listOf(Color(0xFF29B6F6), Color(0xFFFFE082))
        else -> listOf(Color(0xFF607D8B), Color(0xFFB0BEC5))
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors))) {
        FallingFx(sky)
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(weather?.let { "${it.temp}°" } ?: "…", fontSize = 64.sp, color = Color.White)
            Text(skyTitle(sky), fontSize = 20.sp, color = Color.White)
            Spacer(Modifier.weight(1f))
            Buddy(sky)
            Text(phrase(sky), color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Button(onClick = { playing = true }, enabled = sky == Sky.SNOW) {
                Text(if (sky == Sky.SNOW) "Играть в снежки" else "Игра для этой погоды скоро")
            }
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Sky.values().forEach { s ->
                    TextButton(onClick = { forced = s }) { Text(skyTitle(s), color = Color.White) }
                }
            }
        }
        if (playing) SnowballGame { playing = false }
    }
}
