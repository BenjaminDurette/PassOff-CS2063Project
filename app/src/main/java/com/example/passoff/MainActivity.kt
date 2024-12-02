package com.example.passoff

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var passwordAdapter: PasswordAdapter
    private var dbHandler: DBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener    {
            addPasswordDialogue()
        }

    }

    private fun addPasswordDialogue() {
        // Inflate the custom layout that contains multiple EditText fields
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input, null)

        // Create references to the input fields in the custom layout
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)
        val domainEditText = dialogView.findViewById<EditText>(R.id.domainEditText)

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter New Password Information")
            .setView(dialogView)  // Set the custom layout in the dialog
            .setPositiveButton("Submit") { dialog, _ ->
                // Retrieve user input from the EditText fields
                val title = titleEditText.text.toString()
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                val domain = domainEditText.text.toString()

                // Show a toast or handle the data
                dbHandler = DBHandler(this)
                this.dbHandler!!.addNewPassword(title, username, password, domain)

                dialog.dismiss()  // Close the dialog

                Toast.makeText(this, "Password has been added.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()  // Close the dialog without doing anything
            }
            .create()

        // Show the dialog
        dialog.show()
    }
}