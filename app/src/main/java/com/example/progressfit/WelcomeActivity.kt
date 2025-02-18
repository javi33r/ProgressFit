package com.example.progressfit

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WelcomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        userId = intent.getStringExtra("USER_ID") ?: ""
        val nombre = intent.getStringExtra("NOMBRE") ?: "Usuario"

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val todayRoutine = findViewById<TextView>(R.id.todayRoutine)
        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        welcomeText.text = "Bienvenido, $nombre"

        val dayOfWeek = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date())
            .replaceFirstChar { it.titlecase() }

        // Cargar rutina desde la estructura original
        db.collection("rutinas").document(userId)
            .collection(dayOfWeek)
            .get()
            .addOnSuccessListener { documents ->
                val sb = StringBuilder("Tu rutina de hoy:\n\n")
                for (document in documents) {
                    sb.append("• ${document.getString("nombre")}: ")
                        .append("${document.getString("peso")}kg x ")
                        .append("${document.getString("repeticiones")} rep\n")
                }
                todayRoutine.text = if (documents.isEmpty) "No hay rutina para hoy" else sb.toString()
            }

        addButton.setOnClickListener {
            val intent = Intent(this, AddRoutineActivity::class.java).apply {
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }
    }
}