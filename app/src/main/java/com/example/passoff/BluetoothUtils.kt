import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BluetoothUtils {

    // Function to check if Bluetooth permissions are granted and request them if not
    @RequiresApi(Build.VERSION_CODES.S)
    fun areBluetoothPermissionsGranted(activity: Activity): Boolean {
        // Check if Bluetooth permissions are granted
        val bluetoothPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        val bluetoothConnectPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        val bluetoothScanPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

        // If any of the permissions are not granted, request them
        if (!bluetoothPermission || !bluetoothAdminPermission || !bluetoothConnectPermission || !bluetoothScanPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                1 // Request code
            )
            return false
        }

        return true
    }
}
