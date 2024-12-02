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
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        val showPasswordButton = findViewById<ImageButton>(R.id.show_password)
        var isPasswordVisible = false
        showPasswordButton.setOnClickListener {
            if (isPasswordVisible) {
                passwordText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            else {
                passwordText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            isPasswordVisible = !isPasswordVisible
        }

        val domainText = findViewById<TextView>(R.id.item_domain)
        domainText.text = domain

        this.title = itemName

        val copyName = findViewById<ImageButton>(R.id.copy_name)
        copyName.setOnClickListener {
            copyNameToClipboard(titleText.text.toString())
        }

        val copyUsername = findViewById<ImageButton>(R.id.copy_username)
        copyUsername.setOnClickListener {
            copyUsernameToClipboard(usernameText.text.toString())
        }

        val copyPassword = findViewById<ImageButton>(R.id.copy_password)
        copyPassword.setOnClickListener {
            copyPasswordToClipboard(passwordText.text.toString())
        }

        val copyDomain = findViewById<ImageButton>(R.id.copy_url)
        copyDomain.setOnClickListener {
            copyDomainToClipboard(domainText.text.toString())
        }

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
        }

        val quickshareButton = findViewById<Button>(R.id.quickshare_button)
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

        val editButton = findViewById<Button>(R.id.edit_button)
        editButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Enter New Values")
                .setMessage("Any fields left blank will remain the same.")
                .setView(dialogView)
                .setPositiveButton("Update") { dialog, _ ->
                    var newName = dialogView.findViewById<TextView>(R.id.item_name).text.toString()
                    var newUsername = dialogView.findViewById<TextView>(R.id.item_username).text.toString()
                    var newPassword = dialogView.findViewById<TextView>(R.id.item_password).text.toString()
                    var newDomain = dialogView.findViewById<TextView>(R.id.item_domain).text.toString()
                    if (newName.isEmpty()) {
                        newName = itemName.toString()
                    }
                    if (newUsername.isEmpty()) {
                        newUsername = username.toString()
                    }
                    if (newPassword.isEmpty()) {
                        newPassword = password.toString()
                    }
                    if (newDomain.isEmpty()) {
                        newDomain = domain.toString()
                    }

                    val dbHandler = DBHandler(this)
                    val success = dbHandler.updatePassword(
                        this.intent.getIntExtra("id", -1),
                        newName,
                        newUsername,
                        newPassword,
                        newDomain)
                    if (success) {
                        Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                    }

                    dialog.dismiss()

                    //restartActivity
                    val intent = Intent(this, itemView::class.java)
                    val item = dbHandler.getPassword(this.intent.getIntExtra("id", -1))
                    intent.putExtra("itemName", item!!.name)
                    intent.putExtra("username", item.username)
                    intent.putExtra("password", item.password)
                    intent.putExtra("domain", item.domain)
                    intent.putExtra("id", item.id)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
            dialog.show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun copyNameToClipboard(name: String?) {
        if (name != null) {
            // Get the ClipboardManager
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Create a ClipData object
            val clip = ClipData.newPlainText("Name", name)
            // Set the clip
            clipboard.setPrimaryClip(clip)
            // Show a toast message to inform the user
            Toast.makeText(this, "Item name copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUsernameToClipboard(username: String?) {
        if (username != null) {
            // Get the ClipboardManager
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Create a ClipData object
            val clip = ClipData.newPlainText("Username", username)
            // Set the clip
            clipboard.setPrimaryClip(clip)
            // Show a toast message to inform the user
            Toast.makeText(this, "Username copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyDomainToClipboard(domain: String?) {
        if (domain != null) {
            // Get the ClipboardManager
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Create a ClipData object
            val clip = ClipData.newPlainText("Domain", domain)
            // Set the clip
            clipboard.setPrimaryClip(clip)
            // Show a toast message to inform the user
            Toast.makeText(this, "Domain copied to clipboard", Toast.LENGTH_SHORT).show()
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
