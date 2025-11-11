// SismoDetalleActivity.kt
package com.example.sismologx.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sismologx.R
import com.example.sismologx.util.SettingsPrefs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Mapbox
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations

// Para el epicentro
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class SismoDetalleActivity : AppCompatActivity() {

    private var mapView: MapView? = null

    // Texto que compartiremos (se arma en onCreate a partir de los extras)
    private var shareMessage: String = ""

    // Launcher para escoger un número de teléfono desde Contactos
    private val pickPhoneLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    contentResolver.query(
                        uri,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        null, null, null
                    )?.use { c ->
                        if (c.moveToFirst()) {
                            val number = c.getString(0)
                            openSms(number, shareMessage)
                        } else {
                            Toast.makeText(this, "No se pudo leer el número", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    // Pedir permiso READ_CONTACTS en tiempo de uso
    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchPickContact()
            } else {
                Toast.makeText(
                    this,
                    "Permiso de contactos requerido para elegir un destinatario",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sismo_detalle_activity)

        // variables UI
        val tvMagnitud: TextView = findViewById(R.id.tvMagnitud)
        val tvLugar: TextView = findViewById(R.id.tvLugar)
        val tvFechaHora: TextView = findViewById(R.id.tvFechaHora)
        val tvProfundidad: TextView = findViewById(R.id.tvProfundidad)
        val tvLatLon: TextView = findViewById(R.id.tvLatLon)

        val btnRowBack: ImageButton = findViewById(R.id.btnBack)
        val cardItemCSN: CardView = findViewById(R.id.cardItemCSN)
        val cardItemNoticias: CardView = findViewById(R.id.cardItemNoticias)
        val cardItemGuia: CardView = findViewById(R.id.cardItemGuia)

        // NUEVOS: botones de compartir
        val btnLoSentiste: Button = findViewById(R.id.btnLoSentiste) // botón “¿Lo sentiste?”
        val btnShareTop: ImageButton = findViewById(R.id.btnShare)   // botón compartir general (toolbar)

        // Extras del intent
        val mag = intent.getStringExtra("mag") ?: "-"
        val place = intent.getStringExtra("place") ?: "-"
        val dateHour = intent.getStringExtra("dateHour") ?: "-"
        val depth = intent.getStringExtra("depth") ?: "-"
        val latlon = intent.getStringExtra("latlon") ?: "-"
        val infoUrl = intent.getStringExtra("infoUrl")

        // Formateo simple de fecha/hora a "dd-MM-yyyy   HH:mm:ss"
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outD = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outT = DateTimeFormatter.ofPattern("HH:mm:ss")
        val fechaHoraUi = try {
            val dt = LocalDateTime.parse(dateHour, inFmt)
            "${dt.format(outD)}   ${dt.format(outT)}"
        } catch (_: Exception) {
            dateHour.replace("-", "/")
        }

        // Pintar
        tvMagnitud.text = "M $mag"
        tvLugar.text = place
        tvFechaHora.text = fechaHoraUi
        tvProfundidad.text = depth
        tvLatLon.text = latlon

        // Construimos el mensaje a compartir
        shareMessage = "He sentido un sismo de magnitud M $mag que ocurrio a $place, a una profundidad de $depth. ¿Tú lo has sentido?"

        // Back
        btnRowBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Acciones de cards
        cardItemCSN.setOnClickListener {
            val url = infoUrl?.trim()
            if (!url.isNullOrEmpty()) {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(i)
            } else {
                Toast.makeText(this, "No hay enlace disponible", Toast.LENGTH_SHORT).show()
            }
        }

        cardItemGuia.setOnClickListener {
            startActivity(Intent(this, BeforeGuideActivity::class.java))
        }

        cardItemNoticias.setOnClickListener {
            val news = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.csn.uchile.cl/categoria/noticias/")
            )
            startActivity(news)
        }

        // -------- Mapbox dentro de la Card --------
        mapView = findViewById(R.id.mapView)

        // Para parsear "lat, lon"
        val (lat, lon) = run {
            val parts = latlon.split(",")
            val la = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lo = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            la to lo
        }

        if (lat != null && lon != null) {
            val center = Point.fromLngLat(lon, lat)

            // Cámara
            mapView?.mapboxMap?.setCamera(
                CameraOptions.Builder()
                    .center(center)
                    .zoom(8.0)
                    .build()
            )

            // Estilo + marcador con ícono desde drawable (SVG)
            mapView?.mapboxMap?.loadStyle(
                style(Style.MAPBOX_STREETS) {
                    // aquí podrías añadir sources/layers si los necesitaras
                }
            ) { _ ->
                val annPlugin = mapView?.annotations
                val pointManager = annPlugin?.createPointAnnotationManager()

                val drawable = androidx.appcompat.content.res.AppCompatResources
                    .getDrawable(this, R.drawable.epicentro)
                val iconBitmap = drawable?.toBitmap()

                if (iconBitmap != null) {
                    val point = com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions()
                        .withPoint(center)            // center = Point.fromLngLat(lon, lat)
                        .withIconImage(iconBitmap)
                        .withIconSize(4.0)

                    pointManager?.create(point)
                }
            }
        }


        // --- NUEVO: ¿Lo sentiste? -> elegir contacto y abrir SMS ---
        btnLoSentiste.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(intent, "Compartir sismo"))
        }

        // --- NUEVO: compartir general (cualquier app) ---
        btnShareTop.setOnClickListener {
            // Verifica preferencia de acceso a contactos
            val prefs = SettingsPrefs(this)
            if (!prefs.isContactsEnabled()) {
                Toast.makeText(
                    this,
                    "Activa 'Acceso a contactos' en Configuración para elegir destinatarios",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Verifica permiso READ_CONTACTS del sistema
            val hasPerm = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPerm) {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            } else {
                launchPickContact()
            }
        }

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootDetalle)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun launchPickContact() {
        val pick = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        pickPhoneLauncher.launch(pick)
    }

    private fun openSms(number: String, text: String) {
        try {
            val sms = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(number)}")
                putExtra("sms_body", text) // la mayoría de apps de SMS lo respetan
            }
            startActivity(sms)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No se encontró una app de SMS", Toast.LENGTH_SHORT).show()
        }
    }

    // Delegar ciclo de vida al MapView
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
    }
}
