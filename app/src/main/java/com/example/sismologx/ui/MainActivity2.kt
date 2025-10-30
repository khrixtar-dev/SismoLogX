package com.example.sismologx.ui

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sismologx.R
import com.example.sismologx.viewmodel.SismoViewModel


class MainActivity2 : AppCompatActivity() {

    private lateinit var lvSismosRecientes: ListView
    private lateinit var sismoViewModel: SismoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

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
