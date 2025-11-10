package com.example.sismologx.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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
import com.example.sismologx.util.QuakeNotifier
import com.example.sismologx.util.SettingsPrefs
import kotlin.math.round


class SettingsActivity : AppCompatActivity() {

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
    // Arriba, junto al launcher de notificaciones:
    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val sw: Switch = findViewById(R.id.swContactos)
            if (granted) {
                SettingsPrefs(this).setContactsEnabled(true)
                sw.isChecked = true
                Toast.makeText(this, "Acceso a contactos activado", Toast.LENGTH_SHORT).show()
            } else {
                SettingsPrefs(this).setContactsEnabled(false)
                sw.isChecked = false
                Toast.makeText(this, "Permiso de contactos denegado", Toast.LENGTH_SHORT).show()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)

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
        val swContactos: Switch = findViewById(R.id.swContactos)

        // Estado inicial: preferencia + verificación de permiso real
        val contactsEnabledPref = prefs.isContactsEnabled()
        val hasContactsPerm = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        swContactos.isChecked = contactsEnabledPref && hasContactsPerm

        // Listener del switch
        swContactos.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                // Si no hay permiso, pedirlo; si ya está, solo persistir preferencia
                if (!hasContactsPerm &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
                } else {
                    prefs.setContactsEnabled(true)
                    Toast.makeText(this, "Acceso a contactos activado", Toast.LENGTH_SHORT).show()
                }
            } else {
                prefs.setContactsEnabled(false)
                Toast.makeText(this, "Acceso a contactos desactivado", Toast.LENGTH_SHORT).show()
            }
        }



        btnGuardarCambios.setOnClickListener {
            val current = (minMag + seekBarMagnitud.progress * step).coerceIn(minMag, maxMag)
            val rounded = round(current * 10) / 10.0
            prefs.setThreshold(rounded)
            prefs.setNotificationsEnabled(swNotificacion.isChecked)
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            val ok = Intent(this, HomeActivity::class.java)
            startActivity(ok)
        }

        val btnRowBack: ImageButton = findViewById(R.id.btnBack)
        btnRowBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
        // dentro de onCreate(...)
        val btnTestNotif: Button = findViewById(R.id.btnTestNotif)
        btnTestNotif.setOnClickListener {
            // Android 13+ pide permiso en runtime
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@setOnClickListener
            }

            // Datos falsos simulando un sismo real
            val fakeMag = 6.4
            val fakePlace = "85 km al NO de Iquique"
            val fakeDepth = "35 km"

            val title = "Alerta de nuevo sismo de ${"%.1f".format(fakeMag)}"
            val text  = "Lugar: $fakePlace\nProfundidad: $fakeDepth"

            // Manda la notificación usando el mismo helper de producción
            val fakeId = "test-${System.currentTimeMillis()}"
            QuakeNotifier.notify(this, fakeId, title, text)

        }




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
