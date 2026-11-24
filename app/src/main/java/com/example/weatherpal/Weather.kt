package com.example.weatherpal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

enum class Sky { CLEAR, CLOUDY, RAIN, SNOW, STORM, WINDY }

data class Weather(val temp: Int, val sky: Sky)

// Open-Meteo: бесплатно и без ключа
suspend fun loadWeather(lat: Double, lon: Double): Weather = withContext(Dispatchers.IO) {
    val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
        "&current=temperature_2m,weather_code,wind_speed_10m"
    val cur = JSONObject(URL(url).readText()).getJSONObject("current")
    val code = cur.getInt("weather_code")
    val wind = cur.getDouble("wind_speed_10m") // км/ч
    val sky = when {
        code >= 95 -> Sky.STORM
        code in 71..77 || code == 85 || code == 86 -> Sky.SNOW
        code in 51..67 || code in 80..82 -> Sky.RAIN
        wind > 35 -> Sky.WINDY
        code <= 1 -> Sky.CLEAR
        else -> Sky.CLOUDY
    }
    Weather(Math.round(cur.getDouble("temperature_2m")).toInt(), sky)
}

fun skyTitle(s: Sky) = when (s) {
    Sky.CLEAR -> "Солнечно"
    Sky.CLOUDY -> "Облачно"
    Sky.RAIN -> "Дождь"
    Sky.SNOW -> "Идёт снег"
    Sky.STORM -> "Гроза"
    Sky.WINDY -> "Ветрено"
}

// Позже эти фразы будет генерировать ИИ через ваш прокси-сервер
fun phrase(s: Sky) = when (s) {
    Sky.CLEAR -> "Ух, солнце! Очки надел, пойду загорать!"
    Sky.CLOUDY -> "Облачка плывут... Я считаю их."
    Sky.RAIN -> "Кап-кап! Где мой зонтик?!"
    Sky.SNOW -> "СНЕГ! Я такого ещё не видел! Играем в снежки?"
    Sky.STORM -> "Бабах! Я не боюсь... почти."
    Sky.WINDY -> "Ууу, держите меня, а то улечу!"
}
