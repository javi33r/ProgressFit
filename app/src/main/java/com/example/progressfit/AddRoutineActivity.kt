package com.example.progressfit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddRoutineActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine)

        userId = intent.getStringExtra("USER_ID") ?: ""
        val exerciseContainer = findViewById<LinearLayout>(R.id.exerciseContainer)
        val addExerciseButton = findViewById<Button>(R.id.addExerciseButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        repeat(4) { addExerciseFields(exerciseContainer) }

        addExerciseButton.setOnClickListener {
            addExerciseFields(exerciseContainer)
        }

        saveButton.setOnClickListener {
            val exercises = mutableListOf<Map<String, String>>()

            for (i in 0 until exerciseContainer.childCount) {
                val view = exerciseContainer.getChildAt(i)
                val nombre = view.findViewById<EditText>(R.id.exerciseNameEditText).text.toString()
                val peso = view.findViewById<EditText>(R.id.exerciseWeightEditText).text.toString()
                val reps = view.findViewById<EditText>(R.id.exerciseRepsEditText).text.toString()

                if (nombre.isNotEmpty()) {
                    exercises.add(mapOf(
                        "nombre" to nombre,
                        "peso" to peso.ifEmpty { "0" },
                        "repeticiones" to reps.ifEmpty { "0" }
                    ))
                }
            }

            if (exercises.isNotEmpty()) {
                showDaySelectionDialog(exercises)
            } else {
                Toast.makeText(this, "Añade al menos un ejercicio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDaySelectionDialog(exercises: List<Map<String, String>>) {
        val days = resources.getStringArray(R.array.dias_semana)
        AlertDialog.Builder(this)
            .setTitle("Seleccionar día")
            .setSingleChoiceItems(days, -1) { dialog, which ->
                val selectedDay = days[which]
                saveExercises(selectedDay, exercises)
                dialog.dismiss()
            }
            .show()
    }

    private fun saveExercises(day: String, exercises: List<Map<String, String>>) {
        val batch = db.batch()
        val dayRef = db.collection("rutinas").document(userId).collection(day)

        exercises.forEach { exercise ->
            val newDocRef = dayRef.document()
            batch.set(newDocRef, exercise)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Rutina guardada para $day", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addExerciseFields(container: LinearLayout) {
        val exerciseView = layoutInflater.inflate(R.layout.item_exercise, null)
        container.addView(exerciseView)
    }
}