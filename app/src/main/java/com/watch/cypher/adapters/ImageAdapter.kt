package com.watch.cypher.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.watch.cypher.R
import com.watch.cypher.dataModel.Media

class ImageAdapter(private val context: Context, private val imageList: MutableList<Media>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private var player: SimpleExoPlayer? = null

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image);
        val vid: StyledPlayerView = itemView.findViewById(R.id.playvid)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_container, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        player = SimpleExoPlayer.Builder(context).build()
        holder.vid.player = player
        val post = imageList[position]
        if (post.type == "image"){
            holder.vid.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.url)
                .into(holder.imageView)
        }else if (post.type == "video"){
            holder.imageView.visibility = View.GONE
            holder.vid.visibility = View.VISIBLE
            val mediaItem = MediaItem.fromUri(post.url!!)
            player?.setMediaItem(mediaItem)
            player?.prepare()
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

}