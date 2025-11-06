package com.example.sismologx.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sismologx.R
import com.example.sismologx.util.SettingsPrefs
import kotlin.math.round


class MainActivity4 : AppCompatActivity() {

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                val sw: Switch = findViewById(R.id.swNotificacion)
                sw.isChecked = false
                SettingsPrefs(this).setNotificationsEnabled(false)
            } else {
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
                SettingsPrefs(this).setNotificationsEnabled(true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main4)

        //VENTANA CONFIGURACIONES

        //HELPERS Y RANGOS
        val prefs = SettingsPrefs(this)
        val minMag = 1.0
        val maxMag = 8.0
        val step = 0.1
        val maxProgress = ((maxMag - minMag) / step).toInt() // 50

        val swNotificacion: Switch = findViewById(R.id.swNotificacion)
        val seekBarMagnitud: SeekBar = findViewById(R.id.seekBarMagnitud)
        val txMagnitud: TextView = findViewById(R.id.txMagnitud)
        val btnGuardarCambios: Button = findViewById(R.id.btnGuardarCambios)

        // estado inicial desde preferencias
        seekBarMagnitud.max = maxProgress
        val enabled = prefs.isNotificationsEnabled()
        val threshold = prefs.getThreshold()
        swNotificacion.isChecked = enabled
        seekBarMagnitud.progress = ((threshold - minMag)/ step).toInt().coerceIn(0, maxProgress)
        txMagnitud.text = "${round(threshold * 10) / 10.0}"

        //Listeners
        swNotificacion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // en Android 13+ hay que pedir permiso en runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    prefs.setNotificationsEnabled(true)
                    Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.setNotificationsEnabled(false)
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        seekBarMagnitud.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = (minMag + progress * step).coerceIn(minMag, maxMag)
                val shown = round(value * 10) / 10.0
                txMagnitud.text = "$shown"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnGuardarCambios.setOnClickListener {
            val current = (minMag + seekBarMagnitud.progress * step).coerceIn(minMag, maxMag)
            val rounded = round(current * 10) / 10.0
            prefs.setThreshold(rounded)
            prefs.setNotificationsEnabled(swNotificacion.isChecked)
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}