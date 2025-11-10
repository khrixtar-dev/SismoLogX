package com.example.sismologx.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sismologx.model.Sismo
import com.example.sismologx.repository.SismoLocalDBRepository
import com.example.sismologx.repository.SismoRepository
import com.example.sismologx.util.toLocal
import com.example.sismologx.util.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SismoViewModel : ViewModel() {

    val listaSismos: MutableLiveData<List<Sismo>> = MutableLiveData()
    private val repository = SismoRepository()

    /** Elimina la cache local (no publica vacío para evitar parpadeo). */
    suspend fun limpiarDB(context: Context) {
        SismoLocalDBRepository.clear(context).getOrElse { throw it }
    }

    /** Lee desde SQLite y publica en el LiveData (fuente = DB). */
    suspend fun cargarDB(context: Context) {
        val locales = SismoLocalDBRepository.getAll(context).getOrElse { throw it }
        listaSismos.postValue(locales.map { it.toModel() })
    }

    /** Fetch remoto -> persistir en SQLite -> leer DB -> publicar
     */
    suspend fun cargarSismos(context: Context) {
        withContext(Dispatchers.IO) {
            runCatching { repository.obtenerSismos() }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val data = resp.body()?.data ?: emptyList()
                        val locales = data.map { it.toLocal() }
                        SismoLocalDBRepository.replaceAll(context, locales).getOrThrow()
                    }
                }
        }
        runCatching { cargarDB(context) }.onFailure { listaSismos.postValue(emptyList()) }
    }
    // fun cargarSismos()
}
