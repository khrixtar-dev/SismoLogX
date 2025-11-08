// SismosActivity.kt
package com.example.sismologx.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sismologx.R
import com.example.sismologx.model.Sismo
import com.example.sismologx.viewmodel.SismoViewModel

class SismosActivity : AppCompatActivity() {

    private lateinit var lvSismosRecientes: ListView
    private lateinit var sismoViewModel: SismoViewModel

    // >>> guarda la lista actual para usarla en el click
    private var listaActual: List<Sismo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sismos_activity)

        lvSismosRecientes = findViewById(R.id.lvSismosRecientes)
        sismoViewModel = ViewModelProvider(this)[SismoViewModel::class.java]

        sismoViewModel.listaSismos.observe(this) { lista ->
            if (lista.isNotEmpty()) {
                listaActual = lista
                lvSismosRecientes.adapter = SismoAdapter(this, lista)

                // >>> click para abrir el detalle con extras
                lvSismosRecientes.setOnItemClickListener { _, _, position, _ ->
                    val s = listaActual[position]
                    val intent = Intent(this, SismoDetalleActivity::class.java).apply {
                        putExtra("mag", s.magnitude)                       // String
                        putExtra("place", s.place)                           // String
                        putExtra("dateHour", "${s.date} ${s.hour}")          // String compuesto
                        putExtra("depth", s.depth)                           // p.ej. "30 km"
                        putExtra("latlon", "${s.latitude}, ${s.longitude}")  // "lat, lon"
                        putExtra("infoUrl", s.info)
                    }
                    startActivity(intent)
                }

            } else {
                Toast.makeText(this, "No hay sismos recientes", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar sismos
        sismoViewModel.cargarSismos()

        // Back a Home (opcional)
        val btnRowBack: ImageButton = findViewById(R.id.btnBack)
        btnRowBack.setOnClickListener {
            finish() // más rápido que crear un Intent nuevo
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
