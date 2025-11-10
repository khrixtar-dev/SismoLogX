package com.example.sismologx.repository

import android.content.Context
import com.example.sismologx.model.SismoDB
import com.example.sismologx.model.SismoLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SismoLocalDBRepository {
    suspend fun insert(
        context: Context,
        date: String, hour: String, place: String,
        magnitude: String, depth: String, latitude: String, longitude: String,
        image: String, info: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching { SismoDB(context).use { it.insert(date, hour, place, magnitude, depth, latitude, longitude, image, info) } }
    }

    suspend fun replaceAll(context: Context, data: List<SismoLocal>): Result<Int> =
        withContext(Dispatchers.IO) { runCatching { SismoDB(context).use { it.replaceAll(data) } } }

    suspend fun getAll(context: Context): Result<List<SismoLocal>> =
        withContext(Dispatchers.IO) { runCatching { SismoDB(context).use { it.getAll() } } }

    suspend fun clear(context: Context): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { SismoDB(context).use { it.clear() } } }
}
