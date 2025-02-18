package com.example.progressfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Referencias a los elementos del layout
        val nombreEditText = findViewById<EditText>(R.id.nombreEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                generateUserId { id ->
                    if (id != null) {
                        val creadoEn = Timestamp.now()

                        val userData = hashMapOf(
                            "creado_en" to creadoEn,
                            "email" to email,
                            "id" to id,
                            "nombre" to nombre,
                            "password" to password
                        )

                        // Guardar datos del usuario en Firestore (colección users)
                        db.collection("users").document(id.toString()).set(userData)
                            .addOnSuccessListener {
                                // Crear documento en "rutinas" con solo "creado_en"
                                val rutinaData = hashMapOf("creado_en" to creadoEn)
                                db.collection("rutinas").document(id.toString())
                                    .set(rutinaData) // ✅ Solo un documento, sin subcolecciones

                                Toast.makeText(this, "Cuenta creada correctamente.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al crear la cuenta: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error al generar el ID del usuario.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateUserId(callback: (Int?) -> Unit) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val nextId = result.size() + 1
                callback(nextId)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
