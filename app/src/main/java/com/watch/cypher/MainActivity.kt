package com.watch.cypher


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.watch.cypher.R
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.UserData
import com.watch.cypher.databinding.ActivityMainBinding
import com.watch.cypher.fragments.ContactsPage
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val contactsPage: ContactsPage by lazy { ContactsPage() }
    companion object {
        lateinit var mainUser: UserData
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            val appDao = db.appDao()
            val existingUser = appDao.getUserInfo()
            if (existingUser != null){
                mainUser = existingUser
                Log.d("sqdfqijfozsef", mainUser.id)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showFragment(contactsPage, "contacts")

    }

    private fun showFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction().apply {
        }
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Detach all fragments
        supportFragmentManager.fragments.forEach { transaction.hide(it) }

        // Attach the selected fragment
        if (!fragment.isAdded) {
            transaction.add(R.id.screensholder, fragment, tag)
            // Add the fragment to the stack if it's not already there
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
    }
}