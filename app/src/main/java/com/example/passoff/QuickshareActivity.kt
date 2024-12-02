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

class QuickshareActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuickshareBinding
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var isSenderMode = false
    private var matchCode: String? = null
    private var passwordToSend: String? = null
    private var connectionThread: BluetoothConnectionThread? = null
    private val deviceReceiver = DeviceReceiver()

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

            // Check if Bluetooth is enabled, then proceed with app logic
            if (bluetoothAdapter?.isEnabled == true) {
                startDiscovery()
            } else {
                // Bluetooth is disabled, show a toast message
                Toast.makeText(this, "Bluetooth is disabled. Please enable Bluetooth to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
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
    }

    private fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
        Toast.makeText(this, "Starting Bluetooth discovery...", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        connectionThread = BluetoothConnectionThread(device)
        connectionThread?.start()
    }

    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = encryptMessage(message, matchCode ?: "test")
        connectionThread?.write(encryptedMessage)
    }

    private fun encryptMessage(message: String, key: String): String {
        return EncryptionUtils.encrypt(message, key)
    }

    private fun decryptMessage(encryptedMessage: String, key: String): String {
        return EncryptionUtils.decrypt(encryptedMessage, key)
    }

    inner class DeviceReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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
                socket?.let {
                    manageConnectedSocket(it)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Failed to connect to device: ${device.name}", Toast.LENGTH_SHORT).show()
                }
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
            write(acknowledgment)
        }

        fun write(message: String) {
            try {
                socket?.outputStream?.write(message.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}