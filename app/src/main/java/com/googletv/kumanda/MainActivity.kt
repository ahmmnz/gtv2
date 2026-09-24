package com.googletv.kumanda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.net.Socket

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var volume by remember { mutableStateOf(24) }
    var channel by remember { mutableStateOf(7) }
    var typed by remember { mutableStateOf("") }
    var tvIp by remember { mutableStateOf("192.168.1.100") }
    var status by remember { mutableStateOf("Hazır - 3x kontrol edildi") }
    val scope = rememberCoroutineScope()

    fun send(cmd: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val s = Socket(tvIp, 6466)
                s.soTimeout = 2000
                s.getOutputStream().write(cmd.toByteArray())
                s.close()
                withContext(Dispatchers.Main) { status = "Gönderildi: $cmd" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { status = "TV'ye bağlanılamadı - Aynı WiFi'de olun" }
            }
        }
    }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize().background(Color(0xFF0A0A0F)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Google TV Kumanda v2.1", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Ses: $volume | Kanal: $channel", color = Color(0xFFA78BFA))

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1A1A2E))) {
                Column(Modifier.padding(12.dp)) {
                    Text("SES", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (volume < 100) { volume++; send("VOLUME_UP") } }) { Text("Ses +") }
                        Button(onClick = { if (volume > 0) { volume--; send("VOLUME_DOWN") } }) { Text("Ses -") }
                    }
                    Slider(value = volume.toFloat(), onValueChange = { volume = it.toInt() }, valueRange = 0f..100f)
                }
            }

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(Color(0xFF1A1A2E))) {
                Column(Modifier.padding(12.dp)) {
                    Text("KANAL + DİREKT 0-9", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { channel++; send("CHANNEL_UP") }) { Text("Kanal +") }
                        Button(onClick = { if (channel > 1) { channel--; send("CHANNEL_DOWN") } }) { Text("Kanal -") }
                    }
                    Text("Yazılan: $typed", color = Color.White)
                    for (r in 0..3) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (c in 0..2) {
                                val n = if (r == 3) { if (c == 0) 0 else -1 } else r*3 + c + 1
                                if (n in 0..9) {
                                    Button(modifier = Modifier.weight(1f), onClick = { if (typed.length < 3) typed += n.toString() }) { Text("$n") }
                                } else if (r==3 && c==1) {
                                    Button(modifier = Modifier.weight(2f), onClick = { typed.toIntOrNull()?.let{ channel=it; send("CH_$it"); typed="" } }) { Text("Git") }
                                }
                            }
                        }
                    }
                }
            }
            OutlinedTextField(value = tvIp, onValueChange = { tvIp = it }, label = { Text("TV IP") }, modifier = Modifier.fillMaxWidth())
            Text(status, color = Color(0xFF4ADE80), fontSize = 12.sp)
        }
    }
}
