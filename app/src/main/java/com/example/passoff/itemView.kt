package com.example.passoff

import android.app.AlertDialog
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class itemView : AppCompatActivity() {

    private val ALGORITHM = "AES"
    private val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private lateinit var secretKey: SecretKey

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_detail)

        // Generate the secret key
        secretKey = generateKey()

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

        val deleteButton = findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Delete Password")
                .setView(dialogView)
                .setPositiveButton("Delete") { dialog, _ ->
                    val dbHandler = DBHandler(this)
                    val success = dbHandler.deletePassword(this.intent.getIntExtra("id", -1))
                    if (success) {
                        Log.d("Database", "Password deleted successfully")
                    } else {
                        Log.e("Database", "Failed to delete password")
                    }
                    val loadDataTask = LoadDataTask(this)
                    loadDataTask.setRecyclerView(MainActivity.recyclerView)
                    loadDataTask.setCircularProgressIndicator(MainActivity.progressIndicator)
                    loadDataTask.execute()
                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
            dialog.show()
            val copyButton = findViewById<Button>(R.id.copy_button)
            copyButton.setOnClickListener {
                copyPasswordToClipboard(password)
            }

            val encryptLabel = findViewById<TextView>(R.id.encrypted_text)
            val encryptButton = findViewById<Button>(R.id.encrypt_button)
            val decryptButton = findViewById<Button>(R.id.decrypt_button)
            val quickshareButton = findViewById<Button>(R.id.quickshare_button)

            encryptButton.setOnClickListener {
                if (password != null) {
                    val encryptedText = encryptPassword(password)
                    encryptLabel.text = "Encryption Text: $encryptedText"
                }
            }

            // Decrypt button click listener
            decryptButton.setOnClickListener {
                val encryptedText = encryptLabel.text.toString().removePrefix("Encryption Text: ")
                val decryptedText = decrypt(encryptedText)
                encryptLabel.text = "Decryption Text: $decryptedText"
            }

            // Decrypt button click listener
            quickshareButton.setOnClickListener {
                if (BluetoothUtils.areBluetoothPermissionsGranted(this)) {
                    // If Bluetooth permissions are available, start the QuickshareActivity
                    val intent = Intent(this, QuickshareActivity::class.java)
                    intent.putExtra("isSenderMode", false)
                    startActivity(intent)
                } else {
                    // If Bluetooth permissions are not available, show a Toast message
                    Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256) // You can use 128 or 192 as well
        return keyGen.generateKey()
    }

    private fun encryptPassword(password: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(password.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decrypt(encrypted: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT))
        return String(decryptedBytes)
    }
}
