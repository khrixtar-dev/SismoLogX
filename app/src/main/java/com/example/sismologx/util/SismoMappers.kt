package com.example.sismologx.util

import com.example.sismologx.model.Sismo
import com.example.sismologx.model.SismoLocal

fun Sismo.toLocal(): SismoLocal = SismoLocal(
    id = 0, // autoincrement
    date = date,
    hour = hour,
    place = place,
    magnitude = magnitude,
    depth = depth,
    latitude = latitude,
    longitude = longitude,
    image = image,
    info = info
)

fun SismoLocal.toModel(): Sismo = Sismo(
    date = date,
    hour = hour,
    place = place,
    magnitude = magnitude,
    depth = depth,
    latitude = latitude,
    longitude = longitude,
    image = image,
    info = info
)
