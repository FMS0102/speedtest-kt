package com.fms.appspeedtestkt.layouts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fms.appspeedtestkt.ui.theme.AppSpeedTestKtTheme
import com.fms.appspeedtestkt.ui.theme.DarkColor
import com.fms.appspeedtestkt.ui.theme.DarkGradient
import com.fms.appspeedtestkt.R
import com.fms.appspeedtestkt.services.measureDownloadSpeed
import com.fms.appspeedtestkt.ui.UiState
import com.fms.appspeedtestkt.ui.theme.Green500
import com.fms.appspeedtestkt.ui.theme.GreenGradient
import com.fms.appspeedtestkt.ui.theme.LightColor
import com.fms.appspeedtestkt.ui.theme.Pink40
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt


fun Animatable<Float, AnimationVector1D>.toUiState(maxSpeed: Float) = UiState (
    arcValue = ( if(maxSpeed > 0) maxSpeed.coerceIn(value, 180f) else value) / 150,
    speed = "%.1f".format(value),
    ping = if(value > 0.2f) "${( value * 15 ).roundToInt()} ms" else "-",
    maxSpeed = if(maxSpeed > 0f) "%.1f mbps".format(maxSpeed) else "-",
    inProgress = isRunning
)


@Composable
fun SpeedTestScreen () {
    val coroutineScope = rememberCoroutineScope()
    val animation = remember { Animatable(0f) }
    val maxSpeed = remember { mutableStateOf(0f) }
    val ping = remember { mutableStateOf("0 ms") }

    SpeedTestScreen(animation.toUiState(maxSpeed.value)) {
        coroutineScope.launch {
            maxSpeed.value = 0f

            val result = measureDownloadSpeed(animation)

            maxSpeed.value = max(result.maxSpeed.toFloat(), maxSpeed.value)
            ping.value = "${(result.ping).roundToInt()} ms"
        }
    }
}



@Composable
private fun SpeedTestScreen(state: UiState, onClick: () -> Unit){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGradient),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Header()
        SpeedIndicator(state, onClick)
        AditionalInfo(state.ping, state.maxSpeed)
        NavigationView()

    }
}

@Composable
fun SpeedIndicator(state: UiState, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
    )
    {
        CircularSpeedIndicator(state.arcValue, 240f)
        StartButton(!state.inProgress, onClick)
        SpeedValue(state.speed)
    }
}

@Composable
fun StartButton(isEnable: Boolean, onClick: () -> Unit) {

    OutlinedButton(
        modifier = Modifier.padding(bottom = 24.dp),
        enabled = isEnable,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(width = 2.dp, color = LightColor)
    ) {
        Text(
            text = "START",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
    }

}

@Composable
fun SpeedValue(speed: String) {
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("DOWNLOAD", style= MaterialTheme.typography.titleLarge)
        Text(text = speed, fontSize = 45.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("mbps", style= MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun CircularSpeedIndicator(value: Float, angle: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {

        drawLines(value, angle)
        drawArcs(value, angle)

    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress
    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawBlur() {
        for (i in 0..20) {
            drawArc(
                color = Green500.copy(alpha = i / 900f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)

            )
        }


    }
    fun drawStroke(){
        drawArc(
            color = Green500,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 86f, cap = StrokeCap.Round)
        )
    }

    fun drawGradient() {
        drawArc(
            brush = GreenGradient,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()

}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberLines: Int = 100) {
    val oneRotation = maxValue/ numberLines
    val startValue = (if (progress == 0f) 0 else floor(progress * numberLines).toInt() + 1)

    for (i in startValue..numberLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                LightColor,
                Offset(if (i% 5==0) 80f else 30f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}

@Composable
fun AditionalInfo(ping: String, maxSpeed: String) {
   @Composable
   fun RowScope.InfoColumn(title: String, value: String) {
       Column (
           horizontalAlignment = Alignment.CenterHorizontally,
           modifier = Modifier.weight(1f)
       ) {
           Text(title,
               style = MaterialTheme.typography.bodyMedium,
               color = Color.Gray
           )

           Text(value,
               style = MaterialTheme.typography.bodyMedium,
               color = Color.Gray,
               modifier = Modifier.padding(vertical = 8.dp)
           )


       }
   }

    Row (
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
    InfoColumn(title = "PING", value = ping)
        VerticalDivider()
    InfoColumn(title = "MAX SPEED", value = maxSpeed)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF414D66))
            .width(1.dp)
    )
}

@Composable
fun NavigationView(){
    val items = listOf(
        R.drawable.ic_wifi,
        R.drawable.ic_person,
        R.drawable.ic_speed,
        R.drawable.ic_settings
    )
    val selectedItem = 2
    NavigationBar(containerColor = DarkColor) {
        items.mapIndexed{
            index, item ->
            NavigationBarItem(selected = index == selectedItem,
                onClick = {},
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Pink40,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.onSurface

                ),
                icon = {
                    Icon(painterResource(id = item), null)
                }
            )
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "SPEEDTEST",
        modifier = Modifier.padding(top = 60.dp, bottom = 16.dp),
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
@Preview(device = Devices.PIXEL, showBackground = true)
fun SpeedTestScreenPreview(){
    AppSpeedTestKtTheme {
        Surface {
            SpeedTestScreen(
                UiState(
                    speed = "120.5",
                    ping = "5ms",
                    maxSpeed = "150. mpbs",
                    arcValue = 0.75f
                )
            ){}
        }
    }
}