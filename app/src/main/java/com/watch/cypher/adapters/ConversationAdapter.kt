package com.watch.cypher.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.MessageType
import java.io.File
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
    val ITEM_imgRECEIVE = 3
    val ITEM_imgSENT = 4
    val ITEM_audioRECEIVE = 5
    val ITEM_audioSENT = 6
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ReceiveViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_others, parent, false), mListenerA
            )
            2 -> SendViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.msg_main, parent, false), mListenerA
            )
            3 -> ReceiveImgViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.imgmsg_others, parent, false), mListenerA
            )
            4 -> SendImgViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.imgmsg_main, parent, false), mListenerA
            )
            5 -> ReceiveAudioViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.voicemsg_others, parent, false), mListenerA
            )
            6 -> SendAudioViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.voicemsg_main, parent, false), mListenerA
            )
            else -> throw IllegalArgumentException("Invalid view type")
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
        when (holder) {
            is SendViewHolder -> {
                holder.contentMsg.text = convo[position].content
                holder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
            is ReceiveViewHolder -> {
                holder.contentMsg.text = convo[position].content
                holder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
            is SendImgViewHolder -> {
                val bitmap = byteArrayToBitmap(convo[position].imageUrl!!)
                Glide.with(context).load(bitmap).into(holder.img)
                holder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
            is ReceiveImgViewHolder -> {
                val bitmap = byteArrayToBitmap(convo[position].imageUrl!!)
                Glide.with(context).load(bitmap).into(holder.img)
                holder.time.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
            is SendAudioViewHolder -> {
                setupAudioPlayback(convo[position].voiceChat!!,context,holder.audio,holder.img,holder.time)
                holder.timeset.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
            is ReceiveAudioViewHolder -> {
                setupAudioPlayback(convo[position].voiceChat!!,context,holder.audio,holder.img,holder.time)
                holder.timeset.text = "${convertTimestampToDateString(convo[position].timestamp)} "
            }
        }
    }

    fun saveByteArrayToFile(byteArray: ByteArray, context: Context): File {
        val tempFile = File(context.cacheDir, "received_audio.m4a") // Temporary file
        tempFile.writeBytes(byteArray) // Write ByteArray to file
        return tempFile
    }

    fun setupAudioPlayback(
        byteArray: ByteArray,
        context: Context,
        waveformSeekBar: WaveformSeekBar,
        playButton: ImageView,
        timerTextView: TextView
    ) {
        try {
            // Save the received byte array to a file
            val audioFile = saveByteArrayToFile(byteArray, context)

            // Initialize the MediaPlayer
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath) // Set the data source from the file
                prepare() // Prepare the MediaPlayer
            }

            // Get the total duration of the audio
            val totalDuration = mediaPlayer.duration / 1000 // Duration in seconds
            val totalMinutes = totalDuration / 60
            val totalSeconds = totalDuration % 60
            timerTextView.text = String.format("%02d:%02d", totalMinutes, totalSeconds) // Set initial time

            // Set the WaveformSeekBar
            waveformSeekBar.setSampleFrom(audioFile.absolutePath)

            // Handler for updating progress and timer
            val handler = Handler(Looper.getMainLooper())
            val updateTask = object : Runnable {
                override fun run() {
                    if (mediaPlayer.isPlaying) {
                        // Update waveform progress
                        val progress = (mediaPlayer.currentPosition * 100 / mediaPlayer.duration)
                        waveformSeekBar.progress = progress.toFloat()

                        // Update timer TextView (countdown)
                        val remainingTime = (mediaPlayer.duration - mediaPlayer.currentPosition) / 1000
                        val minutes = remainingTime / 60
                        val seconds = remainingTime % 60
                        timerTextView.text = String.format("%02d:%02d", minutes, seconds)

                        // Repeat every 100ms
                        handler.postDelayed(this, 100)
                    }
                }
            }

            // Set button click listener to start or pause playback
            playButton.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    handler.removeCallbacksAndMessages(null) // Stop updates
                } else {
                    mediaPlayer.start()
                    handler.post(updateTask) // Start updating progress and timer
                }
            }

            // Handle completion
            mediaPlayer.setOnCompletionListener {
                handler.removeCallbacksAndMessages(null) // Stop updates
                waveformSeekBar.progress = 0f // Reset progress
                timerTextView.text = String.format("%02d:%02d", totalMinutes, totalSeconds) // Reset timer
                mediaPlayer.seekTo(0) // Reset MediaPlayer to the start
            }

            // Implement the onProgressChanged listener for WaveformSeekBar
            waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(
                    waveformSeekBar: WaveformSeekBar,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    if (fromUser) { // Only update if the user changed the progress
                        // Calculate the new position in milliseconds
                        val newPosition = (mediaPlayer.duration * progress) / 100
                        mediaPlayer.seekTo(newPosition.toInt()) // Update the MediaPlayer's position

                        // Update the timer TextView to reflect the new position
                        val remainingTime = (mediaPlayer.duration - newPosition) / 1000
                        val minutes = (remainingTime / 60).toInt() // Ensure it's an integer
                        val seconds = (remainingTime % 60).toInt() // Ensure it's an integer
                        timerTextView.text = String.format("%02d:%02d", minutes, seconds)

                    }
                }
            }

        } catch (e: Exception) {
            Log.e("AudioPlayback", "Error playing audio", e)
            Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show()
        }
    }



    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = convo[position]
        return if (mainUser == currentMessage.senderID){
            when (currentMessage.type) {
                MessageType.IMAGE -> ITEM_imgSENT
                MessageType.VOICE-> ITEM_audioSENT
                else -> ITEM_SENT
            }
        }else
            when (currentMessage.type) {
                MessageType.IMAGE -> ITEM_imgRECEIVE
                MessageType.VOICE-> ITEM_audioRECEIVE
                else -> ITEM_RECEIVE
            }
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

    class ReceiveImgViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
        val time: TextView = itemView.findViewById(R.id.timereceive)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class ReceiveAudioViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.playbtn)
        val time: TextView = itemView.findViewById(R.id.timer)
        val timeset: TextView = itemView.findViewById(R.id.timereceive)
        val audio: com.masoudss.lib.WaveformSeekBar = itemView.findViewById(R.id.waveformSeekBar)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class SendViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val contentMsg: TextView = itemView.findViewById(R.id.contentMain)
        val time: TextView = itemView.findViewById(R.id.timesend)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class SendImgViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img)
        val time: TextView = itemView.findViewById(R.id.timesend)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    class SendAudioViewHolder(itemView: View, listener: onItemClickListener ) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.playbtn)
        val time: TextView = itemView.findViewById(R.id.timer)
        val timeset: TextView = itemView.findViewById(R.id.timesend)
        val audio: com.masoudss.lib.WaveformSeekBar = itemView.findViewById(R.id.waveformSeekBar)
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


}
