package com.example.sismologx.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sismologx.network.RetrofitInstance
import com.example.sismologx.util.QuakeNotifier
import com.example.sismologx.util.NotifyState
import com.example.sismologx.util.SettingsPrefs
import com.example.sismologx.model.Sismo
import com.example.sismologx.model.SismoResponse
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.round

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val prefs = SettingsPrefs(appContext)
    private val notifyState = NotifyState(appContext)

    // Ajusta a tus formatos reales de date/hour
    private val dateFmt = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")

    override suspend fun doWork(): Result {
        // Si el usuario desactivó notificaciones, no hacemos nada costoso
        if (!prefs.isNotificationsEnabled()) return Result.success()

        // Llama a la API
        val resp: Response<SismoResponse> = try {
            RetrofitInstance.api.getSismosRecientes()
        } catch (e: Exception) {
            return Result.retry()
        }

        if (!resp.isSuccessful) return Result.retry()
        val body = resp.body() ?: return Result.success()

        // Filtra por umbral y novedad
        val threshold = prefs.getThreshold()
        val lastEpoch = notifyState.getLastEpoch()

        val nuevos = body.data
            .mapNotNull { s -> toCandidate(s) }               // convierte y limpia
            .filter { it.magnitude >= threshold }              // umbral usuario
            .filter { it.epochMillis > lastEpoch }             // solo nuevos
            .sortedBy { it.epochMillis }                       // de antiguo a reciente

        // Tomar SOLO el más reciente que cumpla el umbral y sea posterior a lastEpoch
        val ultimo = nuevos.maxByOrNull { it.epochMillis }
        if (ultimo != null) {
            val shownMag = round(ultimo.magnitude * 10) / 10.0
            val title = "Alerta de nuevo sismo de Magnitud $shownMag"
            val place = if (ultimo.place.isNullOrBlank()) {
                "Sin lugar"
            } else{
                ultimo.place
            }
            val depth = formatDepth(ultimo.depthText)
            val text = "Lugar: A $place\nProfundidad: $depth"

            QuakeNotifier.notify(applicationContext, ultimo.id, title, text)

            // Actualiza el “último notificado” para no repetir
            notifyState.setLastEpoch(ultimo.epochMillis)
        }

        return Result.success()
    }

    // Normaliza Sismo → modelo candidato para notificar
    private fun toCandidate(s: Sismo): Candidate? {
        val mag = parseDouble(s.magnitude) ?: return null
        val epoch = parseEpoch(s.date, s.hour) ?: System.currentTimeMillis()
        val id = if (!s.info.isNullOrBlank()) s.info!!
        else "${s.date}|${s.hour}|${s.latitude}|${s.longitude}"
        return Candidate(
            id = id,
            epochMillis = epoch,
            magnitude = mag,
            date = s.date,
            hour = s.hour,
            place = s.place,
            depthText = s.depth // usamos el string tal cual (p.ej. "120 km")
        )
    }

    private fun parseDouble(str: String?): Double? = try {
        str?.replace(",", ".")?.toDouble()
    } catch (_: Exception) { null }

    private fun parseEpoch(date: String?, hour: String?): Long? = try {
        val d = LocalDate.parse(date, dateFmt)
        val t = LocalTime.parse(hour, timeFmt)
        LocalDateTime.of(d, t).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } catch (_: Exception) { null }

    // Normaliza el texto de profundidad: si viene vacío => "N/A", si es número sin sufijo => agrega " km"
    private fun formatDepth(raw: String?): String {
        if (raw.isNullOrBlank()) return "N/A"
        val trimmed = raw.trim()
        val hasKm = trimmed.lowercase().contains("km")
        return if (hasKm) trimmed else {
            val numeric = parseDouble(trimmed)
            if (numeric != null) "${numeric} km" else trimmed
        }
    }

    private data class Candidate(
        val id: String,
        val epochMillis: Long,
        val magnitude: Double,
        val date: String,
        val hour: String,
        val place: String?,
        val depthText: String?
    )
}
