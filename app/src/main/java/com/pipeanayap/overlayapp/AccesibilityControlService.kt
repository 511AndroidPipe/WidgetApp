package com.pipeanayap.overlayapp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class AccesibilityControlService : Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle = _lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = _savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlay()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@AccesibilityControlService)
            setViewTreeSavedStateRegistryOwner(this@AccesibilityControlService)
            setContent {
                AccessibilityControls()
            }
        }

        windowManager.addView(overlayView, getLayoutParams())
    }

    private fun getLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }
    }
}

@Composable
fun AccessibilityControls() {
    val context = LocalContext.current
    var isPortrait by remember { mutableStateOf(true) } // State to track orientation

    Column(
        modifier = Modifier
            .background(Color(0xAA4CAF50), RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isPortrait) {
            // Show vertical controls in portrait mode
            IconButton(onClick = { sendGesture(context, "UP") }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Arriba", tint = Color.White)
            }
            IconButton(onClick = { sendGesture(context, "DOWN") }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Abajo", tint = Color.White)
            }
        } else {
            // Show horizontal controls in landscape mode
            IconButton(onClick = { sendGesture(context, "LEFT") }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Color.White)
            }
            IconButton(onClick = { sendGesture(context, "RIGHT") }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Derecha", tint = Color.White)
            }
        }

        // Button to toggle orientation
        IconButton(onClick = { isPortrait = !isPortrait }) {
            Icon(Icons.Default.Refresh, contentDescription = "Cambiar orientación", tint = Color.White)
        }

        // Settings button (always visible)
        IconButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }) {
            Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White)
        }
    }
}

fun sendGesture(context: Context, direction: String) {
    val intent = Intent("com.example.FLOATING_GESTURE")
    intent.putExtra("direction", direction)
    context.sendBroadcast(intent)
}
