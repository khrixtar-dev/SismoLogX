package com.example.sismologx.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.sismologx.model.SismoDB
import com.example.sismologx.model.SismoLocal

class SismoLocalDBRepository {
    // Introduce local variable = Da onSuccess o onFailure si sale bien o mal
    // Unit es similar al Void
    // use = Trata el bloque de codigo como un objeto, tambien cierra la base de datos de forma automatica

    // Insertar datos al sql
    suspend fun insert(context: Context, date: String, hour: String, place: String,
                       magnitude: String, depth: String, latitude: String, longitude: String,
                       image: String, info: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            SismoDB(context).use {
                it.insert(date, hour, place, magnitude, depth, latitude, longitude, image, info)
            }
        }
    }

    // Trae todos los datos del sql
    suspend fun getAll(context: Context): Result<List<SismoLocal>> = withContext(Dispatchers.IO) {
        runCatching {
            SismoDB(context).use { it.getAll() }
        }
    }

    // Elimina los datos del sql
    suspend fun clear(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            SismoDB(context).use { it.clear() }
        }
    }
}