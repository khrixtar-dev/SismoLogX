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
import com.example.sismologx.viewmodel.SismoViewModel


class SismosActivity : AppCompatActivity() {

    private lateinit var lvSismosRecientes: ListView
    private lateinit var sismoViewModel: SismoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sismos_activity)

        lvSismosRecientes = findViewById(R.id.lvSismosRecientes)

        // Inicializamos el ViewModel
        sismoViewModel = ViewModelProvider(this)[SismoViewModel::class.java]

        // Observamos la lista de sismos
        sismoViewModel.listaSismos.observe(this) { lista ->
            if (lista.isNotEmpty()) {
                lvSismosRecientes.adapter = SismoAdapter(this, lista)
            } else {
                Toast.makeText(this, "No hay sismos recientes", Toast.LENGTH_SHORT).show()
            }
        }


        val btnRowBack: ImageButton = findViewById(R.id.btnBack)
        btnRowBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }


        // Cargamos los sismos desde la API
        sismoViewModel.cargarSismos()

        // Ajuste de padding para barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
