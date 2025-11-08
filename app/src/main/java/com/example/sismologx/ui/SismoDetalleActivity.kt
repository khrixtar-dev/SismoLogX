// SismoDetalleActivity.kt
package com.example.sismologx.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sismologx.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Mapbox
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations

// Para el epicentro
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager


class SismoDetalleActivity : AppCompatActivity() {

    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sismo_detalle_activity)

        // variables
        val tvMagnitud: TextView     = findViewById(R.id.tvMagnitud)
        val tvLugar: TextView        = findViewById(R.id.tvLugar)
        val tvFechaHora: TextView    = findViewById(R.id.tvFechaHora)
        val tvProfundidad: TextView  = findViewById(R.id.tvProfundidad)
        val tvLatLon: TextView       = findViewById(R.id.tvLatLon)

        val btnRowBack: ImageButton  = findViewById(R.id.btnBack)
        val cardItemCSN: CardView    = findViewById(R.id.cardItemCSN)
        val cardItemNoticias: CardView= findViewById(R.id.cardItemNoticias)
        val cardItemGuia: CardView   = findViewById(R.id.cardItemGuia)

        // Extras del intent
        val mag       = intent.getStringExtra("mag") ?: "-"
        val place     = intent.getStringExtra("place") ?: "-"
        val dateHour  = intent.getStringExtra("dateHour") ?: "-"
        val depth     = intent.getStringExtra("depth") ?: "-"
        val latlon    = intent.getStringExtra("latlon") ?: "-"
        val infoUrl   = intent.getStringExtra("infoUrl")

        // Formateo simple de fecha/hora a "dd-MM-yyyy   HH:mm:ss"
        val inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outD   = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val outT   = DateTimeFormatter.ofPattern("HH:mm:ss")
        val fechaHoraUi = try {
            val dt = LocalDateTime.parse(dateHour, inFmt)
            "${dt.format(outD)}   ${dt.format(outT)}"
        } catch (_: Exception) {
            dateHour.replace("-", "/")
        }

        // Pintar
        tvMagnitud.text     = "M $mag"
        tvLugar.text        = place
        tvFechaHora.text    = fechaHoraUi
        tvProfundidad.text  = depth
        tvLatLon.text       = latlon

        // Back
        btnRowBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Acciones de cards
        cardItemCSN.setOnClickListener {
            val url = infoUrl?.trim()
            if (!url.isNullOrEmpty()) {
                val i = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                startActivity(i)
            } else {
                android.widget.Toast.makeText(this, "No hay enlace disponible", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        cardItemGuia.setOnClickListener {
            startActivity(Intent(this, BeforeGuideActivity::class.java))
        }

        cardItemNoticias.setOnClickListener {
            val news = Intent(
                Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.csn.uchile.cl/categoria/noticias/")
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
            mapView?.mapboxMap?.loadStyleUri(Style.MAPBOX_STREETS) {
                val annPlugin = mapView?.annotations
                val pointManager: PointAnnotationManager? = annPlugin?.createPointAnnotationManager()

                // Carga el vector y conviértelo a Bitmap
                val drawable = AppCompatResources.getDrawable(this, R.drawable.epicentro)
                val iconBitmap = drawable?.toBitmap()   // tamaño del vector por defecto

                if (iconBitmap != null) {
                    val point = PointAnnotationOptions()
                        .withPoint(center)
                        .withIconImage(iconBitmap)
                        .withIconSize(4.0) // ajusta si lo ves muy grande/pequeño (ej. 0.8, 1.2, etc.)
                    pointManager?.create(point)
                }
            }

        }


        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootDetalle)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
