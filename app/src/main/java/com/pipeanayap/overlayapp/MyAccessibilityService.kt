package com.pipeanayap.overlayapp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.provider.Settings

class MyAccessibilityService : AccessibilityService() {

    private val gestureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val direction = intent?.getStringExtra("direction")
            Log.d("GestureReceiver", "Recibido gesto: $direction")
            direction?.let { performGesture(it) }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onServiceConnected() {
        super.onServiceConnected()

        // Registra el receptor para escuchar los gestos
        registerReceiver(gestureReceiver, IntentFilter("com.example.FLOATING_GESTURE"))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Desregistra el receptor
        unregisterReceiver(gestureReceiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Este método no es necesario en este caso, ya que solo estamos trabajando con gestos
    }

    override fun onInterrupt() {
        // Este método se llama si el servicio se interrumpe
    }

    private fun performGesture(direction: String) {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val path = Path()
        val startX = (width / 2).toFloat()
        val startY = (height / 2).toFloat()
        val maxOffset = (width * 0.3).toFloat() // Máximo 30% del ancho de la pantalla

        Log.d("performGesture", "Screen width: $width, height: $height")
        Log.d("performGesture", "Starting point: ($startX, $startY)")

        when (direction) {
            "UP" -> {
                path.moveTo(startX, startY + 400f)
                path.lineTo(startX, startY - 400f)
            }
            "DOWN" -> {
                path.moveTo(startX, startY - 400f)
                path.lineTo(startX, startY + 400f)
            }
            "LEFT" -> {
                path.moveTo(startX + maxOffset, startY)
                path.lineTo(startX - maxOffset, startY)
            }
            "RIGHT" -> {
                path.moveTo(startX - maxOffset, startY)
                path.lineTo(startX + maxOffset, startY)
            }
            else -> {
                Log.e("performGesture", "Invalid direction: $direction")
                return
            }
        }

        try {
            val gesture = GestureDescription.Builder()
                .addStroke(
                    GestureDescription.StrokeDescription(
                        path,
                        0L,
                        800L // Duración incrementada
                    )
                )
                .build()

            Log.d("performGesture", "Dispatching gesture for direction: $direction")
            val result = dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Log.d("performGesture", "Gesture completed: $direction")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    Log.e("performGesture", "Gesture cancelled: $direction. Check system constraints or path validity.")
                }
            }, null)

            if (!result) {
                Log.e("performGesture", "Failed to dispatch gesture: $direction")
            }
        } catch (e: Exception) {
            Log.e("performGesture", "Error dispatching gesture: ${e.message}", e)
        }
    }

    // Método para verificar si el servicio de accesibilidad está habilitado
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, MyAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":")
            .any { it.equals(expectedComponentName.flattenToString(), ignoreCase = true) }
    }
}
