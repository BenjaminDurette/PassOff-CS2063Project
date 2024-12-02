package com.example.passoff

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.passoff.databinding.ActivityQuickshareBinding
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class QuickshareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickshareBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var isSenderMode = false
    private var matchCode: String? = null
    private var passwordToSend: String? = null
    private var connectionThread: BluetoothConnectionThread? = null

    private val REQUEST_BLUETOOTH_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickshareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if Bluetooth is available
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check and request Bluetooth permissions
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }

        // Setup mode and password to send
        isSenderMode = intent.getBooleanExtra("isSenderMode", false)
        passwordToSend = intent.getStringExtra("passwordToSend")

        // Set button text based on mode
        binding.beginWaitingButton.text = if (isSenderMode) "Send Password" else "Receive Password"

        // Set onClickListener for the button
        binding.beginWaitingButton.setOnClickListener {
            matchCode = binding.matchCodeInput.text.toString()
            if (matchCode.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter a match code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (bluetoothAdapter?.isEnabled == false) {
                enableBluetooth()
            } else {
                disableBluetooth()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter?.isEnabled == true) {
            if (isSenderMode) {
                // Start Bluetooth Client (Sender)
                connectionThread = BluetoothConnectionThread()
                connectionThread?.start()
            } else {
                // Start Bluetooth Server (Receiver)
                startBluetoothServer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopBluetoothServer()
        connectionThread?.cancel()
    }

    // Check if Bluetooth permissions are granted
    private fun hasBluetoothPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request Bluetooth permissions
    private fun requestBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSIONS)
    }

    // Handle Bluetooth permissions result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required for this app", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Enable Bluetooth
    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, 1)
    }

    // Disable Bluetooth
    @SuppressLint("MissingPermission")
    private fun disableBluetooth() {
        bluetoothAdapter?.disable()
        Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
    }

    // Start the Bluetooth server (Receiver)
    private fun startBluetoothServer() {
        val serverThread = BluetoothServerThread()
        serverThread.start()
    }

    // Stop the Bluetooth server
    @SuppressLint("MissingPermission")
    private fun stopBluetoothServer() {
        bluetoothAdapter?.cancelDiscovery()
    }

    // Encrypt the message before sending
    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = encryptMessage(message, matchCode ?: "test")
        connectionThread?.write(encryptedMessage)
    }

    // Encrypt the message using a key
    private fun encryptMessage(message: String, key: String): String {
        return EncryptionUtils.encrypt(message, key)
    }

    // Decrypt the message using a key
    private fun decryptMessage(encryptedMessage: String, key: String): String {
        return EncryptionUtils.decrypt(encryptedMessage, key)
    }

    // Thread for Bluetooth Server (Receiver)
    inner class BluetoothServerThread : Thread() {
        private val uuid: UUID = UUID.randomUUID()
        @SuppressLint("MissingPermission")
        private val serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BluetoothPassOff", uuid)

        override fun run() {
            val socket: BluetoothSocket? = serverSocket?.accept()
            socket?.let {
                manageConnectedSocket(it)
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            val inputStream: InputStream = socket.inputStream
            val outputStream: OutputStream = socket.outputStream

            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedMessage = String(buffer, 0, bytes)
                    if (receivedMessage == matchCode) {
                        // Acknowledge successful match
                        sendAcknowledgment(true)
                        // Receive encrypted message
                        bytes = inputStream.read(buffer)
                        val receivedEncryptedMessage = String(buffer, 0, bytes)
                        val decryptedMessage = decryptMessage(receivedEncryptedMessage, matchCode ?: "")
                        runOnUiThread {
                            binding.sharedPasswordText.text = "Received message: $decryptedMessage"
                        }
                    } else {
                        sendAcknowledgment(false)
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }

        // Acknowledge success or failure
        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            connectionThread?.write(acknowledgment)
        }
    }

    @SuppressLint("MissingPermission")
    // Thread for Bluetooth Client (Sender)
    inner class BluetoothConnectionThread : Thread() {
        private var socket: BluetoothSocket? = null
        private val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull()

        init {
            device?.let {
                socket = it.createRfcommSocketToServiceRecord(UUID.randomUUID())
            }
        }

        @SuppressLint("MissingPermission")
        override fun run() {
            socket?.connect()
            socket?.let {
                manageConnectedSocket(it)
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            val inputStream = socket.inputStream
            val outputStream = socket.outputStream

            val buffer = ByteArray(1024)
            var bytes: Int
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedMessage = String(buffer, 0, bytes)
                    if (receivedMessage == matchCode) {
                        sendEncryptedMessage(passwordToSend ?: "")
                    } else {
                        sendAcknowledgment(false)
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }

        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            connectionThread?.write(acknowledgment)
        }

        // Write a message to the connected Bluetooth socket
        fun write(message: String) {
            try {
                socket?.outputStream?.write(message.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Cancel the Bluetooth connection
        fun cancel() {
            try {
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
