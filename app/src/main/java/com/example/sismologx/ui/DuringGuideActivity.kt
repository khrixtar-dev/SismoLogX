package com.example.sismologx.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sismologx.R

class DuringGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.during_guide_activity)

        val btnAntes : Button = findViewById(R.id.btnAntes)
        val btnDespues : Button = findViewById(R.id.btnDespues)

        btnAntes.setOnClickListener {
            val antes = Intent(this, BeforeGuideActivity::class.java)
            startActivity(antes)
        }

        btnDespues.setOnClickListener {
            val despues = Intent(this, AfterGuideActivity::class.java)
            startActivity(despues)
        }

        val btnRowBack: ImageButton = findViewById(R.id.btnBack)
        btnRowBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}