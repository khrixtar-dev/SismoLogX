package com.example.sismologx.repository

import com.example.sismologx.model.SismoResponse
import com.example.sismologx.network.RetrofitInstance
import retrofit2.Response


class SismoRepository {
    suspend fun obtenerSismos(): Response<SismoResponse> {
        return RetrofitInstance.api.getSismosRecientes()
    }
}
