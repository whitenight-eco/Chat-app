package com.watch.cypher

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.watch.cypher.databinding.ActivityMain2Binding
import com.watch.cypher.databinding.ItemMessageBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MainActivity2 : AppCompatActivity() {
    private var screenHeight : Int = 0
    private val TAG: String = "999ZDZDZ9"
    private var isHost: Boolean = false
    private lateinit var binding: ActivityMain2Binding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    private var mPairedDevices = listOf<BluetoothDevice>()
    private lateinit var mMessagesAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var eventSheet : BottomSheetBehavior<CardView>
    private var allPosts = mutableListOf<deviceData>()
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1

    private lateinit var mHandler: Handler

    // BroadcastReceiver to listen for discovered devices
    private val receiver2 = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!

                // Log discovered device
                Log.d("BluetoothDevice", "Discovered device: ${device.name} - ${device.address}")

                // Check if the device matches the specific one
                if (device.address == "DA:4C:10:DE:17:00") {
                    // Check if the device is already paired
                    val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices!!
                    val isPaired = pairedDevices.any { it.address == device.address }

                    if (isPaired) {
                        Log.d("BluetoothDevice", "Device already paired: ${device.name} - ${device.address}")
                    } else {
                        // Try to pair with the specific device
                        try {
                            val result = device.createBond()
                            if (result) {
                                Log.d("BluetoothDevice", "Pairing initiated with: ${device.name} - ${device.address}")
                            } else {
                                Log.d("BluetoothDevice", "Pairing failed with: ${device.name} - ${device.address}")
                            }
                        } catch (e: Exception) {
                            Log.e("BluetoothDevice", "Error pairing with device: ${device.name} - ${e.message}")
                        }
                    }
                } else {
                    Log.d("BluetoothDevice", "This device is not the target: ${device.name} - ${device.address}")
                }
            }
        }

    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            Log.d("BluetoothDeviceZZZZZZ", "onrec")
            if (BluetoothDevice.ACTION_FOUND == action) {
                Log.d("BluetoothDeviceZZZZZZ", "yes")
                val device: BluetoothDevice =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!

                // Log discovered device
                Log.d("BluetoothDeviceZZZZZZ", "Discovered device: ${device.name} - ${device.address}")

                val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
                val isPaired = pairedDevices.any { it.address == device.address }

                if (isPaired) {
                    Log.d("BluetoothDeviceZZZZZZ", "Device already paired: ${device.name} - ${device.address}")
                } else {
                    if (!allPosts.any { it.device?.address == device.address }) {
                        // Add the device address to the logged set
                        allPosts.add(deviceData(device))
                        binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                    } else {
                        Log.d("BluetoothDeviceZZZZZZ", "Device already logged: ${device.name} - ${device.address}")
                    }
                }
                // Check if the device matches the specific one

            }else{
                Log.d("BluetoothDeviceZZZZZZ", "no")
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        eventSheet = BottomSheetBehavior.from(binding.addeventsheet)
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        eventSheet.peekHeight = 0
        eventSheet.isDraggable = false
        checkBluetoothPermissions()

        binding.textView.setOnClickListener {
            AcceptThread().start()
        }
        binding.addbl.setOnClickListener {
            binding.mainRecyclerview.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = devicesAdapter(this@MainActivity2, allPosts).apply {
                    setOnItemClickListener(object : devicesAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            pearDevices(allPosts[position].device!!)
                        }
                    })
                }
            }
            if (areBluetoothPermissionsGranted()) {
                startBluetoothDiscovery()
            } else {
                requestBluetoothPermissions()
            }
            val animator = ValueAnimator.ofInt(0, screenHeight)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                eventSheet.peekHeight = height
            }
            animator.duration = 300  // Set the duration of the animation.
            animator.start()  // Start the animation.

        }
        binding.back.setOnClickListener {
            val animator = ValueAnimator.ofInt(screenHeight, 0)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                eventSheet.peekHeight = height
            }
            animator.duration = 300  // Set the duration of the animation.
            animator.start()  // Start the animation.
            setup()
            enableBluetooth()
        }
    }


    private fun setup() {
        binding.apply {
            rvResponse.layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            mMessagesAdapter = GroupAdapter()
            mMessagesAdapter.add(ChatMessageItem("Begin Conversation By Connecting To Another Device.....", "Help", resources.getColor(R.color.white, null)))
            rvResponse.adapter = mMessagesAdapter
        }
    }

    @SuppressLint("MissingPermission")


    private fun enableBluetooth() {
        Log.e(TAG, "CALLED !!!!!!!")
        // There's one Bluetooth adapter for the entire system, call getDefaultAdapter to get one
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Snackbar.make(
                binding.root,
                "Your Device Does Not Support Bluetooth.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        binding.tvDeviceName.text = bluetoothAdapter!!.name
        binding.tvDeviceAddress.text = bluetoothAdapter!!.address

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        setupBluetoothClientConnection()

        AcceptThread().start()
    }

    @SuppressLint("MissingPermission")
    private fun setupBluetoothClientConnection() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val allPairs: MutableList<String> = pairedDevices?.map { device ->
            val deviceName = device.name
            return@map deviceName
        }?.toMutableList() ?: mutableListOf()

        allPairs.add(0, "Select Connection")
        mPairedDevices = pairedDevices?.toList() ?: listOf()

        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPairs)
        binding.spinnerConnections.adapter = arrayAdapter
        binding.spinnerConnections.setSelection(0)
        binding.spinnerConnections.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    val selectedConnection: BluetoothDevice = pairedDevices!!.toList()[position - 1]
                    Log.d(TAG, "SELECTED")
                    val connectionSocket = ConnectThread(selectedConnection)
                    connectionSocket.start()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                AcceptThread().start()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Snackbar.make(binding.root, "Devices Bluetooth Enabled", Snackbar.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {

        // Create the server socket lazily
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                bluetoothAdapter!!.name, mUUID
            )
        }

        override fun run() {
            var shouldLoop = true

            while (shouldLoop) {
                // Establish a connection with a BluetoothSocket
                val socket: BluetoothSocket? = try {
                    Log.d(TAG, "AcceptThread: Waiting for incoming connections...")
                    mmServerSocket?.accept() // Blocking call until a connection is established or fails
                } catch (e: IOException) {
                    Log.e(TAG, "AcceptThread: Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }

                // When a connection is accepted
                socket?.also { bluetoothSocket ->
                    Log.i(TAG, "AcceptThread: Connection accepted with device ${bluetoothSocket.remoteDevice.name}")
                    val client = bluetoothSocket.remoteDevice.name

                    // Manage the connection in a separate thread
                    try {
                        manageServerSocketConnection(bluetoothSocket, client)
                    } catch (e: Exception) {
                        Log.e(TAG, "AcceptThread: Error managing server socket connection", e)
                        shouldLoop = false
                    }

                    // Post UI update for successful connection
                    mHandler.post {
                        val idx = mPairedDevices.indexOfFirst { it.name == client }
                        if (idx != -1) {
                            binding.tvConnectionLabel.text = "Connected To $client"
                        }
                        Log.i(TAG, "AcceptThread: UI updated for connection with $client")
                        Snackbar.make(
                            binding.root,
                            "Connection Established With $client",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    try {
                        Log.d(TAG, "AcceptThread: Closing server socket after successful connection")
                        mmServerSocket?.close() // Close the server socket after accepting the connection
                    } catch (e: IOException) {
                        Log.e(TAG, "AcceptThread: Could not close the server socket", e)
                    }
                    shouldLoop = false
                }
            }
            Log.d(TAG, "AcceptThread: Thread exiting")
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                Log.d(TAG, "AcceptThread: Canceling and closing the server socket")
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread: Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(val device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(mUUID)
        }

        override fun run() {
            bluetoothAdapter?.cancelDiscovery()  // Cancel discovery before attempting to connect

            mmSocket?.let { socket ->
                Log.d(TAG, "ConnectThread: Trying to connect to ${device.name}")

                try {
                    socket.connect()
                    Log.d(TAG, "ConnectThread: Connection successful!")
                } catch (e: IOException) {
                    Log.e(TAG, "ConnectThread: Failed to connect", e)

                    // Close the socket in case of failure to clear the connection state
                    try {
                        socket.close()
                        Log.d(TAG, "ConnectThread: Socket closed after failure.")
                    } catch (closeException: IOException) {
                        Log.e(TAG, "ConnectThread: Could not close the socket after failure", closeException)
                    }

                    // Exit the thread after failure to prevent further actions
                    return
                }

                // Proceed with connection if successful
                val client = socket.remoteDevice.name
                isHost = true
                manageServerSocketConnection(socket, client)

                // Notify UI of connection
                mHandler.post {
                    Snackbar.make(
                        binding.root,
                        "Connection Established With $client",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } ?: Log.e(TAG, "ConnectThread: BluetoothSocket is null.")
        }

        // Closes the socket when the connection is canceled
        fun cancel() {
            try {
                mmSocket?.close()
                Log.d(TAG, "ConnectThread: Socket closed during cancel.")
            } catch (e: IOException) {
                Log.e(TAG, "ConnectThread: Could not close the socket", e)
            }
        }
    }


    private inner class ConnectedThread(private val mmSocket: BluetoothSocket, val opName: String) :
        Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // Number of bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {

                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer) // Read bytes into buffer
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    AcceptThread().start()
                    break
                }

                // Ensure we only send the actual bytes read, not the entire buffer
                val actualData = mmBuffer.copyOf(numBytes)

                // Send the obtained bytes to the UI activity.
                val readMsg = mHandler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    opName to actualData
                )
                readMsg.sendToTarget()

                // Optional: Clear the buffer after processing the message (if needed)
                mmBuffer.fill(0)  // Reset the buffer to avoid residual data
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = mHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = mHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer
            )
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun manageServerSocketConnection(socket: BluetoothSocket, name: String) {
        // Generate the shared UUID when a connection is established
        Log.i("INFSSSSSD", "called msg")

        mHandler = Handler(this.mainLooper, Handler.Callback {
            try {
                val response = it.obj as Pair<String, ByteArray>
                val from = response.first
                val msg = response.second.decodeToString()
                Toast.makeText(this, "New Message Received", Toast.LENGTH_SHORT).show()
                mMessagesAdapter.add(
                    ChatMessageItem(
                        msg,
                        from,
                        resources.getColor(R.color.reply, null)
                    )
                )
                return@Callback true
            } catch (e: Exception) {
                return@Callback false
            }
        })

        // Start the communication thread
        val communicationService = ConnectedThread(socket, name)
        communicationService.start()

        if (isHost){
            Log.i("INFSSSSSD", "host called")
            val sharedUUID = UUID.randomUUID().toString()
            communicationService.write(sharedUUID.encodeToByteArray())
            isHost = false
        }
        // Send the shared UUID as the first message to both devices

        mHandler.post {
            binding.apply {
                etReply.isEnabled = true
                btnSendToConnected.setOnClickListener {
                    val text = etReply.text.toString()
                    communicationService.write(text.encodeToByteArray())
                    mMessagesAdapter.add(
                        ChatMessageItem(
                            text,
                            bluetoothAdapter!!.name,
                            resources.getColor(R.color.response, null)
                        )
                    )
                    etReply.setText("")
                }
            }
        }
    }


    companion object {
        const val REQUEST_ENABLE_BT = 100

        // Defines several constants used when transmitting messages between the
        // service and the UI.
        const val MESSAGE_READ: Int = 0
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_TOAST: Int = 2
    }


    fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                Log.e("pppprsm", "Permissions not granted. Requesting now...")

                // Request the missing permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_PERMISSIONS
                )
            } else {
                setup()
                ensureBluetoothReachable(this)
                Log.d("pppprsm", "Bluetooth permissions are already granted for Android 12+.")
            }
        } else {  // For Android versions below 12
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ) {

                Log.e("pppprsm", "Permissions not granted. Requesting now...")

                // Request the missing permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN),
                    REQUEST_BLUETOOTH_PERMISSIONS
                )
            } else {
                setup()
                ensureBluetoothReachable(this)
                Log.d("pppprsm", "Bluetooth permissions are already granted for Android versions below 12.")
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("pppprsm", "All Bluetooth permissions granted.")
                    setup()
                    ensureBluetoothReachable(this)
                } else {
                    Log.e("pppprsm", "Bluetooth permissions denied.")
                    // Handle the case where permissions are denied
                }
            }
        }
    }

    private val bluetoothDiscoverabilityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED == action) {
                val mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when (mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                        Log.d(TAG, "Device is discoverable.")
                        enableBluetooth()
                    }
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE, BluetoothAdapter.SCAN_MODE_NONE -> {
                        Log.d(TAG, "Device is no longer discoverable.")
                        // Handle the event here when discoverability ends
                        Toast.makeText(context, "Device is no longer discoverable.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    @SuppressLint("MissingPermission")



    fun ensureBluetoothReachable(context: Context) {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth")
            return
        }

        var allConditionsMet = true // Flag to check if all conditions are met

        // Step 1: Ensure Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Bluetooth is off. Turning it on...")
            bluetoothAdapter.enable()
        } else {
            Log.d(TAG, "Bluetooth is already enabled.")
        }

        // Step 2: Make the device discoverable
        if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Log.d(TAG, "Making device discoverable...")
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 300 seconds discoverable
            }
            context.startActivity(discoverableIntent)  // Pass the activity or fragment's context
            allConditionsMet = false // Condition not met as we had to make it discoverable
        } else {
            Log.d(TAG, "Device is already discoverable.")
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        context.registerReceiver(bluetoothDiscoverabilityReceiver, filter)
        // Execute the last two functions only if all conditions are met
        if (allConditionsMet) {
            enableBluetooth()
        }
    }






    //NOT NOW _________________________________________________________________

    private fun areBluetoothPermissionsGranted(): Boolean {
        val permissions = mutableListOf<String>()

        // Check permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(android.Manifest.permission.BLUETOOTH)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val permissionsArray = permissions.toTypedArray()

        return permissionsArray.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()

        // Add permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(android.Manifest.permission.BLUETOOTH)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            REQUEST_ENABLE_BT
        )
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothDiscovery() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.d("BluetoothDeviceZZZZZZ", "Bluetooth not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        }
    }




    @SuppressLint("MissingPermission")
    private fun logPairedDevices() {
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            Log.d("BluetoothDevices", "Paired devices:")
            for (device in pairedDevices) {
                Log.d("BluetoothDevice", "Paired device: ${device.name} - ${device.address}")

            }
        } else {
            Log.d("BluetoothDevices", "No paired devices found")
        }
    }















    @SuppressLint("MissingPermission")
    private fun discoverDevices() {
        Log.d("BluetoothDeviceZZZZZZ", "sstart descov")

        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
            Log.d("BluetoothDeviceZZZZZZ", "sstoped")

        }
        bluetoothAdapter!!.startDiscovery()
        Log.d("BluetoothDeviceZZZZZZ", "Discovery started")
    }

    @SuppressLint("MissingPermission")
    private fun pearDevices(device:BluetoothDevice){
        try {
            val result = device.createBond()
            if (result) {
                Log.d("BluetoothDevice", "Pairing initiated with: ${device.name} - ${device.address}")
            } else {
                Log.d("BluetoothDevice", "Pairing failed with: ${device.name} - ${device.address}")
            }
        } catch (e: Exception) {
            Log.e("BluetoothDevice", "Error pairing with device: ${device.name} - ${e.message}")
        }
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(bluetoothDiscoverabilityReceiver)
    }
    @SuppressLint("MissingPermission")
    fun connectToDevice(bluetoothDevice: BluetoothDevice) {
        // Check if the device is already paired
        if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
            Log.d("Direct111", "Device is already paired: ${bluetoothDevice.name}")
            // Proceed with connection
            connect(bluetoothDevice)
        } else {
            Log.d("Direct111", "Device is not paired, attempting to pair with: ${bluetoothDevice.name}")
            // Try to pair the device
            try {
                val pairingResult = bluetoothDevice.createBond()
                if (pairingResult) {
                    Log.d("Direct111", "Pairing initiated with: ${bluetoothDevice.name}")
                    // Listen for pairing success or failure
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val action = intent.action
                            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                                if (state == BluetoothDevice.BOND_BONDED) {
                                    Log.d("Direct111", "Pairing successful with: ${bluetoothDevice.name}")
                                    // Once paired, attempt connection
                                    connect(bluetoothDevice)
                                    context.unregisterReceiver(this)
                                } else if (state == BluetoothDevice.BOND_NONE) {
                                    Log.e("Direct111", "Pairing failed with: ${bluetoothDevice.name}")
                                    context.unregisterReceiver(this)
                                }
                            }
                        }
                    }
                    // Register the receiver for pairing events
                    this.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                } else {
                    Log.e("Direct111", "Pairing failed with: ${bluetoothDevice.name}")
                }
            } catch (e: Exception) {
                Log.e("Direct111", "Error pairing with device: ${bluetoothDevice.name} - ${e.message}")
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun connectToDeviceByAddress(bluetoothAdapter: BluetoothAdapter, address: String) {
        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(address)

        if (bluetoothDevice != null) {
            // Check if the device is already paired
            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d("TTTTT23", "Device is already paired: ${bluetoothDevice.name}")
                // Proceed with connection
                connect(bluetoothDevice)
            } else {
                Log.d("TTTTT23", "Device is not paired, attempting to pair with: $address")
                // Try to pair the device
                try {
                    val pairingResult = bluetoothDevice.createBond()
                    if (pairingResult) {
                        Log.d("TTTTT23", "Pairing initiated with: $address")
                        // Listen for pairing success or failure
                        val receiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                val action = intent.action
                                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                                    if (state == BluetoothDevice.BOND_BONDED) {
                                        Log.d("TTTTT23", "Pairing successful with: $address")
                                        // Once paired, attempt connection
                                        connect(bluetoothDevice)
                                        context.unregisterReceiver(this)
                                    } else if (state == BluetoothDevice.BOND_NONE) {
                                        Log.e("TTTTT23", "Pairing failed with: $address")
                                        context.unregisterReceiver(this)
                                    }
                                }
                            }
                        }
                        // Register the receiver for pairing events
                        this.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                    } else {
                        Log.e("TTTTT23", "Pairing failed with: $address")
                    }
                } catch (e: Exception) {
                    Log.e("TTTTT23", "Error pairing with device: $address - ${e.message}")
                }
            }
        } else {
            Log.e("TTTTT23", "Bluetooth device not found for address: $address")
        }
    }

    fun connect(bluetoothDevice: BluetoothDevice) {
        val connectThread = ConnectThread(bluetoothDevice)
        connectThread.start() // Start the connection thread
    }
}

class ChatMessageItem(
    private val message: String,
    private val name: String,
    private val color: Int
) :
    BindableItem<ItemMessageBinding>() {
    override fun bind(viewBinding: ItemMessageBinding, position: Int) {
        viewBinding.apply {
            tvFrom.text = name
            tvMessage.text = message
            cardRoot.setCardBackgroundColor(color)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_message
    }

    override fun initializeViewBinding(view: View): ItemMessageBinding {
        return ItemMessageBinding.bind(view)
    }
}