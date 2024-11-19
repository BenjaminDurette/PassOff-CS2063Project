package com.example.passoff

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {

    private var dbHandler: DBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener    {
            addPasswordDialogue()
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressIndicator = findViewById(R.id.circularProgressIndicator)

        loadData()
    }

    private fun loadData() {
        val loadDataTask = LoadDataTask(this)
        loadDataTask.setRecyclerView(recyclerView)
        loadDataTask.setCircularProgressIndicator(progressIndicator)
        loadDataTask.execute()
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
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                // Retrieve user input from the EditText fields
                if (titleEditText.text.isNotEmpty() && usernameEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                    if (domainEditText.text.isEmpty()) {
                        domainEditText.setText("N/A")
                    }
                    val title = titleEditText.text.toString()
                    val username = usernameEditText.text.toString()
                    val password = passwordEditText.text.toString()
                    val domain = domainEditText.text.toString()

                    dbHandler = DBHandler(this)
                    this.dbHandler!!.addNewPassword(title, username, password, domain)
                }

                dialog.dismiss()

                Toast.makeText(this, "Password has been added.", Toast.LENGTH_SHORT).show()

                loadData()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.show()
    }

    companion object {
        lateinit var progressIndicator: CircularProgressIndicator
        lateinit var recyclerView: RecyclerView
    }
}