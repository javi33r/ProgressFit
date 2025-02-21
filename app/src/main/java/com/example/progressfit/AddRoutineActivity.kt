package com.example.progressfit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class AddRoutineActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine)

        userId = intent.getStringExtra("USER_ID") ?: ""
        val exerciseCardView = findViewById<MaterialCardView>(R.id.exerciseContainer)
        val exerciseContainer = exerciseCardView.getChildAt(0) as LinearLayout
        val addExerciseButton = findViewById<Button>(R.id.addExerciseButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        repeat(4) { addExerciseFields(exerciseContainer) }

        addExerciseButton.setOnClickListener {
            if (exerciseContainer.childCount < 25) {
                addExerciseFields(exerciseContainer)
            } else {
                Toast.makeText(this, "No puedes añadir más de 25 ejercicios", Toast.LENGTH_SHORT).show()
            }
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

    private fun addExerciseFields(container: LinearLayout) {
        val exerciseView = layoutInflater.inflate(R.layout.item_exercise, null)
        val deleteButton = exerciseView.findViewById<Button>(R.id.deleteExerciseButton)

        deleteButton.setOnClickListener {
            container.removeView(exerciseView)
        }

        container.addView(exerciseView)
    }

    private fun showDaySelectionDialog(exercises: List<Map<String, String>>) {
        val days = resources.getStringArray(R.array.dias_semana)
        AlertDialog.Builder(this)
            .setTitle("Seleccionar día")
            .setSingleChoiceItems(days, -1) { dialog, which ->
                val selectedDay = days[which]
                dialog.dismiss()
                checkIfRoutineExists(selectedDay, exercises)
            }
            .show()
    }

    private fun checkIfRoutineExists(day: String, exercises: List<Map<String, String>>) {
        val dayRef = db.collection("rutinas").document(userId).collection(day)

        dayRef.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                AlertDialog.Builder(this)
                    .setTitle("Rutina existente")
                    .setMessage("Ya tiene una rutina para este día. ¿Quiere reemplazarla?")
                    .setPositiveButton("Reemplazar") { _, _ ->
                        saveExercises(day, exercises)
                    }
                    .setNegativeButton("Volver") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                saveExercises(day, exercises)
            }
        }
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
    }
}
