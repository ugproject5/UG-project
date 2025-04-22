package com.pant.girly

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

data class BluetoothDeviceInfo(val name: String?, val address: String)

class WatchActivity : AppCompatActivity() {

    private lateinit var heartRateTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false
    private lateinit var addWatchButton: Button
    private lateinit var watchIdEditText: EditText
    private lateinit var scanButton: Button
    private lateinit var devicesRecyclerView: RecyclerView
    private lateinit var devicesAdapter: BluetoothDeviceAdapter // Changed to custom adapter
    private val scannedDevices = mutableListOf<BluetoothDeviceInfo>() // Changed to data class list
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isWatchConnected = false
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val deviceInfo = BluetoothDeviceInfo(device.name, device.address)
                if (!scannedDevices.any { it.address == deviceInfo.address }) {
                    scannedDevices.add(deviceInfo)
                    devicesAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    // Custom RecyclerView Adapter (Moved inside WatchActivity)
    class BluetoothDeviceAdapter(private val deviceList: List<BluetoothDeviceInfo>, private val itemClickListener: (BluetoothDeviceInfo) -> Unit) :
        RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
            val addressTextView: TextView = itemView.findViewById(android.R.id.text2)

            fun bind(deviceInfo: BluetoothDeviceInfo, clickListener: (BluetoothDeviceInfo) -> Unit) {
                nameTextView.text = deviceInfo.name ?: "Unknown Device"
                addressTextView.text = deviceInfo.address
                itemView.setOnClickListener { clickListener(deviceInfo) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentDevice = deviceList[position]
            holder.bind(currentDevice, itemClickListener)
        }

        override fun getItemCount() = deviceList.size
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch)

        heartRateTextView = findViewById(R.id.heartRateTextView)
        addWatchButton = findViewById(R.id.addWatchButton)
        watchIdEditText = findViewById(R.id.watchIdEditText)
        scanButton = findViewById(R.id.scanButton)
        devicesRecyclerView = findViewById(R.id.devicesRecyclerView)
        devicesRecyclerView.layoutManager = LinearLayoutManager(this)
        devicesAdapter = BluetoothDeviceAdapter(scannedDevices) { deviceInfo ->
            Toast.makeText(this, "Clicked: ${deviceInfo.name} - ${deviceInfo.address}", Toast.LENGTH_SHORT).show()
            // Implement pairing logic here
        }
        devicesRecyclerView.adapter = devicesAdapter // Set the custom adapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        startHeartRateSimulation()
        registerChargerDisconnectReceiver()

        addWatchButton.setOnClickListener {
            val watchId = watchIdEditText.text.toString().trim()
            if (watchId.isNotEmpty()) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    database.getReference("Users").child(userId).child("watchId").setValue(watchId)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Watch ID added successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add Watch ID.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter Watch ID.", Toast.LENGTH_SHORT).show()
            }
        }

        scanButton.setOnClickListener {
            if (checkBluetoothPermissions()) {
                startBluetoothScan()
            } else {
                requestBluetoothPermissions()
            }
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions(): Unit {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothScan(): Unit {
        scannedDevices.clear()
        devicesAdapter.notifyDataSetChanged()
        bluetoothLeScanner?.startScan(scanCallback)
        Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothLeScanner?.stopScan(scanCallback)
            Toast.makeText(this, "Scan finished.", Toast.LENGTH_SHORT).show()
        }, 10000) // Scan for 10 seconds
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Unit {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan()
            } else {
                Toast.makeText(this, "Location permission is required for Bluetooth scanning.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startHeartRateSimulation(): Unit {
        isMonitoring = true
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                if (isWatchConnected) {
                    val heartRate = Random().nextInt(70) + 40
                    heartRateTextView.text = "Heart Rate: $heartRate bpm"

                    if (heartRate > 120 || heartRate < 50) {
                        Toast.makeText(
                            this@WatchActivity,
                            "Abnormal Heart Rate Detected!",
                            Toast.LENGTH_SHORT
                        ).show()
                        triggerSosFromWatch()
                    }
                } else {
                    heartRateTextView.text = "Connect Watch"
                }

                handler.postDelayed(this, 5000)
            }
        }, 1000)
    }

    private fun triggerSosFromWatch(): Unit {
        val intent = Intent(this, SosTriggerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun registerChargerDisconnectReceiver(): Unit {
        val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Toast.makeText(context, "Charger Disconnected!", Toast.LENGTH_SHORT).show()
                triggerSosFromWatch()
            }
        }, filter)
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy(): Unit {
        super.onDestroy()
        bluetoothLeScanner?.stopScan(scanCallback)
        isMonitoring = false
    }
}