package com.example.passoff

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginActivity if not logged in
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener {
            addPasswordDialogue()
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val progressIndicator: CircularProgressIndicator = findViewById(R.id.circularProgressIndicator)

        val loadDataTask = LoadDataTask(this)
        loadDataTask.setRecyclerView(recyclerView)
        loadDataTask.setCircularProgressIndicator(progressIndicator)
        loadDataTask.execute()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Home action
                    Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_search -> {
                    // Handle Search action
                    Toast.makeText(this, "Search selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_quickshare -> {
                    // Handle Quickshare action
                    Toast.makeText(this, "Quickshare selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
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

                val jsonUtils = JsonUtils(this)
                val passItem = PassItem(title, username, password, domain)
                jsonUtils.addPassItem(this, passItem)
                // Show a toast or handle the data
                val intent = Intent(this, MainActivity::class.java)
                finish() // Close the current activity
                startActivity(intent) // Start the new activity
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
