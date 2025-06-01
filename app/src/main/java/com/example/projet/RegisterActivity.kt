package com.example.projet

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etDateNaissance: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSoumettre: Button

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etDateNaissance = findViewById(R.id.etDateNaissance)
        etTelephone = findViewById(R.id.etTelephone)
        etEmail = findViewById(R.id.etEmail)
        btnSoumettre = findViewById(R.id.btnSoumettre)

        databaseHelper = DatabaseHelper(this)

        btnSoumettre.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val nom = etNom.text.toString().trim()
            val prenom = etPrenom.text.toString().trim()
            val dateNaissance = etDateNaissance.text.toString().trim()
            val telephone = etTelephone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (!login.matches(Regex("^[A-Za-z][A-Za-z0-9]{0,9}$"))) {
                Toast.makeText(this, "Login invalide (lettre en premier, max 10 caractères)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mot de passe trop court (min. 6 caractères)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.checkLoginExists(login)) {
                Toast.makeText(this, "Login déjà pris !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = databaseHelper.addUser(
                login, password, nom, prenom, dateNaissance,
                telephone, email
            )

            if (success) {
                Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
