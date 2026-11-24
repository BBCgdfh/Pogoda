package com.example.weatherpal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.random.Random

private class Ball(var x: Float, var y: Float, val speed: Float)

private const val MAX_MISSES = 5

/** Мини-игра для снега: сбивай летящие снежки тапом. 5 промахов — конец. */
@Composable
fun SnowballGame(onClose: () -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var tick by remember { mutableLongStateOf(0L) }
    val balls = remember { mutableListOf<Ball>() }

    LaunchedEffect(round) {
        balls.clear()
        var last = 0L
        var spawn = 0f
        while (true) {
            withFrameNanos { t ->
                val dt = if (last == 0L) 0f else (t - last) / 1_000_000_000f
                last = t
                if (misses < MAX_MISSES) {
                    spawn += dt
                    if (spawn > 0.8f) {
                        spawn = 0f
                        balls.add(Ball(0.1f + Random.nextFloat() * 0.8f, 0f, 0.25f + Random.nextFloat() * 0.25f))
                    }
                    val iter = balls.iterator()
                    while (iter.hasNext()) {
                        val b = iter.next()
                        b.y += b.speed * dt
                        if (b.y > 1f) { iter.remove(); misses++ }
                    }
                }
                tick = t
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000020))
            .pointerInput(round) {
                detectTapGestures { o ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val hit = balls.firstOrNull { hypot(it.x * w - o.x, it.y * h - o.y) < 80f }
                    if (hit != null && misses < MAX_MISSES) { balls.remove(hit); score++ }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            tick
            balls.forEach { drawCircle(Color.White, 40f, Offset(it.x * size.width, it.y * size.height)) }
        }
        Text(
            "Очки: $score    Промахи: $misses/$MAX_MISSES",
            color = Color.White, fontSize = 20.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(24.dp)
        )
        if (misses >= MAX_MISSES) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Конец! Очки: $score", color = Color.White, fontSize = 28.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { score = 0; misses = 0; round++ }) { Text("Ещё раз") }
            }
        }
        Button(onClick = onClose, modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) {
            Text("Закрыть")
        }
    }
}
