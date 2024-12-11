package com.watch.cypher.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.dataModel.Like
import com.watch.cypher.dataModel.PostData
import me.relex.circleindicator.CircleIndicator3
import uk.bandev.xplosion.XplosionView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class FeedAdapter (private val context: Context, private val post: MutableList<PostData>) :
    RecyclerView.Adapter<UserViewHolder>() {
    private lateinit var mListenerA: onItemClickListener
    private lateinit var  viewPager2: ViewPager2
    private lateinit var adapter: ImageAdapter
    val theUser = MainActivity.mainUser

    interface onItemClickListener {
        fun onItemClick(position: Int, img: LinearLayout, authorPfp: Bitmap, like: Bitmap)
        fun onLike(position: Int, isLiked:Boolean)
        fun onOptions(position: Int)
        fun onProfile(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListenerA = listener
    }

    override fun getItemCount(): Int {
        return post.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(context,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.posts, parent, false),mListenerA,mListenerA,mListenerA,mListenerA
        )
    }
    fun calculateTimeAgo(createdAt: String): String {
        val dateString = extractDateTimeString(createdAt)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = Calendar.getInstance().time
        val postDate = sdf.parse(dateString)
        val timeDifferenceMillis = Math.abs(currentDate.time - postDate.time)

        val minuteMillis: Long = 60 * 1000
        val hourMillis: Long = 60 * minuteMillis
        val dayMillis: Long = 24 * hourMillis
        val weekMillis: Long = 7 * dayMillis
        val monthMillis: Long = 30 * dayMillis

        return when {
            timeDifferenceMillis < minuteMillis -> "${timeDifferenceMillis / 1000} secs ago"
            timeDifferenceMillis < hourMillis -> "${timeDifferenceMillis / minuteMillis} mins ago"
            timeDifferenceMillis < dayMillis -> "${timeDifferenceMillis / hourMillis} hours ago"
            timeDifferenceMillis < weekMillis -> "${timeDifferenceMillis / dayMillis} days ago"
            timeDifferenceMillis < monthMillis -> "${timeDifferenceMillis / weekMillis} weeks ago"
            else -> "${timeDifferenceMillis / monthMillis} months ago"
        }
    }

    private fun extractDateTimeString(createdAt: String): String {
        val regex = """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z""".toRegex()
        return regex.find(createdAt)?.value ?: throw ParseException("Invalid date format: $createdAt", 0)
    }
    fun formatTimestamp(createdAt: Timestamp): String {
        val date = createdAt.toDate() // Convert Timestamp to Date
        val format = SimpleDateFormat("MMM dd hh:mm a", Locale.getDefault()) // Define format pattern
        return format.format(date) // Format and return the date as a string
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val n = theUser
        holder.img.clipToOutline = true

        val post = post[position]
        val likesNumbers = post.likes
        val checklike : List<Like> = post.likes

        val hasLikeWithUsername = checklike.any { it.author == n.username }
        if (hasLikeWithUsername) {
            holder.likesbtn.imageTintList = null
            holder.isLiked = true
            holder.likesbtn.setImageResource(R.drawable.liked)
        } else{
            val typedValue = TypedValue()
            val theme = context.theme
            val colorAttr = com.google.android.material.R.color.m3_ref_palette_white
            theme.resolveAttribute(colorAttr, typedValue, true)
            holder.likesbtn.imageTintList = ColorStateList.valueOf(typedValue.data)
            holder.isLiked = false
            holder.likesbtn.setImageResource(R.drawable.unliked)
        }

        holder.author.text = post.author.username
        holder.postcontent.text = post.content
        holder.date.text = formatTimestamp(post.createdAt)
        holder.likes.text = "$likesNumbers Likes"
        holder.img2.transitionName = "anyString$position"
        holder.author.transitionName = "anyString2$position"
        viewPager2 = holder.media
        if (post.authorID == theUser.id){
            holder.option.visibility = View.VISIBLE
        }else{
            holder.option.visibility = View.GONE
        }
        if (post.media.isNotEmpty()){
            if (post.media.size <= 1)
                holder.ci.visibility = View.GONE
            else
                holder.ci.visibility = View.VISIBLE

            viewPager2.visibility = View.VISIBLE

            Log.i("MyAmplifqsdddqzqdqzdyApp7", "up ${holder.currentPageIndex}")

            adapter = ImageAdapter(context, post.media)
            viewPager2.adapter = adapter
            viewPager2.setCurrentItem(holder.currentPageIndex,false)
            holder.ci.setViewPager(viewPager2)


        }else{
            viewPager2.visibility = View.GONE
            holder.ci.visibility = View.GONE
        }
        if (post.author.pfpurl != null){
            Glide.with(context)
                .load(post.author.pfpurl)
                .into(holder.img)
        }else{
            holder.img.setImageResource(R.drawable.pfp)
        }
    }
}

class UserViewHolder(context: Context,
    itemView: View, listener: FeedAdapter.onItemClickListener,
    like: FeedAdapter.onItemClickListener,
    options: FeedAdapter.onItemClickListener, profile: FeedAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView) {

    var isLiked: Boolean = false
    var currentPageIndex: Int = 0 // Store current page index

    val author: TextView = itemView.findViewById(R.id.author)
    val postcontent: TextView = itemView.findViewById(R.id.content)
    val media: ViewPager2 = itemView.findViewById(R.id.main)
    val likes: TextView = itemView.findViewById(R.id.lks)
    val date: TextView = itemView.findViewById(R.id.date)
    val likesbtn: ImageView = itemView.findViewById(R.id.btn_like)
    val animator: XplosionView = itemView.findViewById(R.id.heart_animator)
    val img: ImageView = itemView.findViewById(R.id.postpfp)
    val img2: LinearLayout = itemView.findViewById(R.id.sndr)
    val ci: CircleIndicator3 = itemView.findViewById(R.id.ci)
    val option: ImageView = itemView.findViewById(R.id.option)

    init {
        media.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPageIndex = position
            }
        })
        itemView.setOnClickListener {
            listener.onItemClick(adapterPosition, img2, (img.drawable as BitmapDrawable).bitmap,(likesbtn.drawable as BitmapDrawable).bitmap)
        }

        likesbtn.setOnClickListener {
            like.onLike(adapterPosition, isLiked)
            isLiked = !isLiked
            if (isLiked) {
                likesbtn.imageTintList = null
                likesbtn.setImageResource(R.drawable.liked)
                animator.likeAnimation()
            }else{
                val typedValue = TypedValue()
                val theme = context.theme
                val colorAttr = com.google.android.material.R.attr.colorOnPrimary
                theme.resolveAttribute(colorAttr, typedValue, true)
                likesbtn.imageTintList = ColorStateList.valueOf(typedValue.data)
                likesbtn.setImageResource(R.drawable.unliked)
            }
        }

        option.setOnClickListener{ view ->
            val popupMenu = PopupMenu(context,view)

            val menuItems = listOf("Delete Post")

            for ((index, title) in menuItems.withIndex()) {
                popupMenu.menu.add(0, index, index, title)
            }

            // Set an item click listener for the menu items
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                // Handle item click
                when (item.itemId) {
                    0 -> {
                        options.onOptions(adapterPosition)
                        true
                    }
                    // Add more cases for other items as needed
                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()

        }
        img.setOnClickListener {
            profile.onProfile(adapterPosition)
        }
    }
}
