package com.example.passoff

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.dynamicDarkColorScheme

class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        this.title = "Settings"

        val themeSwitch = findViewById<Switch>(R.id.switchTheme)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to dark theme
            } else {
                // Switch to light theme
            }
            // Save the new theme setting to SharedPreferences or other storage
        }
    }
}
