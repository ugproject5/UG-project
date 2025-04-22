package com.pant.girly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SOSAlertsAdapter(private val alerts: List<SOSAlert>) :
    RecyclerView.Adapter<SOSAlertsAdapter.SOSAlertViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SOSAlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_alert, parent, false)
        return SOSAlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: SOSAlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.bind(alert)
    }

    override fun getItemCount() = alerts.size

    class SOSAlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(alert: SOSAlert) {
            // Format timestamp
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = Date(alert.timestamp)
            tvTime.text = dateFormat.format(date)

            tvLocation.text = alert.location
            tvStatus.text = when(alert.status) {
                "pending" -> "Pending Response"
                "responded" -> "Responded"
                else -> "Alert Sent"
            }

            // Set status color
            val statusColor = when(alert.status) {
                "pending" -> itemView.context.getColor(R.color.red)
                "responded" -> itemView.context.getColor(R.color.green)
                else -> itemView.context.getColor(R.color.orange)
            }
            tvStatus.setTextColor(statusColor)
        }
    }
}