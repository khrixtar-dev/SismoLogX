package com.example.sismologx.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.Closeable

class SismoDB(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION),
    Closeable {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SISMOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE TEXT NOT NULL,
                $COL_HOUR TEXT NOT NULL,
                $COL_PLACE TEXT NOT NULL,
                $COL_MAGNITUDE TEXT NOT NULL,
                $COL_DEPTH TEXT NOT NULL,
                $COL_LATITUDE TEXT NOT NULL,
                $COL_LONGITUDE TEXT NOT NULL,
                $COL_IMAGE TEXT NOT NULL,
                $COL_INFO TEXT NOT NULL
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SISMOS")
        onCreate(db)
    }

    fun insert(
        date: String, hour: String, place: String, magnitude: String, depth: String,
        latitude: String, longitude: String, image: String, info: String
    ): Long {
        val cv = ContentValues().apply {
            put(COL_DATE, date); put(COL_HOUR, hour); put(COL_PLACE, place)
            put(COL_MAGNITUDE, magnitude); put(COL_DEPTH, depth)
            put(COL_LATITUDE, latitude); put(COL_LONGITUDE, longitude)
            put(COL_IMAGE, image); put(COL_INFO, info)
        }
        return writableDatabase.insert(TABLE_SISMOS, null, cv)
    }
    fun replaceAll(data: List<SismoLocal>): Int {
        val db = writableDatabase
        var inserted = 0
        db.beginTransaction()
        try {
            db.delete(TABLE_SISMOS, null, null)
            val cv = ContentValues()
            for (s in data.asReversed()) {
                cv.clear()
                cv.put(COL_DATE, s.date); cv.put(COL_HOUR, s.hour); cv.put(COL_PLACE, s.place)
                cv.put(COL_MAGNITUDE, s.magnitude); cv.put(COL_DEPTH, s.depth)
                cv.put(COL_LATITUDE, s.latitude); cv.put(COL_LONGITUDE, s.longitude)
                cv.put(COL_IMAGE, s.image); cv.put(COL_INFO, s.info)
                db.insert(TABLE_SISMOS, null, cv)
                inserted++
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        return inserted
    }

    fun getAll(): List<SismoLocal> {
        val out = mutableListOf<SismoLocal>()
        val sql = """
            SELECT $COL_ID, $COL_DATE, $COL_HOUR, $COL_PLACE, $COL_MAGNITUDE,
                   $COL_DEPTH, $COL_LATITUDE, $COL_LONGITUDE, $COL_IMAGE, $COL_INFO
            FROM $TABLE_SISMOS
            ORDER BY $COL_ID DESC
        """.trimIndent()
        val c: Cursor = readableDatabase.rawQuery(sql, null)
        c.use {
            while (it.moveToNext()) {
                out += SismoLocal(
                    id = it.getInt(0),
                    date = it.getString(1),
                    hour = it.getString(2),
                    place = it.getString(3),
                    magnitude = it.getString(4),
                    depth = it.getString(5),
                    latitude = it.getString(6),
                    longitude = it.getString(7),
                    image = it.getString(8),
                    info = it.getString(9)
                )
            }
        }
        return out
    }

    fun clear() { writableDatabase.delete(TABLE_SISMOS, null, null) }

    override fun close() = super.close()

    companion object {
        private const val DB_NAME = "sismos.db"
        private const val DB_VERSION = 1
        const val TABLE_SISMOS = "sismos_local"
        const val COL_ID = "id"; const val COL_DATE = "date"; const val COL_HOUR = "hour"
        const val COL_PLACE = "place"; const val COL_MAGNITUDE = "magnitude"; const val COL_DEPTH = "depth"
        const val COL_LATITUDE = "latitude"; const val COL_LONGITUDE = "longitude"
        const val COL_IMAGE = "image"; const val COL_INFO = "info"
    }
}

data class SismoLocal(
    val id: Int,
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
