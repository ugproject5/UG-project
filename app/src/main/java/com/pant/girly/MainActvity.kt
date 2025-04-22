package com.pant.girly

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var sosButton: Button
    private lateinit var safetyModeSwitch: Switch
    private lateinit var welcomeTextView: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var cardLiveTracking: CardView
    private lateinit var cardAnonymousReport: CardView
    private lateinit var cardWatch: CardView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fullscreen mode - Try this after setContentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Views
        welcomeTextView = findViewById(R.id.welcomeTextView)
        sosButton = findViewById(R.id.sosButton)
        safetyModeSwitch = findViewById(R.id.safetyModeSwitch)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        cardLiveTracking = findViewById(R.id.cardLiveTracking)
        cardAnonymousReport = findViewById(R.id.cardAnonymousReport)
        cardWatch = findViewById(R.id.cardWatch)

        // Check Login
        if (auth.currentUser == null) {
            redirectToLogin()
        } else {
            checkIfUserFormIsFilled()
        }

        // SOS Trigger
        sosButton.setOnClickListener {
            val intent = Intent(this, SosTriggerActivity::class.java)
            startActivity(intent)
        }
        val intent = Intent(this, ForegroundShakeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }


        // Safety Mode Switch logic
        safetyModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Safety Mode ON" else "Safety Mode OFF"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Live Tracking
        cardLiveTracking.setOnClickListener {
            val intent = Intent(this, LiveTrackingActivity::class.java)
            startActivity(intent)
        }

        // Anonymous Report
        cardAnonymousReport.setOnClickListener {
            val intent = Intent(this, AnonymousReportActivity::class.java)
            startActivity(intent)
        }
        // Watch
        cardWatch.setOnClickListener {
            val intent = Intent(this, WatchActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_community -> {
                    Toast.makeText(this, "Community Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_me -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun checkIfUserFormIsFilled() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value?.toString() ?: "User"
                welcomeTextView.text = "Welcome, $name!"
            } else {
                startActivity(Intent(this, UserFormActivity::class.java))
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Inflate only the settings option in the Toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings Coming Soon", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}