package com.example.sismologx.util

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.sismologx.viewmodel.SismoViewModel
import kotlinx.coroutines.launch

object ManipularLista {
    /** 1) pinta cache; 2) fetch + persist + repinta. */
    fun orden(sismoViewModel: SismoViewModel, context: Context) {
        sismoViewModel.viewModelScope.launch {
            runCatching { sismoViewModel.cargarDB(context) }
            runCatching { sismoViewModel.cargarSismos(context) }
        }
    }
}
