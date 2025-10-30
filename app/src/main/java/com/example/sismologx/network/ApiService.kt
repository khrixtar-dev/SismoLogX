package com.example.sismologx.network

import com.example.sismologx.model.SismoResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("earthquakes/recent.json")
    suspend fun getSismosRecientes(): Response<SismoResponse>
}

