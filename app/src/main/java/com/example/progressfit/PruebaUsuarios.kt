package com.example.progressfit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    //Nos conectamos a la base de datos
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prueba_usuarios)

        // Referencias a los elementos del layout
        val nombreEditText = findViewById<EditText>(R.id.nombreEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val createUserButton = findViewById<Button>(R.id.createUserButton)

        // Acción del botón de crear usuario
        createUserButton.setOnClickListener {
            val nombre = nombreEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                generateUserId { id ->
                    if (id != null) {
                        createUserInFirestore(nombre, email, password, id)
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
                // Generar el próximo ID basado en el tamaño actual de la colección
                val nextId = (result.size() + 1)
                callback(nextId)
            }
            .addOnFailureListener {
                callback(null) // Error al obtener los datos
            }
    }

    private fun createUserInFirestore(nombre: String, email: String, password: String, id: Int) {
        // Crear un mapa con los datos del usuario
        val userData = hashMapOf(
            "creado_en" to Timestamp.now(),
            "email" to email,
            "id" to id,
            "nombre" to nombre,
            "password" to password
        )

        //Guardamos los datos en la base de datos
        db.collection("users").document(id.toString()).set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario creado correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
