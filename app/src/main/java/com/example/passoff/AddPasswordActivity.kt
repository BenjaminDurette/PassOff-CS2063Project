package com.example.passoff

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class AddPasswordActivity : AppCompatActivity() {

    private lateinit var characterCountSeekBar: SeekBar
    private lateinit var characterCountTextView: TextView
    private lateinit var capitalLettersCheckBox: CheckBox
    private lateinit var numbersCheckBox: CheckBox
    private lateinit var symbolsCheckBox: CheckBox
    private lateinit var generatePasswordButton: Button
    private lateinit var generatedPasswordTextView: TextView
    private lateinit var openFormButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_password)

        characterCountSeekBar = findViewById(R.id.characterCountSeekBar)
        characterCountTextView = findViewById(R.id.characterCountTextView)
        capitalLettersCheckBox = findViewById(R.id.capitalLettersCheckBox)
        numbersCheckBox = findViewById(R.id.numbersCheckBox)
        symbolsCheckBox = findViewById(R.id.symbolsCheckBox)
        generatePasswordButton = findViewById(R.id.generatePasswordButton)
        generatedPasswordTextView = findViewById(R.id.generatedPasswordTextView)
        openFormButton = findViewById(R.id.openFormButton)

        characterCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                characterCountTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        generatePasswordButton.setOnClickListener {
            val password = generatePassword(
                characterCountSeekBar.progress,
                capitalLettersCheckBox.isChecked,
                numbersCheckBox.isChecked,
                symbolsCheckBox.isChecked
            )
            generatedPasswordTextView.text = password
            openFormButton.isEnabled = true
        }

        openFormButton.setOnClickListener {
            val password = generatedPasswordTextView.text.toString()
            showPasswordDialogue(password)
        }
    }

    private fun generatePassword(length: Int, includeCapitals: Boolean, includeNumbers: Boolean, includeSymbols: Boolean): String {
        val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
        val capitalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numberChars = "0123456789"
        val symbolChars = "!@#$%^&*()-_=+[]{}|;:,.<>?/"

        var charPool = lowercaseChars
        if (includeCapitals) charPool += capitalChars
        if (includeNumbers) charPool += numberChars
        if (includeSymbols) charPool += symbolChars

        return (1..length)
            .map { charPool[Random.nextInt(charPool.length)] }
            .joinToString("")
    }

    private fun showPasswordDialogue(password: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)
        val domainEditText = dialogView.findViewById<EditText>(R.id.domainEditText)

        passwordEditText.setText(password)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter New Password Information")
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                if (titleEditText.text.isNotEmpty() && usernameEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                    if (domainEditText.text.isEmpty()) {
                        domainEditText.setText("N/A")
                    }
                    val title = titleEditText.text.toString()
                    val username = usernameEditText.text.toString()
                    val passwordText = passwordEditText.text.toString()
                    val domain = domainEditText.text.toString()

                    val resultIntent = Intent()
                    resultIntent.putExtra("title", title)
                    resultIntent.putExtra("username", username)
                    resultIntent.putExtra("password", passwordText)
                    resultIntent.putExtra("domain", domain)
                    setResult(Activity.RESULT_OK, resultIntent)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }

                dialog.dismiss()
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                setResult(Activity.RESULT_CANCELED)
                dialog.cancel()
                finish()
            }
            .create()

        dialog.show()
    }
}
