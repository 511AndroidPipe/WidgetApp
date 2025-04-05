package com.pipeanayap.overlayapp

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import android.view.View
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement

class AccesibilityControlService : AccessibilityService(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle = _lifecycleRegistry

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccesibilityControlService", "Service connected")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        showOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AccesibilityControlService", "Service destroyed")
        windowManager.removeView(overlayView)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    private fun showOverlay() {
        Log.d("AccesibilityControlService", "Showing overlay")
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
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

    Column(
        modifier = Modifier
            .background(Color(0xAA4CAF50), RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(onClick = { sendGesture(context, "UP") }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Arriba", tint = Color.White)
        }
        IconButton(onClick = { sendGesture(context, "DOWN") }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Abajo", tint = Color.White)
        }
        IconButton(onClick = { sendGesture(context, "LEFT") }) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Color.White)
        }
        IconButton(onClick = { sendGesture(context, "RIGHT") }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Derecha", tint = Color.White)
        }
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