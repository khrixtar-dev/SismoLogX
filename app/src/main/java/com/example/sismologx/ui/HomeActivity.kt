package com.example.sismologx.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_activity)

        // worker periodico 1 sola vez
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        // repeticion
        val work = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "quake_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )


        val cardSismos : CardView = findViewById(R.id.cardSismos)
        val cardGuia   : CardView = findViewById(R.id.cardGuia)
        val cardConfig : CardView = findViewById(R.id.cardConfig)

        //enlaces a las demas ventanas
        cardSismos.setOnClickListener {
            val toUltimosSismos = Intent(this, SismosActivity::class.java)
            startActivity(toUltimosSismos)
        }

        cardGuia.setOnClickListener {
            val toGuia = Intent(this, BeforeGuideActivity::class.java)
            startActivity(toGuia)
        }

        cardConfig.setOnClickListener {
            val toConfiguracion = Intent(this, SettingsActivity::class.java)
            startActivity(toConfiguracion)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}