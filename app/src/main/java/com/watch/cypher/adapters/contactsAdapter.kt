package com.watch.cypher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.watch.cypher.R
import com.watch.cypher.dataModel.ContactData


class ContactsAdapter(
    private val context: Context,
    private var contacts: List<ContactData> = emptyList() // Default to an empty list
) : RecyclerView.Adapter<UserViewHolder2>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder2 {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_layout, parent, false)
        return UserViewHolder2(view, mListenerA)
    }

    fun updateContacts(newContacts: List<ContactData>?) {
        contacts = newContacts ?: emptyList() // Safely assign the new list or default to empty
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    override fun onBindViewHolder(holder: UserViewHolder2, position: Int) {
        val contact = contacts[position] // Get the contact at the current position
        holder.name.text = contact.username // Display the contact's id

        // imageSource will be either the URL (String) or the ByteArray
        val imageSource: Any? = contact.pfpurl ?: contact.pfp

        // Check if there is an image source (either URL or byte array)
        if (imageSource != null) {
            when (imageSource) {
                is String -> {
                    // Load the image from URL
                    Glide.with(holder.itemView.context)
                        .load(imageSource) // Load URL
                        .into(holder.img) // Set the image into ImageView
                }
                is ByteArray -> {
                    // Load the image from byte array
                    Glide.with(holder.itemView.context)
                        .load(imageSource) // Load from byte array
                        .into(holder.img) // Set the image into ImageView
                }
                else -> {
                    // If neither URL nor ByteArray, you could load a default image
                    holder.img.setImageResource(R.drawable.pfp)
                }
            }
        } else {
            // Handle the case when there is no image (both URL and byte array are null)
            holder.img.setImageResource(R.drawable.pfp) // Set a default image if no data is found
        }
    }
}

class UserViewHolder2(
    itemView: View,
    listener: ContactsAdapter.onItemClickListener,
) : RecyclerView.ViewHolder(itemView) {

    val name: TextView = itemView.findViewById(R.id.userID)

    val img: ImageView = itemView.findViewById(R.id.img)
    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition) // Notify the listener on click
        }
    }
}

