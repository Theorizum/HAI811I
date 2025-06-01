package com.example.projet

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        databaseHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (databaseHelper.checkLoginCredentials(login, password)) {
                // On récupère le rôle désiré passé en extra (default "Parent")
                val role = intent.getStringExtra("role") ?: "Parent"
                // On enregistre la session
                SessionManager.setLogin(this, true, role)

                Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                // En fonction du rôle, on redirige
                if (role == "Parent") {
                    startActivity(Intent(this, ParentActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, "Login ou mot de passe incorrect !", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}