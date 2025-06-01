package com.example.projet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParentActivity : AppCompatActivity() {

    // Statistiques
    private lateinit var tvSuccessRate: TextView
    private lateinit var tvQuizCounts: TextView
    private lateinit var tvStoryCount: TextView
    private lateinit var tvStoryThemes: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var quizProgressBar: ProgressBar

    // Nom modifiable
    private lateinit var etChildName: EditText
    private lateinit var btnSaveChildName: Button

    // Réglages (âge + difficulté)
    private lateinit var tvChildAgeValue: TextView
    private lateinit var btnDecreaseAge: Button
    private lateinit var btnIncreaseAge: Button

    private lateinit var tvDifficultyValue: TextView
    private lateinit var btnDecreaseDifficulty: Button
    private lateinit var btnIncreaseDifficulty: Button

    // Déconnexion
    private lateinit var btnLogout: Button

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent)

        // Liaison des vues statistiques
        tvSuccessRate   = findViewById(R.id.tvSuccessRate)
        tvQuizCounts    = findViewById(R.id.tvQuizCounts)
        quizProgressBar = findViewById(R.id.progressBarQuiz)
        tvStoryCount    = findViewById(R.id.tvStoryCount)
        tvStoryThemes   = findViewById(R.id.tvStoryThemes)
        progressBar     = findViewById(R.id.progressBarDashboard)

        // Liaison des vues pour le nom modifiable
        etChildName      = findViewById(R.id.etChildName)
        btnSaveChildName = findViewById(R.id.btnSaveChildName)

        // Liaison des vues de réglages (âge + difficulté)
        tvChildAgeValue       = findViewById(R.id.tvChildAgeValue)
        btnDecreaseAge        = findViewById(R.id.btnDecreaseAge)
        btnIncreaseAge        = findViewById(R.id.btnIncreaseAge)

        tvDifficultyValue     = findViewById(R.id.tvDifficultyValue)
        btnDecreaseDifficulty = findViewById(R.id.btnDecreaseDifficulty)
        btnIncreaseDifficulty = findViewById(R.id.btnIncreaseDifficulty)

        // Liaison du bouton Déconnexion
        btnLogout             = findViewById(R.id.btnLogout)

        databaseHelper = DatabaseHelper(this)

        // 1) Initialisation du nom de l'enfant depuis SessionManager
        val savedName = SessionManager.getChildName(this)
        etChildName.setText(savedName)

        // 2) Sauvegarder le nouveau nom quand on clique sur “Enregistrer”
        btnSaveChildName.setOnClickListener {
            val newName = etChildName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Le nom ne peut pas être vide", Toast.LENGTH_SHORT).show()
            } else {
                SessionManager.setChildName(this, newName)
                Toast.makeText(this, "Nom de l'enfant enregistré", Toast.LENGTH_SHORT).show()
            }
        }

        // 3) Initialisation de l'âge et affichage
        val savedAge = SessionManager.getChildAge(this)
        tvChildAgeValue.text = "$savedAge ans"
        btnDecreaseAge.setOnClickListener {
            var age = SessionManager.getChildAge(this)
            if (age > 3) {
                age--
                SessionManager.setChildAge(this, age)
                tvChildAgeValue.text = "$age ans"
            }
        }
        btnIncreaseAge.setOnClickListener {
            var age = SessionManager.getChildAge(this)
            if (age < 18) {
                age++
                SessionManager.setChildAge(this, age)
                tvChildAgeValue.text = "$age ans"
            }
        }

        // 4) Initialisation de la difficulté et affichage
        val savedDifficulty = SessionManager.getDifficultyLevel(this)
        tvDifficultyValue.text = savedDifficulty.toString()
        btnDecreaseDifficulty.setOnClickListener {
            var level = SessionManager.getDifficultyLevel(this)
            if (level > 1) {
                level--
                SessionManager.setDifficultyLevel(this, level)
                tvDifficultyValue.text = level.toString()
            }
        }
        btnIncreaseDifficulty.setOnClickListener {
            var level = SessionManager.getDifficultyLevel(this)
            if (level < 3) {
                level++
                SessionManager.setDifficultyLevel(this, level)
                tvDifficultyValue.text = level.toString()
            }
        }

        // 5) Déconnexion
        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show()
            // Retour à la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Charger les statistiques initiales
        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchir les stats à chaque retour
        loadDashboard()
    }

    private fun loadDashboard() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Récupérer tous les scores
            val allScores = databaseHelper.getAllScores()
            val quizScores = allScores.filter { it.gameType == "quiz_correction" }
            val totalQuiz = quizScores.size
            val successfulQuizCount = quizScores.count { it.score >= 10 }
            val successRate = if (totalQuiz == 0) 0 else (successfulQuizCount * 100) / totalQuiz

            // 2. Récupérer tous les messages d’histoires
            val allMessages = databaseHelper.getAllMessages()
            val storyMessages = allMessages.filter { it.messageType == "story" }
            val storyCount = storyMessages.size

            // 3. Extraire les thèmes demandés
            val themesList = mutableListOf<String>()
            val regexTheme = Regex("""thème\s*"(.*?)"""")
            storyMessages.forEach { entry ->
                val match = regexTheme.find(entry.prompt)
                match?.let {
                    themesList.add(it.groupValues[1])
                }
            }
            val themesCountMap: Map<String, Int> = themesList.groupingBy { it }.eachCount()
            val themesText = if (themesCountMap.isEmpty()) {
                "Aucun"
            } else {
                themesCountMap.entries
                    .sortedByDescending { it.value }
                    .joinToString(", ") { "${it.key} (${it.value})" }
            }

            runOnUiThread {
                progressBar.visibility = View.GONE

                // Mise à jour des vues Quiz
                tvQuizCounts.text = "Quiz passés : $totalQuiz, Réussites : $successfulQuizCount"
                tvSuccessRate.text = "Taux de réussite : $successRate %"
                quizProgressBar.progress = successRate

                // Mise à jour des vues Histoires
                tvStoryCount.text = "Nombre d'histoires : $storyCount"
                tvStoryThemes.text = "Thèmes : $themesText"
            }
        }
    }
}
