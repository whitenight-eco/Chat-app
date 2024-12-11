package com.watch.cypher.fragments

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.ConversationAdapter
import com.watch.cypher.dataManager.AppDao
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.MessageType
import com.watch.cypher.dataModel.UserData
import com.watch.cypher.databinding.FragmentConversationPageBinding
import com.watch.cypher.databinding.FragmentStartPageBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID


class StartPage : Fragment(R.layout.fragment_start_page) {
    private lateinit var binding: FragmentStartPageBinding
    private var currentLayoutIndex = 0
    private var img : ByteArray? = null
    private val TAG: String = "999ZDZDZ9dddzaddd"
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val PERMISSION_REQUEST_CODE = 1
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Register the activity result launcher here, before the fragment is created
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Handle the selected URI
            if (uri != null) {
                img = compressImage(uri,requireContext())
                binding.pfp.setImageURI(uri)
                binding.edits.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartPageBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayoutOffScreen(binding.second)
        binding.startBtn.setOnClickListener {
            if (currentLayoutIndex == 1){
                binding.startBtn.showProgress{
                    progressColor = Color.BLACK
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                binding.startBtn.isEnabled = false
                binding.edtimg.isEnabled = false
                val db = AppDatabase.getDatabase(requireContext())
                val appDao = db.appDao()

                lifecycleScope.launch {
                    // Add a new user after 2 seconds
                    addNewUser(
                        appDao,
                        UUID.randomUUID().toString().replace("-", "").take(12),
                        binding.nameEt.text.toString(), img
                    )
                }
            }else{
                // Slide out the current layout
                slideOut(currentLayoutIndex)

                // Update the current layout index to the next one
                currentLayoutIndex = (currentLayoutIndex + 1)
                // Slide in the next layout
                slideIn(currentLayoutIndex)
                binding.startBtn.text = "Continue"
                bindProgressButton(binding.startBtn)
                binding.startBtn.attachTextChangeAnimator()
            }


        }

        binding.edtimg.setOnClickListener {
            checkAndLaunchPhotoPicker()
        }

    }

    private suspend fun addNewUser(appDao: AppDao, userId: String, username: String, imgset: ByteArray?) {

        // Check if the user already exists in the local database
        val existingUser = appDao.getUserInfo()

        if (existingUser == null) {
            // User does not exist locally, proceed to add the user
            try {
                val imageUrl = uploadImageToFirebase(imgset)
                val user = UserData(id = userId, username = username, pfp = imgset,pfpurl = imageUrl)
                val userfb = UserData(id = userId, username = username, pfpurl = imageUrl)
                // Attempt to add the user to Firestore
                db.collection("Users").document(userId).set(userfb).await()
                Log.d(TAG, "User successfully added to Firestore: $userId")

                // Insert user into the local database
                appDao.insertUser(user)
                Log.d(TAG, "User successfully inserted into local database: $userId")

                // Verify if the user was added to the local database
                val newUser = appDao.getUserInfo()
                if (newUser != null && newUser.id == userId) {
                    MainActivity.mainUser = newUser

                    // Show success message and navigate to ContactsPage
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.screensholder, ContactsPage())
                        .addToBackStack(null)
                        .commit()
                }
                else {
                    // Handle failure case if user wasn't added to the local database
                    Toast.makeText(requireContext(), "Failed try again", Toast.LENGTH_SHORT).show()
                    Log.e("Database", "Failed to add user to local database: $userId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error adding user to Firestore: ${e.message}")

                // Even if Firestore fails, we add the user to the local database
                try {
                    // Insert user into the local database
                    val user = UserData(id = userId, username = username, pfp = imgset)
                    appDao.insertUser(user)
                    Log.d(TAG, "User successfully inserted into local database: $userId")

                    // Verify if the user was added to the local database
                    val newUser = appDao.getUserInfo()
                    if (newUser != null && newUser.id == userId) {
                        MainActivity.mainUser = newUser

                        // Show success message and navigate to ContactsPage
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.screensholder, ContactsPage())
                            .addToBackStack(null)
                            .commit()
                    } else {
                        binding.startBtn.isEnabled = true
                        binding.edtimg.isEnabled = true
                        binding.startBtn.hideProgress("Continue")
                        // Handle failure case if user wasn't added to the local database
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Failed to add user to local database: $userId")
                    }
                } catch (e: Exception) {
                    binding.startBtn.isEnabled = true
                    binding.edtimg.isEnabled = true
                    binding.startBtn.hideProgress("Continue")
                    // Handle any errors while inserting the user into the local database
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error adding user to local database: ${e.message}")
                }
            }

        } else {
            // User already exists in the local database
            Log.d(TAG, "User already exists")
            Toast.makeText(requireContext(), "User already exists", Toast.LENGTH_SHORT).show()

            // Navigate to ContactsPage without adding the user again
            parentFragmentManager.beginTransaction()
                .replace(R.id.screensholder, ContactsPage())
                .addToBackStack(null)
                .commit()
        }
    }

