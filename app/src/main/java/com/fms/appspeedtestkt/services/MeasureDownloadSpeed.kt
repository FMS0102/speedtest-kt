package com.fms.appspeedtestkt.services

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class SpeedResult(
    val maxSpeed: Double,
    val ping: Double
)

suspend fun measureDownloadSpeed(animation: Animatable<Float, AnimationVector1D>): SpeedResult {
    val url = "https://www.dundeecity.gov.uk/sites/default/files/publications/civic_renewal_forms.zip"
    val numberOfTests = 20
    val measurements = mutableListOf<Double>()
    var totalPing = 0.0
    var totalBytesDownloaded = 0
    var totalTimeTaken = 0.0
    var maxSpeed = 0.0

    withContext(Dispatchers.IO) {
        for (i in 1..numberOfTests) {
            val startPingTime = System.currentTimeMillis()
            val connection = URL(url).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connect()

            val endPingTime = System.currentTimeMillis()

            val inputStream = connection.inputStream
            val buffer = ByteArray(128 * 1024) // buffer
            var bytesRead: Int
            var totalBytes = 0

            val startTime = System.currentTimeMillis()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalBytes += bytesRead
            }
            inputStream.close()

            val endTime = System.currentTimeMillis()
            val timeTaken = (endTime - startTime) / 1000.0

            val speed = if (timeTaken > 0) {
                (totalBytes * 8.0) / (timeTaken * 1_000_000) // Mbps
            } else {
                0.0
            }
            // capturar maior velocidade
            if (speed > maxSpeed) {
                maxSpeed = speed
            }

            measurements.add(speed)

            totalBytesDownloaded += totalBytes
            totalTimeTaken += timeTaken

            // Calcula o ping
            totalPing += (endPingTime - startPingTime).toDouble()

            // Atualização da animação
            animation.snapTo(speed.toFloat())
        }
    }

    return SpeedResult(maxSpeed, totalPing / numberOfTests)
}