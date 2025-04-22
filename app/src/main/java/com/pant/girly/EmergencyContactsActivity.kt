package com.pant.girly

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyContactsAdapter
    private val contactList = mutableListOf<EmergencyContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        recyclerView = findViewById(R.id.rvEmergencyContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EmergencyContactsAdapter(contactList, onDelete = { contact ->
            deleteContact(contact)
        })

        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnAddContact).setOnClickListener {
            addContact()
        }

        loadContacts()
    }

    private fun loadContacts() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseDatabase.getInstance().reference
            .child("emergency_contacts")
            .child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    contactList.clear()
                    for (contactSnapshot in snapshot.children) {
                        val contact = contactSnapshot.getValue(EmergencyContact::class.java)
                        contact?.let {
                            it.id = contactSnapshot.key.toString()
                            contactList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EmergencyContactsActivity, "Failed to load contacts", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addContact() {
        val name = findViewById<EditText>(R.id.etContactName).text.toString()
        val phone = findViewById<EditText>(R.id.etContactPhone).text.toString()

        if (name.isBlank() || phone.isBlank()) {
            Toast.makeText(this, "Please enter name and phone", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val contactRef = FirebaseDatabase.getInstance()
            .getReference("emergency_contacts")
            .child(user.uid)
            .push()

        val contact = EmergencyContact(
            name = name,
            phone = phone,
            id = contactRef.key ?: ""
        )

        contactRef.setValue(contact)
            .addOnSuccessListener {
                Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show()
                findViewById<EditText>(R.id.etContactName).text.clear()
                findViewById<EditText>(R.id.etContactPhone).text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
    }


    private fun deleteContact(contact: EmergencyContact) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        contact.id.let {
            FirebaseDatabase.getInstance().reference
                .child("emergency_contacts")
                .child(user.uid)
                .child(it)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    var id: String = ""
)
