package com.luxx.peq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

private val Bg = Color(0xFF090B09)
private val Panel = Color(0xFF141814)
private val Lime = Color(0xFFC7FF65)
private val Muted = Color(0xFF858D80)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LuxxTheme { PeqScreen() } }
    }
}

@Composable
private fun LuxxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg, surface = Panel, primary = Lime,
            onPrimary = Bg, onBackground = Color.White, onSurface = Color.White
        ), content = content
    )
}

@Composable
private fun PeqScreen() {
    var preamp by remember { mutableFloatStateOf(0f) }
    var selected by remember { mutableIntStateOf(0) }
    var bands by remember {
        mutableStateOf(List(10) { i ->
            EqBand(listOf(32f, 64f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)[i])
        })
    }
    val band = bands[selected]
    fun update(newBand: EqBand) { bands = bands.toMutableList().also { it[selected] = newBand } }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("LUXX", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("PARAMETRIC EQ", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                }
                Text("OFFLINE", color = Lime, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Panel, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
            }
            Spacer(Modifier.height(20.dp))
            ResponseGraph(bands, preamp)
            Spacer(Modifier.height(18.dp))
            CardBlock {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("PREAMP", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${preamp.toInt()} dB", color = Lime, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(value = preamp, onValueChange = { preamp = it }, valueRange = -20f..20f, modifier = Modifier.weight(2f))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("BANDS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bands.forEachIndexed { index, b ->
                    Column(
                        Modifier.background(if (selected == index) Lime else Panel, RoundedCornerShape(12.dp))
                            .clickable { selected = index }.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("B${index + 1}", color = if (selected == index) Bg else Color.White, fontWeight = FontWeight.Bold)
                        Text(freqLabel(b.frequency), color = if (selected == index) Bg else Muted, fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            CardBlock {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("BAND ${selected + 1}", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${band.gain.toInt()} dB", color = Lime, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Switch(checked = band.enabled, onCheckedChange = { update(band.copy(enabled = it)) })
                }
                Text("GAIN", color = Muted, fontSize = 10.sp)
                Slider(value = band.gain, onValueChange = { update(band.copy(gain = it)) }, valueRange = -20f..20f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("FREQUENCY", color = Muted, fontSize = 10.sp)
                    Text(freqLabel(band.frequency), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(value = ln(band.frequency) / ln(20000f), onValueChange = {
                    update(band.copy(frequency = exp(it * ln(20000f)).coerceIn(20f, 20000f)))
                }, valueRange = ln(20f) / ln(20000f)..1f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Q FACTOR", color = Muted, fontSize = 10.sp)
                    Text("%.2f".format(band.q), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(value = band.q, onValueChange = { update(band.copy(q = it)) }, valueRange = 0.1f..10f)
                Text("FILTER TYPE", color = Muted, fontSize = 10.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterType.values().forEach { type ->
                        val active = band.type == type
                        Text(type.name.replace("_", " "), fontSize = 9.sp, color = if (active) Bg else Color.White,
                            modifier = Modifier.weight(1f).background(if (active) Lime else Bg, RoundedCornerShape(8.dp))
                                .clickable { update(band.copy(type = type)) }.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { bands = bands.map { it.copy(gain = 0f, enabled = true) }; preamp = 0f },
                    modifier = Modifier.weight(1f)) { Text("RESET", color = Lime) }
                Button(onClick = { bands = bands.map { it.copy(gain = 0f) }; preamp = 0f },
                    modifier = Modifier.weight(1f)) { Text("FLAT") }
            }
            Spacer(Modifier.height(8.dp))
            Text("PEQ DESIGN / RESPONSE PREVIEW", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(16.dp), content = content)
}

@Composable
private fun ResponseGraph(bands: List<EqBand>, preamp: Float) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("FREQUENCY RESPONSE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("20 Hz — 20 kHz", color = Muted, fontSize = 10.sp)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(145.dp)) {
            val left = 8f; val right = size.width - 8f
            val top = 8f; val bottom = size.height - 8f
            fun y(db: Double) = (bottom - ((db + 24.0) / 48.0 * (bottom - top))).toFloat()
            for (db in listOf(-24, -12, 0, 12, 24)) {
                val yy = y(db.toDouble())
                drawLine(Color(0xFF303830), Offset(left, yy), Offset(right, yy), 1f)
            }
            val path = Path()
            for (i in 0..220) {
                val xRatio = i / 220.0
                val hz = 20.0 * (20000.0 / 20.0).pow(xRatio)
                val db = (Biquad.combinedDb(bands, hz) + preamp).coerceIn(-24.0, 24.0)
                val x = left + (right - left) * i / 220f
                val yy = y(db)
                if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy)
            }
            drawPath(path, Lime, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("20", color = Muted, fontSize = 9.sp)
            Text("1k", color = Muted, fontSize = 9.sp)
            Text("20k Hz", color = Muted, fontSize = 9.sp)
        }
    }
}

private fun freqLabel(value: Float): String = if (value >= 1000) "%.1fk".format(value / 1000) else "${value.toInt()} Hz"
private fun ln(v: Float) = kotlin.math.ln(v)
private fun exp(v: Float) = kotlin.math.exp(v)

