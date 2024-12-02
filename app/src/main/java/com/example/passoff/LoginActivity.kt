package com.example.passoff

import UserDBHandler
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        const val CREATE_PASSWORD_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val needsPasswordSetup = true // Replace with logic to add password setup

        if (needsPasswordSetup) {
            val intent = Intent(this, CreatePasswordActivity::class.java)
            startActivityForResult(intent, CREATE_PASSWORD_REQUEST)
        } else {
            setupLogin()
        }
    }

    private fun setupLogin() {
        setContentView(R.layout.activity_login)
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (authenticate(password)) {
                // Save login state (this is just an example, you should use a secure method)
                getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", true)
                    .apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_PASSWORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                setupLogin()
            } else {
                // Handle case where password creation was not successful, if needed
                Toast.makeText(this, "Password setup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticate(password: String): Boolean {
        val userDBHandler = UserDBHandler(this)
        val logins = userDBHandler.getAllLogins()
        if (logins.isEmpty()) {
            return false
        }
        val storedPassword = logins[0]["password"]
        if (storedPassword != password) {
            return false
        }
        return true
    }
}
