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
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.round

// 👇 NUEVOS IMPORTS para cache local
import com.example.sismologx.repository.SismoLocalDBRepository

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val prefs = SettingsPrefs(appContext)
    private val notifyState = NotifyState(appContext)
    private val datePatterns = listOf(
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )
    private val timePatterns = listOf(
        DateTimeFormatter.ofPattern("HH:mm:ss"),
        DateTimeFormatter.ofPattern("HH:mm")
    )

    override suspend fun doWork(): Result {
        if (!prefs.isNotificationsEnabled()) return Result.success()

        val resp: Response<SismoResponse> = try {
            RetrofitInstance.api.getSismosRecientes()
        } catch (_: Exception) {
            return Result.retry()
        }

        if (!resp.isSuccessful) return Result.retry()
        val body = resp.body() ?: return Result.success()

        // Persistencia local offline
        try {
            // clear + inserts
            SismoLocalDBRepository.clear(applicationContext).getOrThrow()
            for (s in body.data.asReversed()) {
                SismoLocalDBRepository.insert(
                    applicationContext,
                    date = s.date,
                    hour = s.hour,
                    place = s.place,
                    magnitude = s.magnitude,
                    depth = s.depth,
                    latitude = s.latitude,
                    longitude = s.longitude,
                    image = s.image,
                    info = s.info
                ).getOrThrow()
            }
        } catch (_: Exception) {
        }

        val lastEpoch = notifyState.getLastEpoch()
        val threshold = prefs.getThreshold()
        val candidatos = body.data.mapNotNull { s -> toCandidateOrNull(s) }

        if (candidatos.isEmpty()) return Result.success()
        if (lastEpoch == 0L) {
            val maxEpoch = candidatos.maxOf { it.epochMillis }
            notifyState.setLastEpoch(maxEpoch)
            return Result.success()
        }
        val ultimo = candidatos
            .asSequence()
            .filter { it.magnitude >= threshold }
            .filter { it.epochMillis > lastEpoch }
            .maxByOrNull { it.epochMillis }

        if (ultimo != null) {
            val shownMag = round(ultimo.magnitude * 10) / 10.0
            val title = "Alerta nuevo sismo de $shownMag"
            val place = if (ultimo.place.isNullOrBlank()) "Sin lugar" else ultimo.place!!
            val depth = formatDepth(ultimo.depthText)
            val text = "Lugar: $place\nProfundidad: $depth"

            QuakeNotifier.notify(applicationContext, ultimo.id, title, text)
            notifyState.setLastEpoch(ultimo.epochMillis)
        }

        return Result.success()
    }

    private fun toCandidateOrNull(s: Sismo): Candidate? {
        val mag = s.magnitude?.replace(",", ".")?.toDoubleOrNull() ?: return null
        val epoch = parseEpochOrNull(s.date, s.hour) ?: return null

        val id = when {
            !s.info.isNullOrBlank() -> s.info!!
            else -> "${s.date}|${s.hour}|${s.latitude}|${s.longitude}"
        }

        return Candidate(
            id = id,
            epochMillis = epoch,
            magnitude = mag,
            place = s.place,
            depthText = s.depth
        )
    }

    private fun parseEpochOrNull(date: String?, hour: String?): Long? {
        if (date.isNullOrBlank() || hour.isNullOrBlank()) return null
        val parsedDate = parseFirstOrNull(datePatterns) { it.parse(date) }?.let { LocalDate.from(it) } ?: return null
        val parsedTime = parseFirstOrNull(timePatterns) { it.parse(hour) }?.let { LocalTime.from(it) } ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun <T> parseFirstOrNull(
        formats: List<DateTimeFormatter>,
        block: (DateTimeFormatter) -> T
    ): T? {
        for (fmt in formats) {
            try { return block(fmt) } catch (_: DateTimeParseException) {}
        }
        return null
    }

    private fun formatDepth(raw: String?): String {
        if (raw.isNullOrBlank()) return "N/A"
        val trimmed = raw.trim()
        return if (trimmed.lowercase().contains("km")) trimmed
        else trimmed.toDoubleOrNull()?.let { "${it} km" } ?: trimmed
    }

    private data class Candidate(
        val id: String,
        val epochMillis: Long,
        val magnitude: Double,
        val place: String?,
        val depthText: String?
    )
}
