package com.watch.cypher.fragments

import android.animation.Animator
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.showProgress
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.ConversationAdapter
import com.watch.cypher.dataManager.AppDao
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.UserData
import com.watch.cypher.databinding.FragmentConversationPageBinding
import com.watch.cypher.databinding.FragmentStartPageBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID


class StartPage : Fragment(R.layout.fragment_start_page) {
    private lateinit var binding: FragmentStartPageBinding
    private var currentLayoutIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                // Set a 2-second delay using Handler
                Handler(Looper.getMainLooper()).postDelayed({
                    val db = AppDatabase.getDatabase(requireContext())
                    val appDao = db.appDao()

                    lifecycleScope.launch {
                        // Add a new user after 2 seconds
                        addNewUser(
                            appDao,
                            UUID.randomUUID().toString().replace("-", "").take(12),
                            binding.nameEt.text.toString()
                        )
                    }
                }, 2000) // 2000 milliseconds = 2 seconds
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

    }

    private suspend fun addNewUser(appDao: AppDao, userId: String,username:String) {
        // Check if user already exists
        val existingUser = appDao.getUserInfo()

        if (existingUser == null) {
            val user = UserData(id = userId, username)
            // User does not exist, insert new user
            appDao.insertUser(user)
            Log.d("checkfs", "New user added with ID: $userId")

            // Verify that the user was added
            val newUser = appDao.getUserInfo()
            if (newUser != null && newUser.id == userId) {
                MainActivity.mainUser = newUser
                parentFragmentManager.beginTransaction()
                    // Replace Fragment A with Fragment B
                    .replace(R.id.screensholder, ContactsPage())
                    // If you don't want Fragment A in the back stack, don't add it
                    .addToBackStack(null) // Optional: adds Fragment B to the back stack
                    .commit()
            } else {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                Log.e("checkfs", "Failed to add user with ID: $userId")
            }
        } else {
            Log.d("checkfs", "User already exists with ID: ${existingUser.id}")
            parentFragmentManager.beginTransaction()
                // Replace Fragment A with Fragment B
                .replace(R.id.screensholder, ContactsPage())
                // If you don't want Fragment A in the back stack, don't add it
                .addToBackStack(null) // Optional: adds Fragment B to the back stack
                .commit()
        }
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