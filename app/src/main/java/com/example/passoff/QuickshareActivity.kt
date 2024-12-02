package com.example.passoff

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.passoff.databinding.ActivityQuickshareBinding
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class QuickshareActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuickshareBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var isSenderMode = false
    private var matchCode: String? = null
    private var passwordToSend: String? = null
    private var connectionThread: BluetoothConnectionThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickshareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        isSenderMode = intent.getBooleanExtra("isSenderMode", false)
        passwordToSend = intent.getStringExtra("passwordToSend")

        binding.beginWaitingButton.text = if (isSenderMode) "Send Password" else "Receive Password"

        binding.beginWaitingButton.setOnClickListener {
            matchCode = binding.matchCodeInput.text.toString()
            if (matchCode.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter a match code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (bluetoothAdapter?.isEnabled == true) {
                Log.d("QuickshareActivity", "Bluetooth is enabled, proceeding with setup.")
                Toast.makeText(this, "Bluetooth enabled, starting process.", Toast.LENGTH_SHORT).show()
                if (isSenderMode) {
                    connectionThread = BluetoothConnectionThread()
                    connectionThread?.start()
                } else {
                    startBluetoothServer()
                }
            } else {
                Toast.makeText(this, "Bluetooth is disabled. Please enable it.", Toast.LENGTH_SHORT).show()
                Log.d("QuickshareActivity", "Bluetooth is disabled. Aborting.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("QuickshareActivity", "Bluetooth is enabled on resume.")
            if (isSenderMode) {
                connectionThread = BluetoothConnectionThread()
                connectionThread?.start()
            } else {
                startBluetoothServer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("QuickshareActivity", "Pausing activity, stopping Bluetooth processes.")
        stopBluetoothServer()
        connectionThread?.cancel()
    }

    private fun startBluetoothServer() {
        val serverThread = BluetoothServerThread()
        serverThread.start()
        Log.d("QuickshareActivity", "Bluetooth server thread started.")
        Toast.makeText(this, "Starting Bluetooth server...", Toast.LENGTH_SHORT).show()
    }


    private fun stopBluetoothServer() {
        Log.d("QuickshareActivity", "Stopping Bluetooth discovery.")
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = encryptMessage(message, matchCode ?: "test")
        connectionThread?.write(encryptedMessage)
        Log.d("QuickshareActivity", "Sending encrypted message.")
    }

    private fun encryptMessage(message: String, key: String): String {
        Log.d("QuickshareActivity", "Encrypting message: $message")
        return EncryptionUtils.encrypt(message, key)
    }

    private fun decryptMessage(encryptedMessage: String, key: String): String {
        Log.d("QuickshareActivity", "Decrypting message.")
        return EncryptionUtils.decrypt(encryptedMessage, key)
    }

    inner class BluetoothServerThread : Thread() {
        private val uuid: UUID = UUID.randomUUID()
        private val serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BluetoothPassOff", uuid)

        override fun run() {
            try {
                Log.d("BluetoothServerThread", "Waiting for a connection...")
                val socket: BluetoothSocket? = serverSocket?.accept()
                socket?.let {
                    Log.d("BluetoothServerThread", "Connection accepted.")
                    manageConnectedSocket(it)
                }
            } catch (e: Exception) {
                Log.e("BluetoothServerThread", "Error accepting connection: ${e.message}")
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            val inputStream: InputStream = socket.inputStream
            val outputStream: OutputStream = socket.outputStream
            val buffer = ByteArray(1024)
            var bytes: Int

            try {
                bytes = inputStream.read(buffer)
                val receivedMessage = String(buffer, 0, bytes)
                Log.d("BluetoothServerThread", "Received message: $receivedMessage")
                if (receivedMessage == matchCode) {
                    sendAcknowledgment(true)
                    // Receive encrypted message
                    bytes = inputStream.read(buffer)
                    val receivedEncryptedMessage = String(buffer, 0, bytes)
                    val decryptedMessage = decryptMessage(receivedEncryptedMessage, matchCode ?: "")
                    runOnUiThread {
                        binding.sharedPasswordText.text = "Received message: $decryptedMessage"
                        binding.sharedPasswordText.visibility = android.view.View.VISIBLE
                    }
                } else {
                    sendAcknowledgment(false)
                }
            } catch (e: Exception) {
                Log.e("BluetoothServerThread", "Error during communication: ${e.message}")
            }
        }

        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            connectionThread?.write(acknowledgment)
            Log.d("BluetoothServerThread", "Acknowledgment sent: $acknowledgment")
        }
    }

    inner class BluetoothConnectionThread : Thread() {
        private var socket: BluetoothSocket? = null
        private val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull()

        init {
            device?.let {
                socket = it.createRfcommSocketToServiceRecord(UUID.randomUUID())
                Log.d("BluetoothConnectionThread", "Socket created for device: ${it.name}")
            }
        }

        override fun run() {
            try {
                Log.d("BluetoothConnectionThread", "Connecting to device: ${device?.name}")
                socket?.connect()
                Log.d("BluetoothConnectionThread", "Connection successful.")
                socket?.let {
                    manageConnectedSocket(it)
                }
            } catch (e: Exception) {
                Log.e("BluetoothConnectionThread", "Connection failed: ${e.message}")
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            Log.d("BluetoothConnectionThread", "Managing connected socket.")
        }

        fun write(message: String) {
            try {
                socket?.outputStream?.write(message.toByteArray())
                Log.d("BluetoothConnectionThread", "Message written: $message")
            } catch (e: Exception) {
                Log.e("BluetoothConnectionThread", "Error writing message: ${e.message}")
            }
        }

        fun cancel() {
            try {
                socket?.close()
                Log.d("BluetoothConnectionThread", "Socket closed.")
            } catch (e: Exception) {
                Log.e("BluetoothConnectionThread", "Error closing socket: ${e.message}")
            }
        }
    }
}


