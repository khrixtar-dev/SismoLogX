package com.example.sismologx.model

data class Sismo(
    val date: String,
    val hour: String,
    val place: String,
    val magnitude: String,
    val depth: String,
    val latitude: String,
    val longitude: String,
    val image: String,
    val info: String
)




/*
      RESPUESTA JSON DE LA API
*     "date": "2025-10-28",
      "hour": "15:30:38",
      "place": "61 km al noreste de Mina La Escondida",
      "magnitude": "2.5",
      "depth": "150 km",
      "latitude": "-23.87",
      "longitude": "-68.65",
      "image": "https://sismologia.cl/sismicidad/informes/2025/10/323654.jpeg",
      "info": "https://sismologia.cl/sismicidad/informes/2025/10/323654.html"
*
* */