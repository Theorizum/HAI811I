package com.example.projet

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "usersDB"
        private const val DATABASE_VERSION = 2 // Incremented for new tables
        private const val TABLE_USERS = "users"
        
        // New tables for child activity
        private const val TABLE_CHILD_MESSAGES = "child_messages"
        private const val TABLE_CHILD_SCORES = "child_scores"

        // Users table columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_LOGIN = "login"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NOM = "nom"
        private const val COLUMN_PRENOM = "prenom"
        private const val COLUMN_DATE_NAISSANCE = "date_naissance"
        private const val COLUMN_TELEPHONE = "telephone"
        private const val COLUMN_EMAIL = "email"
        
        // Child messages table columns
        private const val COLUMN_MSG_ID = "id"
        private const val COLUMN_USER_MESSAGE = "user_message"
        private const val COLUMN_LLM_RESPONSE = "llm_response"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_MESSAGE_TYPE = "message_type"
        
        // Child scores table columns
        private const val COLUMN_SCORE_ID = "id"
        private const val COLUMN_GAME_TYPE = "game_type"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_DIFFICULTY_LEVEL = "difficulty_level"
        private const val COLUMN_CHILD_AGE = "child_age"
    }

    data class ScoreEntry(
        val id: Long,
        val gameType: String,
        val score: Int,
        val difficultyLevel: Int,
        val childAge: Int,
        val date: Long
    )

    data class MessageEntry(
        val id: Long,
        val prompt: String,
        val response: String,
        val messageType: String,
        val timestamp: Long
    )

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOGIN TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_NOM TEXT,
                $COLUMN_PRENOM TEXT,
                $COLUMN_DATE_NAISSANCE TEXT,
                $COLUMN_TELEPHONE TEXT,
                $COLUMN_EMAIL TEXT
            )
        """
        
        // Create child messages table
        val createMessagesTableQuery = """
            CREATE TABLE $TABLE_CHILD_MESSAGES (
                $COLUMN_MSG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_MESSAGE TEXT NOT NULL,
                $COLUMN_LLM_RESPONSE TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_MESSAGE_TYPE TEXT DEFAULT 'text'
            )
        """
        
        // Create child scores table
        val createScoresTableQuery = """
            CREATE TABLE $TABLE_CHILD_SCORES (
                $COLUMN_SCORE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_GAME_TYPE TEXT NOT NULL,
                $COLUMN_SCORE INTEGER NOT NULL,
                $COLUMN_DATE INTEGER NOT NULL,
                $COLUMN_DIFFICULTY_LEVEL INTEGER DEFAULT 1,
                $COLUMN_CHILD_AGE INTEGER DEFAULT 8
            )
        """
        
        db.execSQL(createUsersTableQuery)
        db.execSQL(createMessagesTableQuery)
        db.execSQL(createScoresTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add new tables for version 2
            val createMessagesTableQuery = """
                CREATE TABLE $TABLE_CHILD_MESSAGES (
                    $COLUMN_MSG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_MESSAGE TEXT NOT NULL,
                    $COLUMN_LLM_RESPONSE TEXT NOT NULL,
                    $COLUMN_TIMESTAMP INTEGER NOT NULL,
                    $COLUMN_MESSAGE_TYPE TEXT DEFAULT 'text'
                )
            """
            
            val createScoresTableQuery = """
                CREATE TABLE $TABLE_CHILD_SCORES (
                    $COLUMN_SCORE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_GAME_TYPE TEXT NOT NULL,
                    $COLUMN_SCORE INTEGER NOT NULL,
                    $COLUMN_DATE INTEGER NOT NULL,
                    $COLUMN_DIFFICULTY_LEVEL INTEGER DEFAULT 1,
                    $COLUMN_CHILD_AGE INTEGER DEFAULT 8
                )
            """
            
            db.execSQL(createMessagesTableQuery)
            db.execSQL(createScoresTableQuery)
        }
    }

    fun addUser(login: String, password: String, nom: String, prenom: String, dateNaissance: String, telephone: String, email: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_LOGIN, login)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NOM, nom)
            put(COLUMN_PRENOM, prenom)
            put(COLUMN_DATE_NAISSANCE, dateNaissance)
            put(COLUMN_TELEPHONE, telephone)
            put(COLUMN_EMAIL, email)
        }
        val result = db.insert(TABLE_USERS, null, contentValues)
        db.close()
        return result != -1L
    }

    fun checkLoginExists(login: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ?", arrayOf(login))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun checkLoginCredentials(login: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ? AND $COLUMN_PASSWORD = ?", arrayOf(login, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    fun getAllUsers(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS", null)
    }
    
    fun addChildMessage(userMessage: String, llmResponse: String, messageType: String = "text"): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_MESSAGE, userMessage)
            put(COLUMN_LLM_RESPONSE, llmResponse)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_MESSAGE_TYPE, messageType)
        }
        val result = db.insert(TABLE_CHILD_MESSAGES, null, contentValues)
        db.close()
        return result != -1L
    }
    
    fun getRecentChildMessages(limit: Int = 50): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CHILD_MESSAGES ORDER BY $COLUMN_TIMESTAMP DESC LIMIT ?", 
            arrayOf(limit.toString())
        )
    }
    
    fun clearChildMessages(): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_CHILD_MESSAGES, null, null)
        db.close()
        return result > 0
    }
    
    fun addChildScore(gameType: String, score: Int, difficultyLevel: Int, childAge: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_GAME_TYPE, gameType)
            put(COLUMN_SCORE, score)
            put(COLUMN_DATE, System.currentTimeMillis())
            put(COLUMN_DIFFICULTY_LEVEL, difficultyLevel)
            put(COLUMN_CHILD_AGE, childAge)
        }
        val result = db.insert(TABLE_CHILD_SCORES, null, contentValues)
        db.close()
        return result != -1L
    }
    
    fun getChildScores(gameType: String? = null): Cursor {
        val db = this.readableDatabase
        return if (gameType != null) {
            db.rawQuery(
                "SELECT * FROM $TABLE_CHILD_SCORES WHERE $COLUMN_GAME_TYPE = ? ORDER BY $COLUMN_DATE DESC", 
                arrayOf(gameType)
            )
        } else {
            db.rawQuery("SELECT * FROM $TABLE_CHILD_SCORES ORDER BY $COLUMN_DATE DESC", null)
        }
    }
    
    fun getAverageScore(gameType: String, difficultyLevel: Int): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT AVG($COLUMN_SCORE) as avg_score FROM $TABLE_CHILD_SCORES WHERE $COLUMN_GAME_TYPE = ? AND $COLUMN_DIFFICULTY_LEVEL = ?",
            arrayOf(gameType, difficultyLevel.toString())
        )
        
        var averageScore = 0.0
        if (cursor.moveToFirst()) {
            averageScore = cursor.getDouble(cursor.getColumnIndexOrThrow("avg_score"))
        }
        cursor.close()
        db.close()
        return averageScore
    }
    
    fun getProgressStats(): Map<String, Any> {
        val db = this.readableDatabase
        val stats = mutableMapOf<String, Any>()
        
        // Total games played
        val totalGames = db.rawQuery("SELECT COUNT(*) as total FROM $TABLE_CHILD_SCORES", null)
        if (totalGames.moveToFirst()) {
            stats["totalGames"] = totalGames.getInt(totalGames.getColumnIndexOrThrow("total"))
        }
        totalGames.close()
        
        // Best scores by game type
        val bestScores = db.rawQuery(
            "SELECT $COLUMN_GAME_TYPE, MAX($COLUMN_SCORE) as best_score FROM $TABLE_CHILD_SCORES GROUP BY $COLUMN_GAME_TYPE", 
            null
        )
        val gameScores = mutableMapOf<String, Int>()
        while (bestScores.moveToNext()) {
            val gameType = bestScores.getString(bestScores.getColumnIndexOrThrow(COLUMN_GAME_TYPE))
            val bestScore = bestScores.getInt(bestScores.getColumnIndexOrThrow("best_score"))
            gameScores[gameType] = bestScore
        }
        bestScores.close()
        stats["bestScores"] = gameScores
        
        db.close()
        return stats
    }

    // ----------------------------------------------
    // Récupérer tous les scores (child_scores)
    // ----------------------------------------------
    fun getAllScores(): List<ScoreEntry> {
        val scores = mutableListOf<ScoreEntry>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT " +
                    "$COLUMN_SCORE_ID, " +
                    "$COLUMN_GAME_TYPE, " +
                    "$COLUMN_SCORE, " +
                    "$COLUMN_DIFFICULTY_LEVEL, " +
                    "$COLUMN_CHILD_AGE, " +
                    "$COLUMN_DATE " +
                    "FROM $TABLE_CHILD_SCORES " +
                    "ORDER BY $COLUMN_DATE DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_SCORE_ID))
                val gameType = it.getString(it.getColumnIndexOrThrow(COLUMN_GAME_TYPE))
                val score = it.getInt(it.getColumnIndexOrThrow(COLUMN_SCORE))
                val difficulty = it.getInt(it.getColumnIndexOrThrow(COLUMN_DIFFICULTY_LEVEL))
                val age = it.getInt(it.getColumnIndexOrThrow(COLUMN_CHILD_AGE))
                val date = it.getLong(it.getColumnIndexOrThrow(COLUMN_DATE))
                scores.add(ScoreEntry(id, gameType, score, difficulty, age, date))
            }
        }
        cursor.close()
        return scores
    }

    // ----------------------------------------------
    // Récupérer tous les messages (child_messages)
    // ----------------------------------------------
    fun getAllMessages(): List<MessageEntry> {
        val messages = mutableListOf<MessageEntry>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT " +
                    "$COLUMN_MSG_ID, " +
                    "$COLUMN_USER_MESSAGE, " +
                    "$COLUMN_LLM_RESPONSE, " +
                    "$COLUMN_MESSAGE_TYPE, " +
                    "$COLUMN_TIMESTAMP " +
                    "FROM $TABLE_CHILD_MESSAGES " +
                    "ORDER BY $COLUMN_TIMESTAMP DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_MSG_ID))
                val prompt = it.getString(it.getColumnIndexOrThrow(COLUMN_USER_MESSAGE))
                val response = it.getString(it.getColumnIndexOrThrow(COLUMN_LLM_RESPONSE))
                val msgType = it.getString(it.getColumnIndexOrThrow(COLUMN_MESSAGE_TYPE))
                val timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                messages.add(MessageEntry(id, prompt, response, msgType, timestamp))
            }
        }
        cursor.close()
        return messages
    }

}
