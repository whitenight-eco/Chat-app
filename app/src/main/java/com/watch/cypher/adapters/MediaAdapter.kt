package com.watch.cypher.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.watch.cypher.R



class MediaAdapter (private val context: Context, private val post: MutableList<Uri>) :
    RecyclerView.Adapter<UserViewHolderMedia>() {
    private lateinit var mListenerA: onItemClickListener
    private lateinit var mListenerB: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int, img: Bitmap)
        fun onRemove(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
        mListenerB = listener
    }

    override fun getItemCount(): Int {
        return post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderMedia {
        return UserViewHolderMedia(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.media, parent, false),mListenerA,mListenerB
        )
    }

    fun isImageUri(context: Context, uri: String): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("image") == true
    }

    fun isVideoUri(context: Context, uri: String): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("video") == true
    }

    fun getMimeType(context: Context, uri: String): String? {
        return if (uri.startsWith("content")) {
            context.contentResolver.getType(android.net.Uri.parse(uri))
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri)
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        }
    }
    override fun onBindViewHolder(holder: UserViewHolderMedia, position: Int) {
        val post = post[position]
        Log.i("MyAmpsqdqdqzdqdlifyApp", "$post")

        val isImage = isImageUri(context, post.toString())
        val isVideo = isVideoUri(context, post.toString())

        if (isImage) {
            Glide.with(context)
                .load(post)
                .into(holder.media)
            holder.vid.visibility = View.GONE
        }
        else if (isVideo) {
            try {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(context, post)
                Glide.with(context)
                    .load(mediaMetadataRetriever.frameAtTime)
                    .into(holder.media)
                holder.vid.visibility = View.VISIBLE
            } catch (ex: Exception) {
                Toast
                    .makeText(context, "Error retrieving bitmap", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}

class UserViewHolderMedia(
    itemView: View, listener: MediaAdapter.onItemClickListener,
    remove: MediaAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {

    val media: ImageView = itemView.findViewById(R.id.main)
    val vid: ImageView = itemView.findViewById(R.id.vid)
    val removeBtn: ImageView = itemView.findViewById(R.id.remove)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition, (media.drawable as BitmapDrawable).bitmap)
        }
        removeBtn.setOnClickListener {
            remove.onRemove(adapterPosition)
        }
    }
}
