package com.example.projet

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ErrorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val errorText: TextView = findViewById(R.id.errorText)
        val backButton: Button = findViewById(R.id.backButton)

        val errorCode = intent.getIntExtra("error_code", 0)

        val message = when (errorCode) {
            1 -> getString(R.string.error_child_requires_login)
            2 -> "Cette section est temporairement indisponible."
            else -> "Une erreur inconnue est survenue."
        }

        errorText.text = message

        backButton.setOnClickListener {
            finish()
        }
    }
}
