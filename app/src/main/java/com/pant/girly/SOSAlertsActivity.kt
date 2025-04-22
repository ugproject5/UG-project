package com.pant.girly

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SOSAlertsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SOSAlertsAdapter
    private val alertList = mutableListOf<SOSAlert>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos_alerts)

        recyclerView = findViewById(R.id.rvSOSAlerts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SOSAlertsAdapter(alertList)
        recyclerView.adapter = adapter

        loadAlerts()
    }

    private fun loadAlerts() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseDatabase.getInstance().reference
            .child("sos_alerts")
            .child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    alertList.clear()
                    for (alertSnapshot in snapshot.children) {
                        val alert = alertSnapshot.getValue(SOSAlert::class.java)
                        alert?.let { alertList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}

data class SOSAlert(
    val timestamp: Long = 0,
    val location: String = "",
    val status: String = ""
)