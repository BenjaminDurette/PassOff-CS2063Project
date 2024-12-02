package com.example.passoff

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        setContentView(R.layout.activity_choose_add_password)
        this.title = "Add Password"

        val generateOptionButton = findViewById<Button>(R.id.generateOptionButton)
        generateOptionButton.setOnClickListener {
            addGeneratedPassword()
        }

        val customOptionButton = findViewById<Button>(R.id.customOptionButton)
        customOptionButton.setOnClickListener {
            addCustomPassword()
        }
    }

    private fun addGeneratedPassword() {
        setContentView(R.layout.activity_add_generated_password)

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

    private fun addCustomPassword() {
        setContentView(R.layout.activity_add_custom_password)

        val passwordField: EditText = findViewById(R.id.passwordField)
        val passwordStrengthBar: ProgressBar = findViewById(R.id.passwordStrengthBar)
        val passwordStrengthText: TextView = findViewById(R.id.passwordStrengthText)

        fun updateProgressBar(strength: Int, color: Int, levelText: String) {
            passwordStrengthBar.progress = strength
            passwordStrengthBar.progressTintList = ContextCompat.getColorStateList(this, color)
            passwordStrengthText.text = levelText
        }

        passwordField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                val (strength, color, levelText) = calculatePasswordStrength(password)
                updateProgressBar(strength, color, levelText)

                val openFormButton = findViewById<Button>(R.id.openFormButton)
                openFormButton.isEnabled = password.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val openFormButton = findViewById<Button>(R.id.openFormButton)
        openFormButton.setOnClickListener {
            val password = passwordField.text.toString()
            showPasswordDialogue(password)
        }
    }

    fun calculatePasswordStrength(password: String): Triple<Int, Int, String> {
        var strength = 0
        var color = R.color.weak
        var levelText = "Weak"

        if (password.length >= 8) strength += 25
        if (password.matches(".*[A-Z].*".toRegex())) strength += 25
        if (password.matches(".*[0-9].*".toRegex())) strength += 25
        if (password.matches(".*[!@#\$%^&*()\\-_=+\\[\\]{}|;:,.<>?/].*".toRegex())) strength += 25

        when (strength) {
            in 0..50 -> {
                color = R.color.weak
                levelText = "Weak"
            }
            in 51..75 -> {
                color = R.color.medium
                levelText = "Medium"
            }
            else -> {
                color = R.color.strong
                levelText = "Strong"
            }
        }

        return Triple(strength, color, levelText)
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
