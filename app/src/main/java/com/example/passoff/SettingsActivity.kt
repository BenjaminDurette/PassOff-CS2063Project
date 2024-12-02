package com.example.passoff

import UserDBHandler
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Switch
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.widget.Button
import android.widget.Toast

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        this.title = "Settings"

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("isLoggedIn", false)
                .apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        }

        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        deleteAccountButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_deleteaccount, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setView(dialogView)
                .setPositiveButton("Delete") { dialog, _ ->
                    val dbHandler = DBHandler(this)
                    val dbHandler2 = UserDBHandler(this)
                    dbHandler.deleteAllPasswords()
                    dbHandler2.deleteAllLogins()
                    getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLoggedIn", false)
                        .apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
            dialog.show()
        }


        val themeToggle = findViewById<ToggleButton>(R.id.toggleTheme)
        themeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to dark theme
                themeToggle.setBackgroundColor(getResources().getColor(R.color.colorPrimary))

            } else {
                // Switch to light theme
                themeToggle.setBackgroundColor(getResources().getColor(R.color.grey))

            }
        }

        val touchIDToggle = findViewById<ToggleButton>(R.id.toggleTouchID)
        touchIDToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable TouchID
                touchIDToggle.setBackgroundColor(getResources().getColor(R.color.colorPrimary))

            } else {
                // Disable TouchID
                touchIDToggle.setBackgroundColor(getResources().getColor(R.color.grey))

            }
        }
    }
}
