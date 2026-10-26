package com.github.bernatbc.petitapetit.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.Scheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private class WordDatabase(context: Context) :
    SQLiteOpenHelper(context, "words.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE words (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                word_key TEXT NOT NULL UNIQUE,
                word TEXT NOT NULL,
                translation TEXT NOT NULL,
                sentence TEXT NOT NULL,
                added_at INTEGER NOT NULL,
                due_at INTEGER NOT NULL,
                interval_days REAL NOT NULL,
                ease REAL NOT NULL,
                repetitions INTEGER NOT NULL,
                lapses INTEGER NOT NULL,
                last_reviewed_at INTEGER
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

class WordRepository(context: Context) {

    private val database = WordDatabase(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _cards = MutableStateFlow<List<Card>>(emptyList())

    // Every saved card, newest first. Kept in memory because the dictionary stays small.
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    init {
        scope.launch { _cards.value = queryAll() }
    }

    // Returns false when the word is already in the dictionary.
    suspend fun add(word: String, translation: String, sentence: String, now: Long): Boolean =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("word_key", keyOf(word))
                put("word", word.trim())
                put("translation", translation.trim())
                put("sentence", sentence.trim())
                put("added_at", now)
                put("due_at", now)
                put("interval_days", 0.0)
                put("ease", Scheduler.INITIAL_EASE)
                put("repetitions", 0)
                put("lapses", 0)
            }
            val inserted = database.writableDatabase
                .insertWithOnConflict("words", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
            if (inserted) _cards.value = queryAll()
            inserted
        }

    suspend fun update(card: Card) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("translation", card.translation)
            put("due_at", card.dueAt)
            put("interval_days", card.intervalDays)
            put("ease", card.ease)
            put("repetitions", card.repetitions)
            put("lapses", card.lapses)
            put("last_reviewed_at", card.lastReviewedAt)
        }
        database.writableDatabase.update("words", values, "id = ?", arrayOf(card.id.toString()))
        _cards.value = queryAll()
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        database.writableDatabase.delete("words", "id = ?", arrayOf(id.toString()))
        _cards.value = queryAll()
    }

    suspend fun dueCards(endOfToday: Long): List<Card> = withContext(Dispatchers.IO) {
        queryAll().filter { it.dueAt <= endOfToday }
    }

    private fun queryAll(): List<Card> =
        database.readableDatabase.query("words", null, null, null, null, null, "added_at DESC, id DESC")
            .use { cursor -> generateSequence { if (cursor.moveToNext()) cursor.toCard() else null }.toList() }

    private fun Cursor.toCard() = Card(
        id = getLong(getColumnIndexOrThrow("id")),
        word = getString(getColumnIndexOrThrow("word")),
        translation = getString(getColumnIndexOrThrow("translation")),
        sentence = getString(getColumnIndexOrThrow("sentence")),
        addedAt = getLong(getColumnIndexOrThrow("added_at")),
        dueAt = getLong(getColumnIndexOrThrow("due_at")),
        intervalDays = getDouble(getColumnIndexOrThrow("interval_days")),
        ease = getDouble(getColumnIndexOrThrow("ease")),
        repetitions = getInt(getColumnIndexOrThrow("repetitions")),
        lapses = getInt(getColumnIndexOrThrow("lapses")),
        lastReviewedAt = getColumnIndexOrThrow("last_reviewed_at").let { index ->
            if (isNull(index)) null else getLong(index)
        },
    )

    companion object {
        fun keyOf(word: String): String = word.trim().lowercase()
    }
}
