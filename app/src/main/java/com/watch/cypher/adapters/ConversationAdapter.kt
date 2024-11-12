package com.watch.cypher.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.dataModel.MessageData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter (private val context: Context, private var convo: List<MessageData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mainUser = MainActivity.mainUser.id

    private lateinit var mListenerA: onItemClickListener

    fun updateMessages(newMessage: List<MessageData>) {
        convo = newMessage // Safely assign the new list or default to empty
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 1){
            ReceiveViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_others, parent, false),mListenerA
            )
        }else{
            SendViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_main, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return convo.size
    }

    fun convertTimestampToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.javaClass == SendViewHolder::class.java){
            val viewHolder = holder as SendViewHolder
            viewHolder.contentMsg.text = convo[position].content
            viewHolder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "

        }else{
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.contentMsg.text = convo[position].content
            viewHolder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = convo[position]

        return if (mainUser == currentMessage.senderID){
            ITEM_SENT
        }else
            ITEM_RECEIVE
    }

    class ReceiveViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val contentMsg: TextView = itemView.findViewById(R.id.content)
        val time: TextView = itemView.findViewById(R.id.timereceive)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class SendViewHolder(itemView: View, ) : RecyclerView.ViewHolder(itemView) {
        val contentMsg: TextView = itemView.findViewById(R.id.contentMain)
        val time: TextView = itemView.findViewById(R.id.timesend)
    }

}
