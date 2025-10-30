package com.example.sismologx.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sismologx.model.Sismo
import com.example.sismologx.repository.SismoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SismoViewModel : ViewModel() {

    val listaSismos: MutableLiveData<List<Sismo>> = MutableLiveData()

    private val repository = SismoRepository()

    fun cargarSismos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.obtenerSismos()
                if (response.isSuccessful) {
                    val lista = response.body()?.data ?: emptyList()
                    listaSismos.postValue(lista)
                } else {
                    listaSismos.postValue(emptyList())
                }
            } catch (e: Exception) {
                listaSismos.postValue(emptyList())
            }
        }
    }
}
