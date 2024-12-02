package com.example.passoff

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private var serverThread: BluetoothServerThread? = null
    private val deviceReceiver = DeviceReceiver()

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
        Log.d("QuickshareActivity", "Received passwordToSend: $passwordToSend")

        binding.beginWaitingButton.text = if (isSenderMode) "Send Password" else "Receive Password"

        binding.beginWaitingButton.setOnClickListener {
            matchCode = binding.matchCodeInput.text.toString()
            if (matchCode.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter a match code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (bluetoothAdapter?.isEnabled == true) {
                if (isSenderMode) {
                    startDiscovery()
                } else {
                    makeDeviceDiscoverable()
                    startServer()
                }
            } else {
                Toast.makeText(this, "Bluetooth is disabled. Please enable Bluetooth to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeDeviceDiscoverable() {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(deviceReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(deviceReceiver)
        stopDiscovery()
        connectionThread?.cancel()
        serverThread?.cancel()
    }

    private fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
        Toast.makeText(this, "Starting Bluetooth discovery...", Toast.LENGTH_SHORT).show()
    }

    private fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
        Toast.makeText(this, "Stopping Bluetooth discovery...", Toast.LENGTH_SHORT).show()
    }

    private fun startServer() {
        serverThread = BluetoothServerThread()
        serverThread?.start()
        Toast.makeText(this, "Bluetooth server started.", Toast.LENGTH_SHORT).show()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        connectionThread = BluetoothConnectionThread(device)
        connectionThread?.start()
        Toast.makeText(this, "Connecting to device: ${device.name}", Toast.LENGTH_SHORT).show()
    }

    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = encryptMessage(message, matchCode ?: "test")
        connectionThread?.write(encryptedMessage)
        Toast.makeText(this, "Encrypted message sent.", Toast.LENGTH_SHORT).show()
    }

    private fun encryptMessage(message: String, key: String): String {
        return EncryptionUtils.encrypt(message, key)
    }

    private fun decryptMessage(encryptedMessage: String, key: String): String {
        return EncryptionUtils.decrypt(encryptedMessage, key)
    }

    inner class DeviceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        Toast.makeText(context, "Device found: ${it.name}", Toast.LENGTH_SHORT).show()
                        connectToDevice(it)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class BluetoothConnectionThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID())
        }

        override fun run() {
            try {
                socket?.connect()
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Connected to device: ${device.name}", Toast.LENGTH_SHORT).show()
                }
                manageConnectedSocket(socket!!)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Failed to connect to device: ${device.name}", Toast.LENGTH_SHORT).show()
                }
                cancel()
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
                        Toast.makeText(this@QuickshareActivity, "Match code received, sending encrypted message.", Toast.LENGTH_SHORT).show()
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
            write(acknowledgment)
            Toast.makeText(this@QuickshareActivity, "Acknowledgment sent: $acknowledgment", Toast.LENGTH_SHORT).show()
        }

        fun write(message: String) {
            try {
                socket?.outputStream?.write(message.toByteArray())
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Message sent: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                socket?.close()
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Connection closed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class BluetoothServerThread : Thread() {
        private val uuid: UUID = UUID.randomUUID()
        private val serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BluetoothPassOff", uuid)

        override fun run() {
            try {
                val socket: BluetoothSocket? = serverSocket?.accept()
                socket?.let {
                    manageConnectedSocket(it)
                    runOnUiThread {
                        Toast.makeText(this@QuickshareActivity, "Bluetooth server accepted connection.", Toast.LENGTH_SHORT).show()
                    }
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
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedMessage = String(buffer, 0, bytes)
                    if (receivedMessage == matchCode) {
                        sendAcknowledgment(true)
                        bytes = inputStream.read(buffer)
                        val receivedEncryptedMessage = String(buffer, 0, bytes)
                        val decryptedMessage = decryptMessage(receivedEncryptedMessage, matchCode ?: "")
                        runOnUiThread {
                            binding.sharedPasswordText.text = "Received message: $decryptedMessage"
                            binding.sharedPasswordText.visibility = android.view.View.VISIBLE
                        }
                        Toast.makeText(this@QuickshareActivity, "Message received and decrypted.", Toast.LENGTH_SHORT).show()
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
            runOnUiThread {
                Toast.makeText(this@QuickshareActivity, "Acknowledgment sent: $acknowledgment", Toast.LENGTH_SHORT).show()
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Server connection closed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}