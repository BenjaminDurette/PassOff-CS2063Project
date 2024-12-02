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
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.passoff.databinding.ActivityQuickshareBinding
import java.io.IOException
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
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DISCOVERABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Discoverability request denied", Toast.LENGTH_SHORT).show()
            } else {
                startServer()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(deviceReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        registerReceiver(deviceReceiver, IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(deviceReceiver)
        stopDiscovery()
        connectionThread?.cancel()
        serverThread?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionThread?.cancel()
        serverThread?.cancel()
    }

    private fun startDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
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
        bluetoothAdapter?.cancelDiscovery()
        connectionThread = BluetoothConnectionThread(device)
        connectionThread?.start()
        Toast.makeText(this, "Connecting to device: ${device.name}", Toast.LENGTH_SHORT).show()
    }

    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = encryptMessage(message, matchCode ?: "test")
        connectionThread?.write(encryptedMessage)
        Toast.makeText(this, "Encrypted message sent.", Toast.LENGTH_SHORT).show()
    }

    private fun encryptMessage(message: String, matchCode: String): String {
        val key = EncryptionUtils.deriveKeyFromMatchCode(matchCode)
        return EncryptionUtils.encrypt(message, key)
    }

    private fun decryptMessage(encryptedMessage: String, matchCode: String): String {
        val key = EncryptionUtils.deriveKeyFromMatchCode(matchCode)
        Log.d(
            "java",
            "After key"
        )
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
        private val maxRetries = 3
        private var connectionStartTime: Long = 0

        init {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        }

        override fun run() {
            var attempt = 0
            while (attempt < maxRetries) {
                try {
                    Log.d("run", "Before socket?.connect()")
                    socket?.connect()
                    connectionStartTime = SystemClock.currentThreadTimeMillis()
                    runOnUiThread {
                        Toast.makeText(this@QuickshareActivity, "Connected to device: ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    manageConnectedSocket(socket!!)
                    return
                } catch (e: IOException) {
                    Log.d("manageConnectedSocket", e.message ?: "No message")
                    attempt++
                    if (attempt >= maxRetries) {
                        logConnectionDuration("From NonServer Run, attempts> max retries", SystemClock.currentThreadTimeMillis() - connectionStartTime)
                        runOnUiThread {
                            Toast.makeText(this@QuickshareActivity, "Failed to connect to device: ${device.name}", Toast.LENGTH_SHORT).show()
                        }
                        cancel()
                        return;
                    }
                }
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            Log.d("manageConnectedSocket", "Sender Beggining")
            val inputStream = socket.inputStream
            val outputStream = socket.outputStream

            val buffer = ByteArray(1024)
            var bytes: Int

                try {
                    Thread.sleep(100)
                    Log.d("manageConnectedSocket", "Sender Before inputStream.read")
                    bytes = inputStream.read(buffer)
                    Log.d("manageConnectedSocket", "Sender After inputStream.read")
                    val receivedMessage = String(buffer, 0, bytes)
                    Log.d("MatchCheck", "Received: $receivedMessage")
                    Log.d("MatchCheck", "MatchCode: $matchCode")
                    Log.d("manageConnectedSocket", "After String buffer")
                    if (receivedMessage == matchCode) {
                        Thread.sleep(100)
                        sendEncryptedMessage(passwordToSend ?: "")
                        Log.d("manageConnectedSocket", "Sender After send encrypted")
                        runOnUiThread {
                            Toast.makeText(
                                this@QuickshareActivity,
                                "Match code received, sending encrypted message.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.d("manageConnectedSocket", "Sender Match codes did not match")
                        sendAcknowledgment(false)
                    }
                } catch (e: Exception) {
                    logConnectionDuration("From NonServer MCS, Exception: ${e.message}", SystemClock.currentThreadTimeMillis() - connectionStartTime)
                    Log.d("manageConnectedSocket", e.message ?: "No message")
                }
        }

        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            write(acknowledgment)
            Toast.makeText(this@QuickshareActivity, "Acknowledgment sent: $acknowledgment", Toast.LENGTH_SHORT).show()
        }

        fun write(message: String) {
            try {
                Log.d("manageConnectedSocket", "writing message: ${message}")
                socket?.outputStream?.write(message.toByteArray())
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Message sent: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("manageConnectedSocket", e.message ?: "No message")
            }
        }

        fun cancel() {
            try {
                socket?.close()
                logConnectionDuration("From NonServer Cancel", SystemClock.currentThreadTimeMillis() - connectionStartTime)
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Connection closed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("manageConnectedSocket", e.message ?: "No message")
                e.printStackTrace()
            }
        }
    }

    inner class BluetoothServerThread : Thread() {
        private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("BluetoothPassOff", uuid)
        private var connectionStartTime: Long = 0
        private var socket: BluetoothSocket? = null

        override fun run() {
            try {
                socket = serverSocket?.accept()
                socket?.let {
                    connectionStartTime = SystemClock.elapsedRealtime()
                    Log.d("manageConnectedSocket", "Receiver Socket open before MCS call: ${socket?.isConnected}")

                    manageConnectedSocket(it)
                    Log.d("manageConnectedSocket", "Receiver Socket open after MCS call: ${socket?.isConnected}")

                    runOnUiThread {
                        Toast.makeText(this@QuickshareActivity, "Bluetooth server accepted connection.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                logConnectionDuration("From Server Run, Exception: ${e.message}", SystemClock.currentThreadTimeMillis() - connectionStartTime)
                Log.e("BluetoothServerThread", "Error accepting connection: ${e.message}")
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {
            Log.d("manageConnectedSocket", "Receiver Socket open at beggingin: ${socket?.isConnected}")

            val inputStream: InputStream = socket.inputStream
            val outputStream: OutputStream = socket.outputStream

            Log.d("MatchCheck", "MatchCodeToSend: $matchCode")
            write(matchCode ?: "")

            val buffer = ByteArray(1024)
            var bytes: Int

                try {
                    Log.d("manageConnectedSocket", "Receiver Socket open at begigning of loop: ${socket?.isConnected}")

                    bytes = inputStream.read(buffer)
                    Log.d("manageConnectedSocket", "Receiver Socket open at after input read: ${socket?.isConnected}")
                    Log.d("manageConnectedSocket", "bytes: $bytes")

                    val receivedMessage = String(buffer, 0, bytes)
                    Log.d("manageConnectedSocket", "Received message: $receivedMessage")
                    if (receivedMessage == "MATCH_FAILED") {
                        runOnUiThread {
                            Toast.makeText(
                                this@QuickshareActivity,
                                "Match code verification failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Log.d(
                        "java",
                        "Before Decrypt"
                    )
                    val decryptedMessage = decryptMessage(receivedMessage, matchCode ?: "")
                    runOnUiThread {
                        binding.sharedPasswordText.text = "Received message: $decryptedMessage"
                        binding.sharedPasswordText.visibility = android.view.View.VISIBLE
                    }
                    Log.d(
                        "java",
                        "After Decrypt"
                    )
                     sendAcknowledgment(true)
                } catch (e: Exception) {
                    Log.d(
                        "manageConnectedSocket",
                        "Receiver Socket open at excpetion: ${socket?.isConnected}"
                    )
                    logConnectionDuration(
                        "From Server MCS, Excpetion: ${e.message}",
                        SystemClock.currentThreadTimeMillis() - connectionStartTime
                    )
                    Log.d("manageConnectedSocket", e.message ?: "No message")

                }
        }

        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            write(acknowledgment)
            runOnUiThread {
                Toast.makeText(this@QuickshareActivity, "Acknowledgment sent: $acknowledgment", Toast.LENGTH_SHORT).show()
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
                logConnectionDuration("From Server Cancel", SystemClock.currentThreadTimeMillis() - connectionStartTime)
                runOnUiThread {
                    Toast.makeText(this@QuickshareActivity, "Server connection closed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("manageConnectedSocket", e.message ?: "No message")
                e.printStackTrace()
            }
        }

        fun write(message: String) {
            try {
                Log.d("manageConnectedSocket", "writing message: ${message}")
                 socket?.outputStream?.write(message.toByteArray())
            } catch (e: Exception) {
                Log.d("manageConnectedSocket", e.message ?: "No message")

                e.printStackTrace()
            }
        }
    }

    fun logConnectionDuration(tag: String, time: Long){
        Log.d("Connection Duration", "${tag}: Connection duration: $time ms")
    }

    companion object {
        private const val REQUEST_DISCOVERABLE_BT = 1
    }
}