package com.example.progressfit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WelcomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String
    private lateinit var todayRoutine: TextView
    private lateinit var dayOfWeek: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        userId = intent.getStringExtra("USER_ID") ?: ""
        val nombre = intent.getStringExtra("NOMBRE") ?: "Usuario"

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        todayRoutine = findViewById(R.id.todayRoutine)
        val addButton = findViewById<ExtendedFloatingActionButton>(R.id.addButton)
        val editButton = findViewById<ExtendedFloatingActionButton>(R.id.editButton)

        welcomeText.text = "Bienvenido, $nombre"

        dayOfWeek = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date())
            .replaceFirstChar { it.titlecase() }

        listenToRoutineUpdates()

        addButton.setOnClickListener {
            val intent = Intent(this, AddRoutineActivity::class.java).apply {
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditRoutineActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("DAY_OF_WEEK", dayOfWeek)
            }
            startActivity(intent)
        }
    }

    private fun listenToRoutineUpdates() {
        db.collection("rutinas").document(userId)
            .collection(dayOfWeek)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    todayRoutine.text = "Error al cargar rutina"
                    Log.e("FirestoreDebug", "Error al obtener rutina: ${e.message}")
                    return@addSnapshotListener
                }

                Log.d("FirestoreDebug", "Ejercicios recuperados: ${snapshots?.size() ?: 0}")

                val sb = StringBuilder("Tu rutina de hoy:\n\n")
                for (document in snapshots!!) {
                    sb.append("• ${document.getString("nombre")}: ")
                        .append("${document.getString("peso")}kg x ")
                        .append("${document.getString("repeticiones")} rep\n")
                }
                todayRoutine.text = if (snapshots!!.isEmpty) "No hay rutina para hoy" else sb.toString()
            }

    }
}
