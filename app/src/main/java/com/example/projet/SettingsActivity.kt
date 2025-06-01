package com.example.projet

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var childNameEditText: EditText
    private lateinit var ageSeekBar: SeekBar
    private lateinit var ageValueTextView: TextView
    private lateinit var difficultySeekBar: SeekBar
    private lateinit var difficultyValueTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initializeViews()
        loadCurrentSettings()
        setupSeekBars()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        childNameEditText = findViewById(R.id.childNameEditText)
        ageSeekBar = findViewById(R.id.ageSeekBar)
        ageValueTextView = findViewById(R.id.ageValueTextView)
        difficultySeekBar = findViewById(R.id.difficultySeekBar)
        difficultyValueTextView = findViewById(R.id.difficultyValueTextView)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
    }
    
    private fun loadCurrentSettings() {
        val settings = SessionManager.getChildSettings(this)
        
        childNameEditText.setText(settings["name"] as String)
        val age = settings["age"] as Int
        val difficulty = settings["difficultyLevel"] as Int
        
        ageSeekBar.progress = age - 3 // SeekBar starts at 0, age starts at 3
        difficultySeekBar.progress = difficulty - 1 // SeekBar starts at 0, difficulty starts at 1
        
        updateAgeDisplay(age)
        updateDifficultyDisplay(difficulty)
    }
    
    private fun setupSeekBars() {
        // Age SeekBar (3-15 years)
        ageSeekBar.max = 12 // 15 - 3 = 12
        ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val age = progress + 3
                updateAgeDisplay(age)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Difficulty SeekBar (1-3)
        difficultySeekBar.max = 2 // 3 - 1 = 2
        difficultySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val difficulty = progress + 1
                updateDifficultyDisplay(difficulty)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveSettings()
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun updateAgeDisplay(age: Int) {
        ageValueTextView.text = "$age ans"
    }
    
    private fun updateDifficultyDisplay(difficulty: Int) {
        val difficultyText = when (difficulty) {
            1 -> "Facile 🟢"
            2 -> "Moyen 🟡"
            3 -> "Difficile 🔴"
            else -> "Facile"
        }
        difficultyValueTextView.text = difficultyText
    }
    
    private fun saveSettings() {
        val childName = childNameEditText.text.toString().trim()
        val age = ageSeekBar.progress + 3
        val difficulty = difficultySeekBar.progress + 1
        
        if (childName.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom pour l'enfant", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save to SessionManager
        SessionManager.setChildSettings(this, age, difficulty, childName)
        
        Toast.makeText(this, "Paramètres sauvegardés ! 🎉", Toast.LENGTH_SHORT).show()
        finish()
    }
} 