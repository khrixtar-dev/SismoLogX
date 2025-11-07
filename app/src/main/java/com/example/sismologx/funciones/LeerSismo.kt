package com.example.sismologx.funciones

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope // ⬅️ Ajusta a .DB si tu paquete es en mayúscula
import kotlinx.coroutines.launch
import com.example.sismologx.model.SismoDB
import com.example.sismologx.model.SismoLocal
import com.example.sismologx.repository.SismoLocalDBRepository

// Esta clase esta enfo
object LeerSismo {


    suspend fun obtenerTodos(context: Context): Result<List<SismoLocal>> {
        return SismoLocalDBRepository.getAll(context)
    }

    /** Carga los sismos locales y los pinta en un ListView. */
    fun cargarEnListView(
        owner: LifecycleOwner,
        context: Context,
        listView: ListView
    ) {
        owner.lifecycleScope.launch {
            val res = SismoLocalDBRepository.getAll(context)
            res.onSuccess { lista ->
                val datos = lista.map { s ->
                    "${s.date} ${s.hour}\nLugar: ${s.place}  Magnitud: ${s.magnitude}\nProfundidad: ${s.depth}"
                }
                listView.adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1,
                    datos
                )
            }.onFailure { e ->
                Toast.makeText(context, "Error leyendo SQLite: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


