package com.example.passoff

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Switch
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        this.title = "Settings"



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
