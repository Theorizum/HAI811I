package com.example.projet

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class ParentVerification : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: EditText
    private lateinit var btnVerify: Button

    private var correctAnswer: Int = 0
    private var targetPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parent_verification)

        tvQuestion = findViewById(R.id.tvQuestion)
        etAnswer = findViewById(R.id.etAnswer)
        btnVerify = findViewById(R.id.btnVerify)

        targetPage = intent.getIntExtra("targetPage", 0)

        // Génère une question simple
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)
        val c = Random.nextInt(1, 10)
        correctAnswer = a + (b * c)
        tvQuestion.text = "Combien font $a + $b x $c?"

        btnVerify.setOnClickListener {
            val answer = etAnswer.text.toString().toIntOrNull()
            if (answer == correctAnswer) {
                goToTargetPage(targetPage)
            } else {
                Toast.makeText(this, "Mauvaise réponse, essayez encore.", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun goToTargetPage(page: Int) {
        when (page) {
            0 -> startActivity(Intent(this, MainActivity::class.java))
            1 -> startActivity(Intent(this, ParentActivity::class.java))
            else -> Toast.makeText(this, "Page inconnue", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}