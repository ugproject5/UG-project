package com.pant.girly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmergencyContactsAdapter(
    private val contactList: List<EmergencyContact>,
    private val onDelete: (EmergencyContact) -> Unit
) : RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contactList.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteContact)

        fun bind(contact: EmergencyContact) {
            tvName.text = contact.name
            tvPhone.text = contact.phone

            btnDelete.setOnClickListener {
                onDelete(contact)
            }
        }
    }
}
