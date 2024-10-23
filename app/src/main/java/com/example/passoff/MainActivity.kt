package com.example.passoff

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener    {
            addPasswordDialogue()
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val progressIndicator: CircularProgressIndicator = findViewById(R.id.circularProgressIndicator)

        val loadDataTask = LoadDataTask(this)
        loadDataTask.setRecyclerView(recyclerView)
        loadDataTask.setCircularProgressIndicator(progressIndicator)
        loadDataTask.execute()




    }

    private fun addPasswordDialogue() {
        // Inflate the custom layout that contains multiple EditText fields
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)

        // Create references to the input fields in the custom layout
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val emailEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Your Details")
            .setView(dialogView)  // Set the custom layout in the dialog
            .setPositiveButton("Submit") { dialog, _ ->
                // Retrieve user input from the EditText fields
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()
                val phone = phoneEditText.text.toString()

                // Show a toast or handle the data
                Toast.makeText(this, "Name: $name\nEmail: $email\nPhone: $phone", Toast.LENGTH_LONG).show()

                dialog.dismiss()  // Close the dialog
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()  // Close the dialog without doing anything
            }
            .create()

        // Show the dialog
        dialog.show()
    }
}