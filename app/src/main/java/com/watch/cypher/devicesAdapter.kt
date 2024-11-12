package com.watch.cypher

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class devicesAdapter (private val context: Context, private val event: MutableList<deviceData>) :
    RecyclerView.Adapter<UserViewHolder2>() {
    private lateinit var mListenerA: onItemClickListener


    interface onItemClickListener {
        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }

    override fun getItemCount(): Int {
        return event.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder2 {
        return UserViewHolder2(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.devices_layout, parent, false),mListenerA
        )
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: UserViewHolder2, position: Int) {
        val event = event[position]
        holder.name.text = event.device?.name?.toString() ?: "Unknown name"
        holder.address.text = event.device?.address?.toString() ?: "Unknown address"

    }
}

class UserViewHolder2(
    itemView: View, listener: devicesAdapter.onItemClickListener,
) : RecyclerView.ViewHolder(itemView) {

    val name: TextView = itemView.findViewById(R.id.devicename)
    val address: TextView = itemView.findViewById(R.id.deviceaddress)


    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }
    }
}
