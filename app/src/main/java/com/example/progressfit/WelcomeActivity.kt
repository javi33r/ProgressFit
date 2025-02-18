package com.example.progressfit

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class WelcomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val todayRoutine = findViewById<TextView>(R.id.todayRoutine)
        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        // Obtener ID del usuario actual
        val userId = auth.currentUser?.uid ?: ""

        // Obtener el día actual
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

        // Obtener la rutina del día actual desde Firebase
        db.collection("rutinas").document(userId).collection(dayOfWeek).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    todayRoutine.text = "No tienes ejercicios asignados para hoy."
                } else {
                    val exercises = StringBuilder("Esta es tu rutina de hoy:\n\n")
                    for (document in documents) {
                        val nombre = document.getString("nombre") ?: "Ejercicio"
                        val peso = document.getString("peso") ?: "0 kg"
                        val repeticiones = document.getString("repeticiones") ?: "0 rep"
                        exercises.append("$nombre - $peso - $repeticiones\n")
                    }
                    todayRoutine.text = exercises.toString()
                }
            }

        // Acción del botón flotante
        addButton.setOnClickListener {
            val intent = Intent(this, AddRoutineActivity::class.java)
            startActivity(intent)
        }
    }
}
