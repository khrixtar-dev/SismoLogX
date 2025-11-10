package com.example.sismologx.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sismologx.model.Sismo
import com.example.sismologx.model.SismoDB
import com.example.sismologx.repository.SismoLocalDBRepository
import com.example.sismologx.repository.SismoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SismoViewModel : ViewModel() {

    val listaSismos: MutableLiveData<List<Sismo>> = MutableLiveData()

    private val repository = SismoRepository()
    private val daBaseRepo = SismoLocalDBRepository

    fun limpiarDB(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            daBaseRepo.clear(context)
            System.out.println("Se limpio la base de datos")
        }
    }

    // Poblar la DB y luego cargar sismos con la DB (Data Base = Base de Datos)
    fun cargarDB(context : Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.obtenerSismos()
                if (response.isSuccessful) {
                    val listaDatBase = response.body()?.data ?: emptyList()
                    listaDatBase.forEach { sismo ->
                        daBaseRepo.insert(
                            context,
                            sismo.date,
                            sismo.hour,
                            sismo.place,
                            sismo.magnitude,
                            sismo.depth,
                            sismo.latitude,
                            sismo.longitude,
                            sismo.image,
                            sismo.info
                        )
                    }
                    System.out.println(listaDatBase[0])
                }
            } catch (e: Exception) {
                //
            }
            System.out.println("Se incerto algo")
        }
    }


    // Tomar datos de DB
    fun cargarSismos(context : Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = daBaseRepo.getAll(context)
                System.out.println("carga 1")
                result.onSuccess {
                    listaDatos ->
                    val listaNueva = listaDatos.map { sismoLocal ->
                        Sismo(
                            date = sismoLocal.date,
                            hour = sismoLocal.hour,
                            place = sismoLocal.place,
                            magnitude = sismoLocal.magnitude,
                            depth = sismoLocal.depth,
                            latitude = sismoLocal.latitude,
                            longitude = sismoLocal.longitude,
                            image = sismoLocal.image,
                            info = sismoLocal.info
                        )
                    }
                    if (listaNueva.isNotEmpty()) {
                        listaSismos.postValue(listaNueva)
                        System.out.println("Carga 2")
                    } else {
                        listaSismos.postValue(emptyList())
                        System.out.println("Lista Vacia")
                    }
                }

            } catch (e: Exception) {
                listaSismos.postValue(emptyList())
                System.out.println("Un error")
            }
        }
    }
    /*
    fun cargarSismos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.obtenerSismos()
                if (response.isSuccessful) {
                    val lista = response.body()?.data ?: emptyList()
                    listaSismos.postValue(lista)
                    System.out.println("Mensaje general en consola")
                } else {
                    listaSismos.postValue(emptyList())
                }
            } catch (e: Exception) {
                listaSismos.postValue(emptyList())
            }
        }
    }
    */
}
