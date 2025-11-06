package com.example.sismologx.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sismologx.R
import com.example.sismologx.workers.SyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Programar el worker periódico una sola vez
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES) // mínimo 15 min
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "quake_sync",
            ExistingPeriodicWorkPolicy.KEEP, // no reprograma si ya existe
            work
        )
        //VENTANA PANTALLA PRINCIPAL
        val btnUltimosSismos : Button = findViewById(R.id.btnUltimosSismos)
        val btnGuia : Button = findViewById(R.id.btnGuia)
        val btnConfiguracion : ImageView = findViewById(R.id.btnConfiguracion)


        btnUltimosSismos.setOnClickListener {
            val toUltimosSismos = Intent(this, MainActivity2::class.java)
            startActivity(toUltimosSismos)
        }

        btnGuia.setOnClickListener {
            val toGuia = Intent(this, MainActivity3::class.java)
            startActivity(toGuia)
        }

        btnConfiguracion.setOnClickListener {
            val toConfiguracion = Intent(this, MainActivity4::class.java)
            startActivity(toConfiguracion)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}