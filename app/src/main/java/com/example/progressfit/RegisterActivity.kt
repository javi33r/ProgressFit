package com.example.progressfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var generatedUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nombreEditText = findViewById<EditText>(R.id.nombreEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                generateUserId { userId ->
                    generatedUserId = userId
                    val userData = hashMapOf(
                        "creado_en" to Timestamp.now(),
                        "email" to email,
                        "id" to userId,
                        "nombre" to nombre,
                        "password" to password // ¡Atención! Almacenar contraseñas en texto plano es INSECURE
                    )

                    db.collection("users").document(userId.toString())
                        .set(userData)
                        .addOnSuccessListener {
                            createRutinasDocument(userId)
                            Toast.makeText(this, "Registro exitoso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateUserId(callback: (Int) -> Unit) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val nextId = result.size() + 1
                callback(nextId)
            }
    }

    private fun createRutinasDocument(userId: Int) {
        val rutinaData = hashMapOf(
            "creado_en" to Timestamp.now()
        )

        db.collection("rutinas").document(userId.toString())
            .set(rutinaData)
    }
}