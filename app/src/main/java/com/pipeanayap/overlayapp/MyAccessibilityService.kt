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
        val startX = (width / 2).toFloat() // Centro de la pantalla
        val startY = (height / 2).toFloat() // Centro de la pantalla

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
                path.moveTo(startX + 500f, startY)
                path.lineTo(startX - 500f, startY)
            }
            "RIGHT" -> {
                path.moveTo(startX - 500f, startY)
                path.lineTo(startX + 500f, startY)
            }
        }

        // Crear el gesto
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0L,
                    600L // Duración del gesto
                )
            )
            .build()

        // Ejecutar el gesto
        dispatchGesture(gesture, null, null)
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
