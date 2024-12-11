 package com.watch.cypher.fragments

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.FeedAdapter
import com.watch.cypher.adapters.MediaAdapter
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.Media
import com.watch.cypher.dataModel.PostData
import com.watch.cypher.dataModel.UserData2
import com.watch.cypher.databinding.FragmentFeedPageBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

 class FeedPage : Fragment(R.layout.fragment_feed_page) {
    private lateinit var binding: FragmentFeedPageBinding
     var mediaListed = mutableListOf<Uri>()
     private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
     private val PERMISSION_REQUEST_CODE = 1
     var allPosts = mutableListOf<PostData>()
     var author = MainActivity.mainUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
     override fun onAttach(context: Context) {
         super.onAttach(context)
         // Register the activity result launcher here, before the fragment is created
         pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
             // Handle the selected URI
             if (uri != null) {
                 mediaListed.add(uri)
                 binding.mainRecyclerviewMedia.adapter?.notifyDataSetChanged()
             }
         }
     }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedPageBinding.inflate(inflater, container, false)
        return binding.root

    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)

         val db = AppDatabase.getDatabase(requireContext())
         val contactDao = db.appDao()

         CoroutineScope(Dispatchers.IO).launch {
             fetchFeedsByAuthors(contactDao.getAllContactIds().toMutableList(), onSuccess = { posts ->
                 allPosts.clear() // Clear the old list
                 allPosts.addAll(posts) // Add the fetched posts to allPosts
                 binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                 // Update the UI (RecyclerView, etc.)
             }, onFailure = { e ->
                 Log.e("Error", "Failed to fetch feeds", e)
             })
         }


         binding.mainRecyclerviewMedia.apply {
             layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
             adapter = MediaAdapter(requireContext(), mediaListed).apply {
                 setOnItemClickListener(object : MediaAdapter.onItemClickListener {
                     override fun onItemClick(position: Int, img: Bitmap) {
                     }
                     override fun onRemove(position: Int) {
                         mediaListed.removeAt(position)
                         binding.mainRecyclerviewMedia.adapter?.notifyDataSetChanged()

                     }
                 })
             }
         }

         binding.addimg.setOnClickListener {
             checkAndLaunchMediaPicker()
         }

         binding.mainRecyclerview.apply {
             layoutManager = LinearLayoutManager(this.context)
             adapter = FeedAdapter(requireContext(), allPosts).apply {
                 setOnItemClickListener(object : FeedAdapter.onItemClickListener {
                     override fun onItemClick(
                         position: Int,
                         img: LinearLayout,
                         authorPfp: Bitmap,
                         like: Bitmap
                     ) {
                         val post = allPosts[position]

                     }
                     override fun onLike(position: Int, isLiked: Boolean) {
                         if (!isLiked && position != -1){
                             val postid = allPosts[position]
                             //likePost(theUser,postid)
                         }else if (isLiked && position != -1){
                             val postid = allPosts[position]
                             //unLikePost(theUser,postid)
                         }
                         // Notify the adapter that the data has changed
                     }

                     override fun onOptions(position: Int) {
                         //delete
                     }

                     override fun onProfile(position: Int) {
                     }
                 })
             }
         }

         binding.publish.setOnClickListener {
             val aut = UserData2(author.id, author.username, author.pfpurl)
             createPost(aut, binding.posttext.text.toString(), mediaListed)
         }


     }

     fun createPost(author: UserData2, content: String, mediaList: List<Uri>) {
         //hideKeyboard(binding.posttext)

         if (mediaList.isNotEmpty()) {
             var progress = 0
             val total = mediaList.size
             val scope = CoroutineScope(Dispatchers.Main)

             binding.progress.visibility = View.VISIBLE
             scope.launch {
                 val uploadedMediaList = mutableListOf<Media>() // Store uploaded media

                 try {
                     // Upload media concurrently
                     val deferredUploads = mediaList.map { post ->
                         async { uploadMediaToFirebase(post) }
                     }

                     deferredUploads.awaitAll().forEach { media ->
                         uploadedMediaList.add(media)
                         val progressIncrement = (100.0 / total).toInt()
                         updateProgressBar(progress, progress + progressIncrement)
                         progress += progressIncrement
                     }

                     // Once all media is uploaded, create the post in Firestore

                     val postID = UUID.randomUUID().toString()
                     val post = PostData(postID, author.id, author, content, Timestamp.now(), emptyList(), uploadedMediaList)

                     val db = FirebaseFirestore.getInstance()
                     db.collection("Feed")
                         .add(post)
                         .addOnSuccessListener { documentRef ->
                             Log.i("Firestore", "Post created with ID: ${documentRef.id}")
                             activity?.runOnUiThread {
                                 binding.progress.visibility = View.INVISIBLE
                                 binding.progress.progress = 0
                                 val db = AppDatabase.getDatabase(requireContext())
                                 val contactDao = db.appDao()
                                 CoroutineScope(Dispatchers.IO).launch {
                                     fetchFeedsByAuthors(contactDao.getAllContactIds().toMutableList(), onSuccess = { posts ->
                                         allPosts.clear() // Clear the old list
                                         allPosts.addAll(posts) // Add the fetched posts to allPosts
                                         binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                                         // Update the UI (RecyclerView, etc.)
                                     }, onFailure = { e ->
                                         Log.e("Error", "Failed to fetch feeds", e)
                                     })
                                 }
                                 mediaListed.clear()
                                 binding.posttext.text.clear()
                             }
                         }
                         .addOnFailureListener { error ->
                             Log.e("Firestore", "Failed to create post", error)
                         }
                 } catch (e: Exception) {
                     Log.e("CreatePost", "Error uploading media or creating post: ${e.message}", e)
                 }
             }
         } else {
             val postID = UUID.randomUUID().toString()
             // Handle the case where there is no media
             val post = PostData(postID, author.id, author, content, Timestamp.now(), emptyList(), mutableListOf())
             val db = FirebaseFirestore.getInstance()
             db.collection("Feed")
                 .add(post)
                 .addOnSuccessListener { documentRef ->
                     Log.i("Firestore", "Post created with ID: ${documentRef.id}")
                     activity?.runOnUiThread {
                         binding.progress.visibility = View.INVISIBLE
                         mediaListed.clear()
                         allPosts.add(0,post)
                         binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                         binding.posttext.text.clear()
                     }
                 }
                 .addOnFailureListener { error ->
                     Log.e("Firestore", "Failed to create post", error)
                 }
         }
     }

     private suspend fun uploadMediaToFirebase(post: Uri): Media {
         val deferred = CompletableDeferred<Media>() // Create a CompletableDeferred

         val storageReference = FirebaseStorage.getInstance().reference

         val isImage = isImageUri(requireContext(), post.toString())
         val isVideo = isVideoUri(requireContext(), post.toString())

         val fileName = UUID.randomUUID().toString()
         val fileRef = if (isImage) {
             storageReference.child("images/$fileName.jpg")
         } else if (isVideo) {
             storageReference.child("videos/$fileName.mp4")
         } else {
             null
         }

         if (fileRef != null) {
             try {
                 val uploadTask = fileRef.putFile(post).await()
                 val downloadUrl = fileRef.downloadUrl.await().toString()
                 val mediaType = if (isImage) "image" else "video"
                 deferred.complete(Media(downloadUrl, mediaType))
             } catch (e: Exception) {
                 Log.e("FirebaseStorage", "Error uploading media: ${e.message}", e)
                 deferred.completeExceptionally(e)
             }
         } else {
             deferred.completeExceptionally(Exception("Unsupported media type"))
         }

         return deferred.await()
     }

     private fun updateProgressBar(prg: Int = 0, nprg: Int = 0) {
         // Create a value animator that goes from 0 to 100 over 5 seconds
         val animator = ValueAnimator.ofInt(prg, nprg).apply {
             duration = 1000 // 5 seconds
             addUpdateListener { valueAnimator ->
                 // Update the progress bar with the current value
                 binding.progress.progress = valueAnimator.animatedValue as Int
             }
         }
         animator.start()
     }

     fun fetchFeedsByAuthors(authorIds: MutableList<String>, onSuccess: (List<PostData>) -> Unit, onFailure: (Exception) -> Unit) {
         val db = FirebaseFirestore.getInstance()
         authorIds.add(author.id)
         Log.d("FetchFeeds", "au $authorIds")

         // Step 1: Query the "feeds" collection where 'author' matches any of the IDs in the list
         db.collection("Feed")
             .whereIn("authorID", authorIds) // Fetch posts where the author is in the list of author IDs
             .orderBy("createdAt", Query.Direction.DESCENDING) // Order posts by 'createdAt' field
             .get()
             .addOnSuccessListener { feedSnapshot ->
                 if (!feedSnapshot.isEmpty) {
                     val posts = mutableListOf<PostData>()

                     // Step 2: Map the documents to PostData objects and add them to the list
                     for (document in feedSnapshot) {
                         val postData = document.toObject<PostData>() // Directly map the document to PostData
                         posts.add(postData) // Add to the list of posts
                         binding.mainRecyclerview.adapter?.notifyDataSetChanged()

                     }

                     // Step 3: Pass the list of posts to the success callback
                     onSuccess(posts)
                 } else {
                     Log.d("FetchFeeds", "No feeds found for the specified authors.")
                     onSuccess(emptyList()) // Return an empty list if no posts are found
                 }
             }
             .addOnFailureListener { e ->
                 Log.e("FetchFeeds", "Error fetching feeds: $e")
                 onFailure(e) // Pass the exception to the failure callback
             }
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
             MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
         }
     }



     private fun checkAndLaunchMediaPicker() {
         if (checkStoragePermission()) {
             if (isPhotoPickerAvailable()) {
                 // Launch the media picker (ImageAndVideo, ImageOnly, or VideoOnly)
                 pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) // You can also use .ImageOnly or .VideoOnly based on your needs
             } else {
                 // Handle fallback for devices that don't support the photo picker
                 handleLegacyMediaPicker()
             }
         } else {
             requestStoragePermission()
         }
     }

     private fun isPhotoPickerAvailable(): Boolean {
         return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R // Android 11 or higher
     }

     private fun checkStoragePermission(): Boolean {
         // Check for permission based on SDK version
         return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                     ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
         } else {
             ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
         }
     }

     private fun requestStoragePermission() {
         val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
         } else {
             arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
         }

         ActivityCompat.requestPermissions(requireActivity(), permission, PERMISSION_REQUEST_CODE)
     }

     // Handle permission results
     override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<String>,
         grantResults: IntArray
     ) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == PERMISSION_REQUEST_CODE) {
             if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 checkAndLaunchMediaPicker()
             } else {
                 Toast.makeText(requireContext(), "Permission Denied. You need to allow storage permission to pick a photo/video.", Toast.LENGTH_LONG).show()
             }
         }
     }

     private fun handleLegacyMediaPicker() {
         // For older versions, we use the ACTION_OPEN_DOCUMENT intent
         val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
         pickIntent.type = "*/*" // Allow both image and video
         pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

         try {
             startActivityForResult(pickIntent, PERMISSION_REQUEST_CODE)
         } catch (e: Exception) {
             Toast.makeText(requireContext(), "Error launching media picker: ${e.message}", Toast.LENGTH_LONG).show()
         }
     }

     // Handle the result for both images and videos
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         if (requestCode == PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
             val uri = data?.data
             if (uri != null) {
                 // Handle either image or video selection
                 val mimeType = requireContext().contentResolver.getType(uri)
                 if (mimeType?.startsWith("image/") == true) {
                     mediaListed.add(uri)
                     binding.mainRecyclerviewMedia.adapter?.notifyDataSetChanged()
                 } else if (mimeType?.startsWith("video/") == true) {
                     // Handle video selection
                     handleSelectedVideo(uri)
                 }
             } else {
                 Toast.makeText(requireContext(), "No media selected", Toast.LENGTH_SHORT).show()
             }
         } else {
             Toast.makeText(requireContext(), "Failed to select media", Toast.LENGTH_SHORT).show()
         }
     }

     private fun handleSelectedVideo(uri: Uri) {
         mediaListed.add(uri)
         binding.mainRecyclerviewMedia.adapter?.notifyDataSetChanged()
     }

     // Compress the selected image if it is an image
     private fun compressImage(uri: Uri, context: Context): ByteArray {
         val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
         val originalSize = bitmap.byteCount
         Log.i("TAG", "Original image size: $originalSize bytes")

         val outputStream = ByteArrayOutputStream()

         // Compress the bitmap to a smaller size (e.g., 70% quality)
         bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
         val compressedData = outputStream.toByteArray()

         Log.i("TAG", "Compressed image size: ${compressedData.size} bytes")
         return compressedData
     }

 }