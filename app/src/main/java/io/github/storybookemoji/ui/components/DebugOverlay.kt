package io.github.storybookemoji.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Debug state singleton - collects debug information from across the app
 */
object DebugState {
    var isEnabled by mutableStateOf(false)
    
    // Gesture tracking
    var pointerCount by mutableStateOf(0)
    var lastZoom by mutableStateOf(1f)
    var lastRotation by mutableStateOf(0f)
    var lastPan by mutableStateOf("(0, 0)")
    var gestureType by mutableStateOf("None")
    
    // State flow tracking
    var lastStickerScale by mutableStateOf(1f)
    var lastStickerRotation by mutableStateOf(0f)
    var callbackCount by mutableStateOf(0)
    
    // Event log (keep last 50 events)
    private val _eventLog = mutableStateListOf<String>()
    val eventLog: List<String> get() = _eventLog.toList()
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    
    // Keep up to 5000 log entries for full session capture
    private const val MAX_LOG_ENTRIES = 5000
    
    fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        _eventLog.add(0, "[$timestamp] $message")
        if (_eventLog.size > MAX_LOG_ENTRIES) {
            _eventLog.removeAt(_eventLog.lastIndex)
        }
    }
    
    fun logGesture(type: String, pointers: Int, zoom: Float, rotation: Float, panX: Float, panY: Float) {
        pointerCount = pointers
        lastZoom = zoom
        lastRotation = rotation
        lastPan = "(${panX.toInt()}, ${panY.toInt()})"
        gestureType = type
        log("$type: pointers=$pointers, zoom=${"%.3f".format(zoom)}, rot=${"%.1f".format(rotation)}°, pan=$lastPan")
    }
    
    fun logStateUpdate(scale: Float, rotation: Float) {
        lastStickerScale = scale
        lastStickerRotation = rotation
        callbackCount++
        log("STATE UPDATE #$callbackCount: scale=${"%.3f".format(scale)}, rotation=${"%.1f".format(rotation)}°")
    }
    
    fun getFullLog(): String {
        val header = """
            |=== DEBUG LOG ===
            |Gesture: $gestureType
            |Pointers: $pointerCount
            |Last Zoom: $lastZoom
            |Last Rotation: $lastRotation
            |Last Pan: $lastPan
            |Callbacks: $callbackCount
            |Sticker Scale: $lastStickerScale
            |Sticker Rotation: $lastStickerRotation
            |
            |=== EVENT LOG (${_eventLog.size} entries) ===
        """.trimMargin()
        
        // Return all entries in chronological order (oldest first)
        return header + "\n" + _eventLog.reversed().joinToString("\n")
    }
    
    fun copyToClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Debug Log", getFullLog())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied ${_eventLog.size} log entries!", Toast.LENGTH_SHORT).show()
    }
    
    fun logEvent(event: String) {
        log(event)
    }
    
    fun clear() {
        _eventLog.clear()
        pointerCount = 0
        lastZoom = 1f
        lastRotation = 0f
        lastPan = "(0, 0)"
        gestureType = "None"
    }
}

/**
 * Secret debug activator - tap 5 times with last being a 3-second hold in top-right corner
 */
@Composable
fun DebugActivator(
    modifier: Modifier = Modifier
) {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // Reset tap count if too much time passes between taps
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(2000) // 2 second window between taps
            if (System.currentTimeMillis() - lastTapTime > 2000) {
                tapCount = 0
            }
        }
    }
    
    Box(
        modifier = modifier
            .size(60.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 2000 || tapCount == 0) {
                            tapCount++
                            lastTapTime = now
                        } else {
                            tapCount = 1
                            lastTapTime = now
                        }
                    },
                    onLongPress = {
                        if (tapCount >= 4) {
                            // 4 taps + 1 long press = 5 total
                            DebugState.isEnabled = !DebugState.isEnabled
                            if (DebugState.isEnabled) {
                                DebugState.log("Debug mode ENABLED")
                            } else {
                                DebugState.clear()
                            }
                            tapCount = 0
                        }
                    }
                )
            }
    )
}

/**
 * Debug overlay that shows real-time gesture information
 */
@Composable
fun DebugOverlay(
    modifier: Modifier = Modifier
) {
    if (!DebugState.isEnabled) return
    
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                Color.Black.copy(alpha = 0.85f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // Header with buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔧 DEBUG",
                color = Color.Yellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Copy button
                Text(
                    text = "📋 COPY",
                    color = Color.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color.DarkGray, RoundedCornerShape(4.dp))
                        .clickable { DebugState.copyToClipboard(context) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                // Clear button
                Text(
                    text = "🗑️ CLEAR",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color.DarkGray, RoundedCornerShape(4.dp))
                        .clickable { DebugState.clear() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Current gesture state
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                DebugValue("Gesture", DebugState.gestureType)
                DebugValue("Pointers", DebugState.pointerCount.toString())
            }
            Column {
                DebugValue("Zoom", "%.3f".format(DebugState.lastZoom))
                DebugValue("Rotation", "%.1f°".format(DebugState.lastRotation))
            }
            Column {
                DebugValue("Pan", DebugState.lastPan)
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // State flow tracking
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                DebugValue("Callbacks", DebugState.callbackCount.toString())
            }
            Column {
                DebugValue("Sticker Scale", "%.3f".format(DebugState.lastStickerScale))
            }
            Column {
                DebugValue("Sticker Rot", "%.1f°".format(DebugState.lastStickerRotation))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Event log
        Text(
            text = "Event Log:",
            color = Color.Cyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 150.dp)
        ) {
            items(DebugState.eventLog) { event ->
                Text(
                    text = event,
                    color = when {
                        event.contains("ERROR") -> Color.Red
                        event.contains("WARN") -> Color.Yellow
                        event.contains("Multi") -> Color.Green
                        event.contains("Single") -> Color.Cyan
                        else -> Color.White
                    },
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DebugValue(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
