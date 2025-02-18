package com.example.progressfit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddRoutineActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val exerciseList = mutableListOf<Map<String, String>>() // Lista para almacenar los ejercicios dinámicos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine)

        val trainingName = findViewById<EditText>(R.id.trainingNameEditText)
        val exerciseContainer = findViewById<LinearLayout>(R.id.exerciseContainer)
        val addExerciseButton = findViewById<Button>(R.id.addExerciseButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Añadir un nuevo ejercicio dinámicamente
        addExerciseButton.setOnClickListener {
            addExerciseFields(exerciseContainer)
        }

        // Guardar rutina en Firebase
        saveButton.setOnClickListener {
            val trainingNameText = trainingName.text.toString().trim()
            val dayOfWeek = findViewById<Spinner>(R.id.dayOfWeekSpinner).selectedItem.toString()

            if (trainingNameText.isNotEmpty() && exerciseList.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: ""
                val dayRef = db.collection("rutinas").document(userId).collection(dayOfWeek)

                for (exercise in exerciseList) {
                    dayRef.add(exercise)
                }

                Toast.makeText(this, "Entrenamiento guardado para $dayOfWeek", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para añadir recuadros dinámicamente
    private fun addExerciseFields(container: LinearLayout) {
        val exerciseView = layoutInflater.inflate(R.layout.item_exercise, null)

        val exerciseName = exerciseView.findViewById<EditText>(R.id.exerciseNameEditText)
        val exerciseWeight = exerciseView.findViewById<EditText>(R.id.exerciseWeightEditText)
        val exerciseReps = exerciseView.findViewById<EditText>(R.id.exerciseRepsEditText)

        container.addView(exerciseView)

        exerciseList.add(
            mapOf(
                "nombre" to (exerciseName.text.toString()),
                "peso" to (exerciseWeight.text.toString()),
                "repeticiones" to (exerciseReps.text.toString())
            )
        )
    }
}
