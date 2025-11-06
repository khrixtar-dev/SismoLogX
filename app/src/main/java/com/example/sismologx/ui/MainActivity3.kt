package com.example.sismologx.ui

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sismologx.R

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)

        //VENTANA GUIA PREVENCION SISMOS

        val textView = findViewById<TextView>(R.id.txParrafo2)
        val texto = " <strong>Primero que nada debes de mantener la calma.</strong>" +
                "Es importante no correr ni gritar, ya que esto puede aumentar el riesgo de lesiones."
        textView.text = Html.fromHtml(texto, Html.FROM_HTML_MODE_LEGACY)








        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}