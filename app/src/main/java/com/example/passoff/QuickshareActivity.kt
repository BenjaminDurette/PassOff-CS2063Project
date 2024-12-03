package com.example.passoff

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private var sharedPassword: String? = null

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

        binding.copyPasswordButton.setOnClickListener {
            copyPasswordToClipboard(sharedPassword)
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
    }

    private fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    private fun startServer() {
        serverThread = BluetoothServerThread()
        serverThread?.start()
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
    }

    private fun encryptMessage(message: String, matchCode: String): String {
        val key = EncryptionUtils.deriveKeyFromString(matchCode)
        return EncryptionUtils.encrypt(message, key)
    }

    private fun decryptMessage(encryptedMessage: String, matchCode: String): String {
        val key = EncryptionUtils.deriveKeyFromString(matchCode)
        return EncryptionUtils.decrypt(encryptedMessage, key)
    }

    inner class DeviceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        connectToDevice(it)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
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
                    socket?.connect()
                    connectionStartTime = SystemClock.currentThreadTimeMillis()
                    runOnUiThread {
                        Toast.makeText(this@QuickshareActivity, "Connected to device: ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    manageConnectedSocket(socket!!)
                    return
                } catch (e: IOException) {
                    attempt++
                    if (attempt >= maxRetries) {
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
            val inputStream = socket.inputStream
            val outputStream = socket.outputStream

            val buffer = ByteArray(1024)
            var bytes: Int
            try {
                Thread.sleep(100)
                bytes = inputStream.read(buffer)
                val receivedMessage = String(buffer, 0, bytes)
                if (receivedMessage == matchCode) {
                    Thread.sleep(100)
                    sendEncryptedMessage(passwordToSend ?: "")
                    bytes = inputStream.read(buffer)

                    val receivedAcknowledgement = String(buffer, 0, bytes)
                    if (receivedAcknowledgement == "MATCH_FAILED") {
                        runOnUiThread {
                            Toast.makeText(
                                this@QuickshareActivity,
                                "Failed to share password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        runOnUiThread {
                            Toast.makeText(
                                this@QuickshareActivity,
                                "Password shared successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    closeConnection()
                } else {
                    sendAcknowledgment(false)
                    closeConnection()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                closeConnection()
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

        fun closeConnection() {
            try {
                socket?.close()
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
                    manageConnectedSocket(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun manageConnectedSocket(socket: BluetoothSocket) {

            val inputStream: InputStream = socket.inputStream
            val outputStream: OutputStream = socket.outputStream

            write(matchCode ?: "")

            val buffer = ByteArray(1024)
            val bytes: Int

                try {

                    bytes = inputStream.read(buffer)

                    val receivedMessage = String(buffer, 0, bytes)
                    if (receivedMessage == "MATCH_FAILED") {
                        runOnUiThread {
                            Toast.makeText(
                                this@QuickshareActivity,
                                "Match code verification failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        closeConnection()
                        return;
                    }
                    val decryptedMessage = decryptMessage(receivedMessage, matchCode ?: "")
                    sharedPassword = decryptedMessage
                    runOnUiThread {
                        binding.sharedPasswordText.text = "Received Password: $sharedPassword"
                        binding.sharedPasswordText.visibility = android.view.View.VISIBLE
                        binding.copyPasswordButton.visibility = android.view.View.VISIBLE
                    }
                     sendAcknowledgment(true)
                    closeConnection()
                } catch (e: Exception) {
                    e.printStackTrace()
                    closeConnection()
                }
        }

        private fun sendAcknowledgment(isSuccess: Boolean) {
            val acknowledgment = if (isSuccess) "MATCH_SUCCESS" else "MATCH_FAILED"
            write(acknowledgment)
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun closeConnection() {
            try {
                socket?.close()
                serverSocket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun write(message: String) {
            try {
                 socket?.outputStream?.write(message.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val REQUEST_DISCOVERABLE_BT = 1
    }
}