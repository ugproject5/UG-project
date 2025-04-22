package com.pant.girly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pant.girly.LiveVideoActivity

class SosTriggerActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        } else {
            triggerSOS()
        }
    }



    private fun triggerSOS() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
            val latLng = if (location != null) "${location.latitude}, ${location.longitude}" else "Unknown"

            val sosData = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "location" to latLng,
                "status" to "Triggered"
            )

            FirebaseDatabase.getInstance().reference
                .child("sos_alerts")
                .child(user.uid)
                .push()
                .setValue(sosData)

            sendAlertToEmergencyContacts(latLng)

            Toast.makeText(this, "SOS Alert Triggered", Toast.LENGTH_SHORT).show()

            // Start Live Video Stream
            val intent = Intent(this, LiveVideoActivity::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendAlertToEmergencyContacts(location: String) {
        val emergencyMessage = "⚠️ SOS Triggered! Location: $location. Please respond immediately!"

        // Firebase path: users/{uid}/emergency_contacts
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(user.uid)
            .child("emergency_contacts")

        ref.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val number = child.child("number").getValue(String::class.java)
                number?.let {
                    SmsUtil.sendSMS(this, it, emergencyMessage)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerSOS()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}
