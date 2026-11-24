package com.example.weatherpal

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private class Particle(var x: Float, var y: Float, val speed: Float, val size: Float)

/** Падающий снег / дождь. Координаты частиц — доли экрана (0..1). */
@Composable
fun FallingFx(sky: Sky) {
    val snow = sky == Sky.SNOW
    if (!snow && sky != Sky.RAIN && sky != Sky.STORM) return

    val parts = remember(sky) {
        List(if (snow) 90 else 140) {
            Particle(
                Random.nextFloat(), Random.nextFloat(),
                if (snow) 0.08f + Random.nextFloat() * 0.15f else 0.9f + Random.nextFloat() * 0.6f,
                if (snow) 3f + Random.nextFloat() * 5f else 1f
            )
        }
    }
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(sky) {
        var last = 0L
        while (true) {
            withFrameNanos { t ->
                val dt = if (last == 0L) 0f else (t - last) / 1_000_000_000f
                last = t
                parts.forEach {
                    it.y += it.speed * dt
                    if (it.y > 1f) { it.y = 0f; it.x = Random.nextFloat() }
                }
                tick = t
            }
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        tick // подписка на перерисовку
        parts.forEach {
            val x = it.x * size.width
            val y = it.y * size.height
            if (snow) drawCircle(Color.White, it.size, Offset(x, y))
            else drawLine(Color(0xAA9EC9FF), Offset(x, y), Offset(x - 4f, y + 30f), strokeWidth = 3f)
        }
    }
}

/** Наш персонаж. Пока простой комочек, потом заменим на Rive/Lottie. */
@Composable
fun Buddy(sky: Sky, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "buddy")
    val bob by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Canvas(modifier.size(200.dp)) {
        val w = size.width
        val cx = w / 2
        val r = w * 0.28f
        val cy = w * 0.6f - bob * w * 0.04f
        val lean = if (sky == Sky.WINDY) 8f + bob * 14f else 0f

        rotate(lean, Offset(cx, w * 0.9f)) {
            // тело
            drawCircle(Color(0xFF6B4EFF), r, Offset(cx, cy))
            // глаза
            val ex = r * 0.4f
            val ey = cy - r * 0.1f
            drawCircle(Color.White, r * 0.18f, Offset(cx - ex, ey))
            drawCircle(Color.White, r * 0.18f, Offset(cx + ex, ey))
            drawCircle(Color.Black, r * 0.08f, Offset(cx - ex, ey))
            drawCircle(Color.Black, r * 0.08f, Offset(cx + ex, ey))
            // рот
            drawArc(Color.White, 20f, 140f, false,
                Offset(cx - r * 0.3f, cy + r * 0.15f), Size(r * 0.6f, r * 0.4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))

            when (sky) {
                Sky.SNOW -> drawArc(Color.White, 180f, 180f, true,
                    Offset(cx - r * 0.8f, cy - r * 1.15f), Size(r * 1.6f, r * 0.8f))
                Sky.CLEAR -> {
                    drawRoundRect(Color.Black, Offset(cx - ex - r * 0.3f, ey - r * 0.17f),
                        Size(r * 0.6f, r * 0.35f), CornerRadius(8f))
                    drawRoundRect(Color.Black, Offset(cx + ex - r * 0.3f, ey - r * 0.17f),
                        Size(r * 0.6f, r * 0.35f), CornerRadius(8f))
                }
                Sky.RAIN, Sky.STORM -> {
                    drawLine(Color.DarkGray, Offset(cx, cy - r * 1.6f), Offset(cx, cy - r * 0.9f), 6f)
                    drawArc(Color(0xFFE53935), 180f, 180f, true,
                        Offset(cx - r * 1.3f, cy - r * 2.4f), Size(r * 2.6f, r * 1.6f))
                }
                else -> {}
            }
        }
    }
}
