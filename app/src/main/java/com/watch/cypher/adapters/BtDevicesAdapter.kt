package com.watch.cypher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.watch.cypher.R
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.btData


class BtDevicesAdapter(
    private val context: Context,
    private var contacts: List<btData> = emptyList() // Default to an empty list
) : RecyclerView.Adapter<UserViewHolder3>() {

    private lateinit var mListenerA: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListenerA = listener
    }

    override fun getItemCount(): Int {
        return contacts.size // Changed from event to contacts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder3 {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.devices_layout, parent, false)
        return UserViewHolder3(view, mListenerA)
    }

    override fun onBindViewHolder(holder: UserViewHolder3, position: Int) {
        val contact = contacts[position] // Get the contact at the current position
        holder.name.text = contact.name // Display the contact's id
        holder.adrs.text = contact.addres // Display the contact's id
        // Hide divider if it's the last item
        if (position == contacts.size - 1) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
    }
}

class UserViewHolder3(
    itemView: View,
    listener: BtDevicesAdapter.onItemClickListener,
) : RecyclerView.ViewHolder(itemView) {

    val name: TextView = itemView.findViewById(R.id.devicename)
    val adrs: TextView = itemView.findViewById(R.id.deviceaddress)
    val divider: FrameLayout = itemView.findViewById(R.id.divider) // ID for FrameLayout divider

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition) // Notify the listener on click
        }
    }
}

