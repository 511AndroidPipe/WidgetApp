package com.pipeanayap.overlayapp

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    private val gestureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val direction = intent?.getStringExtra("direction")
            Log.d("GestureReceiver", "Recibido gesto: $direction")
            direction?.let { performGesture(it) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        registerReceiver(gestureReceiver, IntentFilter("com.example.FLOATING_GESTURE"),
            RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gestureReceiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    private fun performGesture(direction: String) {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val path = Path()
        val startX = (width / 2).toFloat()
        val startY = (height / 2).toFloat()

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

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0L,
                    600L
                )
            )
            .build()

        dispatchGesture(gesture, null, null)
    }
}