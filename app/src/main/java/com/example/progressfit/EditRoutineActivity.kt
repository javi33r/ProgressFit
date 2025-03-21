package com.example.progressfit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class EditRoutineActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String
    private lateinit var dayOfWeek: String
    private lateinit var exerciseContainer: LinearLayout
    private lateinit var addExerciseButton: Button
    private val exercises = mutableListOf<ExerciseData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine)

        userId = intent.getStringExtra("USER_ID") ?: ""
        dayOfWeek = intent.getStringExtra("DAY_OF_WEEK") ?: ""

        val cardView = findViewById<MaterialCardView>(R.id.exerciseContainer)
        exerciseContainer = cardView.getChildAt(0) as LinearLayout
        addExerciseButton = findViewById(R.id.addExerciseButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        addExerciseButton.setOnClickListener { addExerciseFields(null) }
        saveButton.setOnClickListener { saveExercises() }

        loadExercises()
    }

    private fun loadExercises() {
        db.collection("rutinas").document(userId).collection(dayOfWeek)
            .get()
            .addOnSuccessListener { documents ->
                exercises.clear()
                for (document in documents) {
                    val exercise = ExerciseData(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        peso = document.getString("peso") ?: "0",
                        repeticiones = document.getString("repeticiones") ?: "0"
                    )
                    exercises.add(exercise)
                    addExerciseFields(exercise)
                }

                // Si ya hay 25 ejercicios, deshabilita el botón de añadir más
                if (exercises.size >= 25) {
                    addExerciseButton.isEnabled = false
                    Toast.makeText(this, "No puedes añadir más de 25 ejercicios", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addExerciseFields(exercise: ExerciseData?) {
        if (exerciseContainer.childCount >= 25) {
            Toast.makeText(this, "No puedes añadir más de 25 ejercicios", Toast.LENGTH_SHORT).show()
            return
        }

        val exerciseView = layoutInflater.inflate(R.layout.item_exercise, null)
        val nameEditText = exerciseView.findViewById<EditText>(R.id.exerciseNameEditText)
        val weightEditText = exerciseView.findViewById<EditText>(R.id.exerciseWeightEditText)
        val repsEditText = exerciseView.findViewById<EditText>(R.id.exerciseRepsEditText)
        val deleteButton = exerciseView.findViewById<Button>(R.id.deleteExerciseButton)

        if (exercise != null) {
            nameEditText.setText(exercise.nombre)
            weightEditText.setText(exercise.peso)
            repsEditText.setText(exercise.repeticiones)
        }

        deleteButton.setOnClickListener {
            exerciseContainer.removeView(exerciseView)

            // Rehabilita el botón si se elimina un ejercicio y hay menos de 25
            if (exerciseContainer.childCount < 25) {
                addExerciseButton.isEnabled = true
            }
        }

        exerciseContainer.addView(exerciseView)

        // Si llegamos a 25, deshabilita el botón
        if (exerciseContainer.childCount >= 25) {
            addExerciseButton.isEnabled = false
            Toast.makeText(this, "No puedes añadir más de 25 ejercicios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveExercises() {
        if (exerciseContainer.childCount > 25) {
            Toast.makeText(this, "No puedes guardar más de 25 ejercicios", Toast.LENGTH_SHORT).show()
            return
        }

        val dayRef = db.collection("rutinas").document(userId).collection(dayOfWeek)

        // 🛑 Eliminar todos los ejercicios previos antes de guardar los nuevos
        dayRef.get().addOnSuccessListener { documents ->
            val batch = db.batch()

            // Eliminar todos los documentos previos
            for (document in documents) {
                batch.delete(document.reference)
            }

            // Guardar los nuevos ejercicios
            for (i in 0 until exerciseContainer.childCount) {
                val view = exerciseContainer.getChildAt(i)
                val nombre = view.findViewById<EditText>(R.id.exerciseNameEditText).text.toString()
                val peso = view.findViewById<EditText>(R.id.exerciseWeightEditText).text.toString()
                val reps = view.findViewById<EditText>(R.id.exerciseRepsEditText).text.toString()

                if (nombre.isNotEmpty()) {
                    val newDocRef = dayRef.document()
                    batch.set(newDocRef, mapOf(
                        "nombre" to nombre,
                        "peso" to peso.ifEmpty { "0" },
                        "repeticiones" to reps.ifEmpty { "0" }
                    ))
                }
            }

            batch.commit()
                .addOnSuccessListener {
                    Toast.makeText(this, "Rutina actualizada para $dayOfWeek", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }


    data class ExerciseData(val id: String, val nombre: String, val peso: String, val repeticiones: String)
}
