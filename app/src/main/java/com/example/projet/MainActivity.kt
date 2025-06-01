package com.example.projet

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ToggleButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var childToggle: ToggleButton
    private lateinit var parentToggle: ToggleButton
    private lateinit var playButton: ImageButton

    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        childToggle = findViewById(R.id.childToggle)
        parentToggle = findViewById(R.id.parentToggle)
        playButton = findViewById(R.id.playButton)

        childToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                parentToggle.isChecked = false
                selectedRole = "Enfant"
                childToggle.setBackgroundResource(R.drawable.paw_button)
                parentToggle.setBackgroundResource(R.drawable.paw_button_off)
            } else if (!parentToggle.isChecked) {
                selectedRole = null
                childToggle.setBackgroundResource(R.drawable.paw_button_off)
            }
        }

        parentToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                childToggle.isChecked = false
                selectedRole = "Parent"
                parentToggle.setBackgroundResource(R.drawable.paw_button)
                childToggle.setBackgroundResource(R.drawable.paw_button_off)
            } else if (!childToggle.isChecked) {
                selectedRole = null
                parentToggle.setBackgroundResource(R.drawable.paw_button_off)
            }
        }

        playButton.setOnClickListener {
            if (selectedRole == null) {
                Toast.makeText(this, "Veuillez sélectionner Enfant ou Parent", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Lancement en mode $selectedRole", Toast.LENGTH_SHORT).show()

            // Si pas connecté
            if (!SessionManager.isLoggedIn(this)) {
                if (selectedRole == "Parent") {
                    // Parent non connecté → page de login Parent
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    // Enfant non connecté → erreur
                    val intent = Intent(this, ErrorActivity::class.java)
                    intent.putExtra("error_code", 1)
                    startActivity(intent)
                }
                return@setOnClickListener
            }

            // Si connecté
            if (selectedRole == "Parent") {
                // Parent connecté → ParentVerification (targetPage = 1)
                val intent = Intent(this, ParentVerification::class.java)
                intent.putExtra("targetPage", 1)
                startActivity(intent)
            } else {
                // Enfant connecté → ActivitySelection
                startActivity(Intent(this, ChildActivity::class.java))
            }
        }
    }
}
