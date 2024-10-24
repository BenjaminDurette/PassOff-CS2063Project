package com.example.passoff

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text

class itemView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val itemName = this.intent.getStringExtra("itemName")
        val username = this.intent.getStringExtra("username")
        val password = this.intent.getStringExtra("password")
        val domain = this.intent.getStringExtra("domain")

        val titleText = findViewById<TextView>(R.id.item_name)
        titleText.text = itemName

        val usernameText = findViewById<TextView>(R.id.item_username)
        usernameText.text = username

        val passwordText = findViewById<TextView>(R.id.item_password)
        passwordText.text = password

        val domainText = findViewById<TextView>(R.id.item_domain)
        domainText.text = domain

        this.title = itemName

        val copyButton = findViewById<Button>(R.id.copy_button)
        copyButton.setOnClickListener    {
            copyPasswordToClipboard(password)
        }
    }

    private fun copyPasswordToClipboard(password: String?) {
        if (password != null) {
            // Get the ClipboardManager
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Create a ClipData object
            val clip = ClipData.newPlainText("Password", password)
            // Set the clip
            clipboard.setPrimaryClip(clip)

            // Show a toast message to inform the user
            Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}