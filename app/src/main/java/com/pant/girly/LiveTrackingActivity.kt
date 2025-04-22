package com.pant.girly

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LiveTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_tracking)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentLocation = location
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(latLng).title("You're here"))
                    updateLiveLocationToFirebase(latLng)
                }
            }
        }

        findViewById<Button>(R.id.btnStartTracking).setOnClickListener {
            startLocationUpdates()
        }

        findViewById<Button>(R.id.btnStopTracking).setOnClickListener {
            stopLocationUpdates()
        }

        findViewById<Button>(R.id.btnZoomIn).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        findViewById<Button>(R.id.btnZoomOut).setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }

        findViewById<Button>(R.id.btnSendLocation).setOnClickListener {
            Toast.makeText(this, "Sending location to emergency contacts...", Toast.LENGTH_SHORT).show()
            sendLocationToEmergencyContacts()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                val userLatLng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f))
                mMap.addMarker(MarkerOptions().position(userLatLng).title("You're here"))
                updateLiveLocationToFirebase(userLatLng)
            }
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        Toast.makeText(this, "Live tracking started", Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Toast.makeText(this, "Live tracking stopped", Toast.LENGTH_SHORT).show()
    }

    private fun sendLocationToEmergencyContacts() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(user.uid)
            .child("emergency_contacts")

        if (currentLocation != null) {
            val message = "🚨 Emergency! I'm here: https://maps.google.com/?q=${currentLocation!!.latitude},${currentLocation!!.longitude}"

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 102)
                Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show()
                return
            }

            databaseRef.get().addOnSuccessListener { snapshot ->
                for (contactSnapshot in snapshot.children) {
                    val number = contactSnapshot.child("number").getValue(String::class.java)
                    number?.let {
                        try {
                            val smsManager = SmsManager.getDefault()
                            smsManager.sendTextMessage(it, null, message, null, null)
                            Toast.makeText(this, "Alert sent to $it", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to send SMS to $it", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load emergency contacts", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLiveLocationToFirebase(latLng: LatLng) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val locationMap = mapOf(
            "latitude" to latLng.latitude,
            "longitude" to latLng.longitude,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().reference
            .child("live_locations")
            .child(user.uid)
            .setValue(locationMap)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
        } else if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
