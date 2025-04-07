package com.pipeanayap.overlayapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lanzador de la actividad para pedir permisos de superposición
        overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                startAccessibilityService()
            } else {
                Toast.makeText(this, "Permiso de superposición no otorgado", Toast.LENGTH_SHORT).show()
            }
        }

        // Verificar si ya se tiene permiso de superposición
        if (Settings.canDrawOverlays(this)) {
            startAccessibilityService()
        } else {
            // Solicitar permiso de superposición
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            overlayPermissionLauncher.launch(intent)
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("WIDGET", fontSize = 20.sp)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Verificar si el servicio de accesibilidad está habilitado
        if (!isAccessibilityServiceEnabled(this)) {
            // Si no está habilitado, mostrar un mensaje
            Toast.makeText(this, "Por favor, habilita el servicio de accesibilidad", Toast.LENGTH_SHORT).show()
            // Aquí puedes llevar al usuario a la configuración de accesibilidad para habilitarlo
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    // Iniciar el servicio de accesibilidad
    private fun startAccessibilityService() {
        val intent = Intent(this, AccesibilityControlService::class.java)
        startService(intent)
    }

    // Verificar si el servicio de accesibilidad está habilitado
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, MyAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":")
            .any { it.equals(expectedComponentName.flattenToString(), ignoreCase = true) }
    }
}
