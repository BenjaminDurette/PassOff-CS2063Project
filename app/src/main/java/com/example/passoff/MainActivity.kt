package com.example.passoff

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var entryNameText: String? = ""
    private var usernameText: String? = ""
    private var passwordText: String? = ""
    private var descriptionText: String? = ""
    private var dbHandler: DBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DBHandler(this@MainActivity)

        val addPasswordButton = findViewById<Button>(R.id.addpassword_button)
        addPasswordButton.setOnClickListener {
            entryNameText = findViewById<EditText>(R.id.entryname_input).text.toString()
            usernameText = findViewById<EditText>(R.id.username_input).text.toString()
            passwordText = findViewById<EditText>(R.id.password_input).text.toString()
            descriptionText = findViewById<EditText>(R.id.description_input).text.toString()

            if (entryNameText!!.isEmpty() || usernameText!!.isEmpty() || passwordText!!.isEmpty() || descriptionText!!.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please fill all fields..", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            this.dbHandler!!.addNewPassword(entryNameText, usernameText, passwordText, descriptionText)

            Toast.makeText(this@MainActivity, "Password has been added.", Toast.LENGTH_SHORT).show()
            findViewById<EditText>(R.id.entryname_input).setText("")
            findViewById<EditText>(R.id.username_input).setText("")
            findViewById<EditText>(R.id.password_input).setText("")
            findViewById<EditText>(R.id.description_input).setText("")
        }
    }
}