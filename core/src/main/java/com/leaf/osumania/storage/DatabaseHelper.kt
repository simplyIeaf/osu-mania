package com.leaf.osumania.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ScoreEntry(
    val id: Long,
    val beatmapHash: String,
    val score: Long,
    val accuracy: Float,
    val maxCombo: Int,
    val judgements: String,
    val mods: String,
    val letterGrade: String,
    val pp: Float,
    val timestamp: Long,
    val replayId: String?
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "osumania.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SCORES = "scores"
        private const val TABLE_COLLECTIONS = "collections"
        private const val TABLE_BEATMAP_CACHE = "beatmap_cache"
        private const val TABLE_REPLAY_FILES = "replay_files"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_SCORES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                beatmap_hash TEXT NOT NULL,
                score INTEGER NOT NULL,
                accuracy REAL NOT NULL,
                max_combo INTEGER NOT NULL,
                judgements TEXT NOT NULL,
                mods TEXT NOT NULL,
                letter_grade TEXT NOT NULL,
                pp REAL NOT NULL,
                timestamp INTEGER NOT NULL,
                replay_id TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE $TABLE_COLLECTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                beatmap_set_ids TEXT NOT NULL DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE $TABLE_BEATMAP_CACHE (
                beatmap_set_id TEXT PRIMARY KEY,
                file_data BLOB NOT NULL,
                date_added INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE $TABLE_REPLAY_FILES (
                replay_id TEXT PRIMARY KEY,
                file_data BLOB NOT NULL,
                date_added INTEGER NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCORES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COLLECTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BEATMAP_CACHE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPLAY_FILES")
        onCreate(db)
    }

    fun insertScore(hash: String, score: Long, accuracy: Float, maxCombo: Int, judgements: String, mods: String, letterGrade: String, pp: Float, replayId: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("beatmap_hash", hash)
            put("score", score)
            put("accuracy", accuracy)
            put("max_combo", maxCombo)
            put("judgements", judgements)
            put("mods", mods)
            put("letter_grade", letterGrade)
            put("pp", pp)
            put("timestamp", System.currentTimeMillis())
            put("replay_id", replayId)
        }
        db.insert(TABLE_SCORES, null, values)
    }

    fun getScoresForBeatmap(hash: String): List<ScoreEntry> {
        val db = readableDatabase
        val cursor = db.query(TABLE_SCORES, null, "beatmap_hash = ?", arrayOf(hash), null, null, "score DESC", "5")
        val scores = mutableListOf<ScoreEntry>()
        while (cursor.moveToNext()) {
            scores.add(ScoreEntry(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                beatmapHash = cursor.getString(cursor.getColumnIndexOrThrow("beatmap_hash")),
                score = cursor.getLong(cursor.getColumnIndexOrThrow("score")),
                accuracy = cursor.getFloat(cursor.getColumnIndexOrThrow("accuracy")),
                maxCombo = cursor.getInt(cursor.getColumnIndexOrThrow("max_combo")),
                judgements = cursor.getString(cursor.getColumnIndexOrThrow("judgements")),
                mods = cursor.getString(cursor.getColumnIndexOrThrow("mods")),
                letterGrade = cursor.getString(cursor.getColumnIndexOrThrow("letter_grade")),
                pp = cursor.getFloat(cursor.getColumnIndexOrThrow("pp")),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                replayId = cursor.getString(cursor.getColumnIndexOrThrow("replay_id"))
            ))
        }
        cursor.close()
        return scores
    }

    fun deleteScore(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_SCORES, "id = ?", arrayOf(id.toString()))
    }

    fun clearAllScores() {
        val db = writableDatabase
        db.delete(TABLE_SCORES, null, null)
    }

    fun createCollection(name: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("beatmap_set_ids", "")
        }
        db.insert(TABLE_COLLECTIONS, null, values)
    }

    fun deleteCollection(name: String) {
        val db = writableDatabase
        db.delete(TABLE_COLLECTIONS, "name = ?", arrayOf(name))
    }

    fun renameCollection(oldName: String, newName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", newName)
        }
        db.update(TABLE_COLLECTIONS, values, "name = ?", arrayOf(oldName))
    }

    fun getCollections(): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.query(TABLE_COLLECTIONS, arrayOf("id", "name"), null, null, null, null, "name ASC")
        val collections = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            collections.add(Pair(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name"))
            ))
        }
        cursor.close()
        return collections
    }

    fun addBeatmapSetToCollection(collectionId: Int, setId: Int) {
        val db = writableDatabase
        val cursor = db.query(TABLE_COLLECTIONS, arrayOf("beatmap_set_ids"), "id = ?", arrayOf(collectionId.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val current = cursor.getString(cursor.getColumnIndexOrThrow("beatmap_set_ids"))
            cursor.close()
            val ids = if (current.isNullOrBlank()) mutableSetOf() else current.split(",").toMutableSet()
            ids.add(setId.toString())
            val values = ContentValues().apply {
                put("beatmap_set_ids", ids.joinToString(","))
            }
            db.update(TABLE_COLLECTIONS, values, "id = ?", arrayOf(collectionId.toString()))
        } else {
            cursor.close()
        }
    }

    fun removeBeatmapSetFromCollection(collectionId: Int, setId: Int) {
        val db = writableDatabase
        val cursor = db.query(TABLE_COLLECTIONS, arrayOf("beatmap_set_ids"), "id = ?", arrayOf(collectionId.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val current = cursor.getString(cursor.getColumnIndexOrThrow("beatmap_set_ids"))
            cursor.close()
            val ids = if (current.isNullOrBlank()) mutableSetOf() else current.split(",").toMutableSet()
            ids.remove(setId.toString())
            val values = ContentValues().apply {
                put("beatmap_set_ids", ids.joinToString(","))
            }
            db.update(TABLE_COLLECTIONS, values, "id = ?", arrayOf(collectionId.toString()))
        } else {
            cursor.close()
        }
    }

    fun getCollectionBeatmapSets(collectionId: Int): List<Int> {
        val db = readableDatabase
        val cursor = db.query(TABLE_COLLECTIONS, arrayOf("beatmap_set_ids"), "id = ?", arrayOf(collectionId.toString()), null, null, null)
        val result = mutableListOf<Int>()
        if (cursor.moveToFirst()) {
            val raw = cursor.getString(cursor.getColumnIndexOrThrow("beatmap_set_ids"))
            cursor.close()
            if (!raw.isNullOrBlank()) {
                raw.split(",").forEach { entry ->
                    entry.trim().toIntOrNull()?.let { result.add(it) }
                }
            }
        } else {
            cursor.close()
        }
        return result
    }

    fun cacheBeatmapSet(setId: String, data: ByteArray) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("beatmap_set_id", setId)
            put("file_data", data)
            put("date_added", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_BEATMAP_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCachedBeatmapSet(setId: String): ByteArray? {
        val db = readableDatabase
        val cursor = db.query(TABLE_BEATMAP_CACHE, arrayOf("file_data"), "beatmap_set_id = ?", arrayOf(setId), null, null, null)
        val result = if (cursor.moveToFirst()) {
            cursor.getBlob(cursor.getColumnIndexOrThrow("file_data"))
        } else {
            null
        }
        cursor.close()
        return result
    }

    fun clearBeatmapCache() {
        val db = writableDatabase
        db.delete(TABLE_BEATMAP_CACHE, null, null)
    }

    fun getBeatmapCacheSize(): Pair<Int, Long> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*), COALESCE(SUM(LENGTH(file_data)), 0) FROM $TABLE_BEATMAP_CACHE", null)
        val count: Int
        val totalBytes: Long
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
            totalBytes = cursor.getLong(1)
        } else {
            count = 0
            totalBytes = 0L
        }
        cursor.close()
        return Pair(count, totalBytes)
    }
}
