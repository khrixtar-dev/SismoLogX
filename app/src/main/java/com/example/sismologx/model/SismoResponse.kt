package com.example.sismologx.model

data class SismoResponse(
    val status: String,
    val data: List<Sismo>
)
