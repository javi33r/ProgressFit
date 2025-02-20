package com.example.progressfit

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditRoutineActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String
    private lateinit var dayOfWeek: String
    private lateinit var exerciseContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_routine)

        userId = intent.getStringExtra("USER_ID") ?: ""
        dayOfWeek = intent.getStringExtra("DAY_OF_WEEK") ?: ""

        exerciseContainer = findViewById(R.id.exerciseContainer)
        val addExerciseButton = findViewById<Button>(R.id.addExerciseButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

        addExerciseButton.setOnClickListener { addExerciseFields(null) }
        saveButton.setOnClickListener { saveExercises() }

        // Cargar los ejercicios ya guardados
        loadExercises()
    }

    private fun loadExercises() {
        db.collection("rutinas").document(userId).collection(dayOfWeek)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nombre = document.getString("nombre") ?: ""
                    val peso = document.getString("peso") ?: "0"
                    val reps = document.getString("repeticiones") ?: "0"
                    addExerciseFields(ExerciseData(document.id, nombre, peso, reps))
                }
            }
    }

    private fun addExerciseFields(existingExercise: ExerciseData?) {
        val exerciseView = layoutInflater.inflate(R.layout.item_exercise, null)

        val nameEditText = exerciseView.findViewById<EditText>(R.id.exerciseNameEditText)
        val weightEditText = exerciseView.findViewById<EditText>(R.id.exerciseWeightEditText)
        val repsEditText = exerciseView.findViewById<EditText>(R.id.exerciseRepsEditText)

        existingExercise?.let {
            nameEditText.setText(it.nombre)
            weightEditText.setText(it.peso)
            repsEditText.setText(it.repeticiones)
            exerciseView.tag = it.id  // Guardamos el ID del documento de Firestore
        }

        exerciseContainer.addView(exerciseView)
    }

    private fun saveExercises() {
        val batch = db.batch()
        val dayRef = db.collection("rutinas").document(userId).collection(dayOfWeek)

        for (i in 0 until exerciseContainer.childCount) {
            val view = exerciseContainer.getChildAt(i)
            val nombre = view.findViewById<EditText>(R.id.exerciseNameEditText).text.toString()
            val peso = view.findViewById<EditText>(R.id.exerciseWeightEditText).text.toString()
            val reps = view.findViewById<EditText>(R.id.exerciseRepsEditText).text.toString()

            if (nombre.isNotEmpty()) {
                val exerciseData = mapOf(
                    "nombre" to nombre,
                    "peso" to peso.ifEmpty { "0" },
                    "repeticiones" to reps.ifEmpty { "0" }
                )

                val docId = view.tag as? String
                val docRef = docId?.let { dayRef.document(it) } ?: dayRef.document()
                batch.set(docRef, exerciseData)
            }
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class ExerciseData(val id: String, val nombre: String, val peso: String, val repeticiones: String)
}
