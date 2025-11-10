package com.example.sismologx.funciones

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.sismologx.viewmodel.SismoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


object ManipularLista {
    fun orden(sismoViewModel : SismoViewModel, context : Context){
        sismoViewModel.viewModelScope.launch(Dispatchers.IO ) {
            try {
                sismoViewModel.limpiarDB(context)
                sismoViewModel.cargarDB(context)
                sismoViewModel.cargarSismos(context)

                withContext(Dispatchers.Main){
                    println("Iniciando Corrutinas en Orden")
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }

        }
    }
}