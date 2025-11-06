package com.example.sismologx.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.sismologx.R
import com.example.sismologx.model.Sismo

class SismoAdapter(
    context: Context,
    private val sismos: List<Sismo>
) : ArrayAdapter<Sismo>(context, 0, sismos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val sismo = sismos[position]

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_sismo, parent, false)

        val tvPlace = view.findViewById<TextView>(R.id.tvPlace)
        val tvMagnitude = view.findViewById<TextView>(R.id.tvMagnitude)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvHour = view.findViewById<TextView>(R.id.tvHour)

        // Asignar texto
        tvPlace.text = sismo.place
        tvMagnitude.text = sismo.magnitude
        tvDate.text = sismo.date
        tvHour.text = sismo.hour

        // Cambiar color del círculo según magnitud
        val magnitudeValue = sismo.magnitude.toDoubleOrNull()?:0.0
        val background = tvMagnitude.background.mutate() as GradientDrawable

        val color = when {
            magnitudeValue >= 7.0 -> Color.parseColor("#ef4444")// rojo intenso
            magnitudeValue >= 6.0 -> Color.parseColor("#f97316") // naranjo
            magnitudeValue >= 4.0 -> Color.parseColor("#eab308") // amarillo claro
            else -> Color.parseColor("#22c55e") // verde
        }
        background.setColor(color)
        return view
    }
}