    suspend fun uploadImageToFirebase(imgset: ByteArray?): String? {
        // Ensure imgset is not null
        if (imgset == null) {
            Log.d(TAG, "Image data is null.")
            return null
        }

        // Firebase Storage reference
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("profile_images/${UUID.randomUUID()}.jpg") // Create a unique path for the image
        Log.d(TAG, "Starting image upload to Firebase Storage. Image path: ${imagesRef.path}")

        try {
            // Upload the image to Firebase Storage
            Log.d(TAG, "Uploading image...")
            val uploadTask = imagesRef.putBytes(imgset)
            uploadTask.await() // Wait for the upload to complete
            Log.d(TAG, "Image upload complete.")

            // Get the download URL of the uploaded image
            val downloadUrl = imagesRef.downloadUrl.await()
            Log.d(TAG, "Image uploaded successfully. Download URL: $downloadUrl")
            return downloadUrl.toString() // Return the download URL
        } catch (e: Exception) {
            // Handle errors
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            e.printStackTrace()
            return null
        }
    }

    private fun checkAndLaunchPhotoPicker() {
        if (checkStoragePermission()) {
            if (isPhotoPickerAvailable()) {
                // Launch the photo picker (choose either ImageOnly, VideoOnly, or ImageAndVideo)
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                // Handle fallback for devices that don't support the photo picker
                handleLegacyPhotoPicker()
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
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), PERMISSION_REQUEST_CODE)
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
                checkAndLaunchPhotoPicker()
            } else {
                Toast.makeText(requireContext(), "Permission Denied. You need to allow storage permission to pick a photo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleLegacyPhotoPicker() {
        // For older versions, we use the ACTION_OPEN_DOCUMENT intent
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = "image/*" // You can customize this to handle videos or both image/video
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

        try {
            startActivityForResult(pickIntent, PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error launching photo picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle the result for legacy devices that don't support the Photo Picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                img = compressImage(uri,requireContext())
                binding.pfp.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No Image selected", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage(uri: Uri, context: Context): ByteArray {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val originalSize = bitmap.byteCount
        Log.i(TAG, "Original image size: $originalSize bytes")

        val outputStream = ByteArrayOutputStream()

        // Compress the bitmap to a smaller size (e.g., 70% quality)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val compressedData = outputStream.toByteArray()

        Log.i(TAG, "Compressed image size: ${compressedData.size} bytes")
        return compressedData
    }
    //animations
    private fun slideOut(index: Int) {
        when (index) {
            0 -> slideLayout(binding.first, isSlideOut = true)
            1 -> slideLayout(binding.second, isSlideOut = true)
        }
    }

    private fun slideIn(index: Int) {
        when (index) {
            0 -> {
                binding.first.visibility = View.VISIBLE
                slideLayout(binding.first, isSlideOut = false)
            }
            1 -> {
                binding.second.visibility = View.VISIBLE
                slideLayout(binding.second, isSlideOut = false)
            }
        }
    }

    private fun slideLayout(layout: LinearLayout, isSlideOut: Boolean) {
        val width = layout.width.toFloat()

        // Set the initial position based on whether we are sliding in or out
        layout.translationX = if (isSlideOut) 0f else width
        layout.animate()
            .translationX(if (isSlideOut) -width else 0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(200)
            .start()
    }

    private fun setLayoutOffScreen(layout: LinearLayout) {
        layout.translationX = 10000f
    }
}