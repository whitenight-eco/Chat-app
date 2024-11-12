package com.watch.cypher.fragments

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.watch.cypher.PortraitCaptureActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.BtDevicesAdapter
import com.watch.cypher.adapters.ContactsAdapter
import com.watch.cypher.dataManager.AppDao
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.UserData
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.ConversationData
import com.watch.cypher.dataModel.btData
import com.watch.cypher.databinding.FragmentContactsPageBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.UUID


class ContactsPage : Fragment(R.layout.fragment_contacts_page) {
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var eventSheet : BottomSheetBehavior<CardView>
    private val discoveredDevices = mutableListOf<btData>() // Store discovered devices
    private lateinit var binding: FragmentContactsPageBinding
    private var allPosts = mutableListOf<ContactData>()
    private val TAG: String = "999ZDZDZ9"
    private lateinit var mHandler: Handler

    private val mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Registering the barcode launcher
        barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null) {
                Log.e("ContactsPageqzdqzdqd", "Scan result is null (canceled or failed scan)")
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d("ContactsPageqzdqdqz", "Scanned QR Code: ${result.contents}")
                Toast.makeText(requireContext(), "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()

            }
        }
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContactsPageBinding.inflate(inflater, container, false)
        eventSheet = BottomSheetBehavior.from(binding.addeventsheet)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        eventSheet.peekHeight = 0
        eventSheet.isDraggable = false
        // Add a new user
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()
        lifecycleScope.launch {
            // Step 1: Add a new user
            addNewUser(appDao, UUID.randomUUID().toString().replace("-", "").take(12))

            // Step 2: Fetch the contacts in the coroutine
            val contacts = getContactsList(appDao)

            allPosts.addAll(contacts)
            // Step 3: Set up the RecyclerView with the fetched contacts
            binding.mainRecyclerview.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = ContactsAdapter(requireContext(), allPosts).apply {
                    setOnItemClickListener(object : ContactsAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val bundle = Bundle()
                            bundle.putString("convoID", allPosts[position].conversationId)
                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                            val frg = ConversationPage()
                            frg.arguments = bundle
                            // Check if the fragment is already added
                            if (!frg.isAdded) {
                                transaction.add(R.id.screensholder, frg, "ConversationPage")
                            }

                            // Hide all fragments
                            requireActivity().supportFragmentManager.fragments.forEach { transaction.hide(it) }

                            // Show the selected fragment
                            transaction.show(frg)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }
                    })
                }
            }
        }

        var searchHandler: Handler? = null
        val searchView = requireView().findViewById<androidx.appcompat.widget.SearchView>(R.id.srchv)
        val searchTextView = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchTextView.setTextColor(Color.BLACK)
        searchTextView.setHintTextColor(Color.GRAY)
        val textSizeInPixels = 14 * resources.displayMetrics.density
        searchTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels)
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        val closeIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)

        binding.addcontact.setOnClickListener {
            binding.mainRecyclerviewbt.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = BtDevicesAdapter(requireActivity(), discoveredDevices).apply {
                    setOnItemClickListener(object : BtDevicesAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val bundle = Bundle()
                            bundle.putInt("convoType", 1)
                            bundle.putString("convoID", discoveredDevices[position].addres)
                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                            val frg = ConversationPage()
                            frg.arguments = bundle
                            // Check if the fragment is already added
                            if (!frg.isAdded) {
                                transaction.add(R.id.screensholder, frg, "ConversationPage")
                            }

                            // Hide all fragments
                            requireActivity().supportFragmentManager.fragments.forEach { transaction.hide(it) }

                            // Show the selected fragment
                            transaction.show(frg)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }
                    })
                }
            }
            val animator = ValueAnimator.ofInt(0, screenHeight)
            animator.addUpdateListener { valueAnimator ->
                // Update the peek height of the first bottom sheet during the animation.
                val height = valueAnimator.animatedValue as Int
                eventSheet.peekHeight = height
            }
            animator.duration = 300  // Set the duration of the animation.
            animator.start()  // Start the animation.
            binding.lottieView.setOnClickListener {
                binding.lottieView.playAnimation()
                binding.swiperbt.setOnRefreshListener {
                    startBluetoothScan()
                }
                startBluetoothScan()

            }

            binding.backbtn.setOnClickListener {

                val animator = ValueAnimator.ofInt(screenHeight, 0)
                animator.addUpdateListener { valueAnimator ->
                    // Update the peek height of the first bottom sheet during the animation.
                    val height = valueAnimator.animatedValue as Int
                    eventSheet.peekHeight = height
                }
                animator.duration = 300  // Set the duration of the animation.
                animator.start()  // Start the animation.
            }

            //checkAndCreateDocument("Convs",generateConsistentHashedId(mainUser.id,"35f4c40b2c1e"),mainUser.id,"35f4c40b2c1e")
        }

        startBluetoothAcceptThread()



    }

    private fun launchScannerQR(){
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("sssssssss")
            setCaptureActivity(PortraitCaptureActivity::class.java)  // Point to the custom capture activity
        }

        barcodeLauncher.launch(options)
    }

    private suspend fun getContactsList(appDao: AppDao): List<ContactData> {
        val contactsList = appDao.getAllContacts()
        if (contactsList.isNotEmpty()) {
            Log.d("checkfs", "User's contacts: $contactsList")
        } else {
            Log.d("checkfs", "No contacts found or user does not exist.")
        }
        return contactsList
    }

    private suspend fun addNewUser(appDao: AppDao, userId: String) {
        // Check if user already exists
        val existingUser = appDao.getUserInfo()

        if (existingUser == null) {
            // User does not exist, insert new user
            appDao.insertUser(UserData(id = userId))
            Log.d("checkfs", "New user added with ID: $userId")
        } else {
            Log.d("checkfs", "User already exists with ID: ${existingUser.id}")
        }
    }

    private suspend fun addContact(appDao: AppDao, contactId: String, convoID:String) {
        // Check if contact already exists
        val existingContact = appDao.getContactById(contactId)

        if (existingContact == null) {
            // Contact does not exist, insert new contact
            appDao.insertContact(ContactData(id = contactId, conversationId = convoID))
            updateContactsList()
            Log.d("checkfs", "New contact added with ID: $contactId")
        } else {
            Log.d("checkfs", "Contact already exists with ID: ${existingContact.id}")
        }
    }

    private fun updateContactsList() {
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()
        lifecycleScope.launch {
            allPosts.clear()
            val contacts = getContactsList(appDao) // Fetch updated contacts
            allPosts.addAll(contacts)
            binding.mainRecyclerview.adapter?.notifyDataSetChanged()
        }
    }

    fun checkAndCreateDocument(collectionPath: String, documentId: String, userID: String, contactID:String) {
        val ldb = AppDatabase.getDatabase(requireContext())
        val appDao = ldb.appDao()
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(collectionPath).document(documentId)
        val conversation = ConversationData(conversationId = documentId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Document exists, retrieve the current members list
                    val currentMembers = document.get("members") as? MutableList<String> ?: mutableListOf()
                    if (!currentMembers.contains(userID)) {
                        // Add userID if it's not already in the list
                        currentMembers.add(userID)
                        docRef.update("members", currentMembers)
                            .addOnSuccessListener {
                                Log.d("checkfs", "UserID added to existing document")
                                lifecycleScope.launch {
                                    appDao.insertConversation(conversation)
                                    addContact(appDao, contactID, document.id)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d("checkfs", "Error updating document: $e")
                            }
                    } else {
                        Log.d("checkfs", "UserID already exists in the document")
                    }
                } else {
                    // Document does not exist, create it with userID in the members list
                    val data = mapOf("members" to listOf(userID))
                    docRef.set(data)
                        .addOnSuccessListener {
                            Log.d("checkfs", "Document created successfully with userID in members list")
                            lifecycleScope.launch {
                                appDao.insertConversation(conversation)
                                addContact(appDao, contactID, document.id)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.d("checkfs", "Error creating document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.d("checkfs", "Error fetching document: $e")
            }
    }

    fun generateConsistentHashedId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        val concatenated = "${sortedIds[0]}_${sortedIds[1]}"
        val md = MessageDigest.getInstance("SHA-256") // or "MD5" for a shorter hash
        val hashBytes = md.digest(concatenated.toByteArray())
        Log.d("sqdfqijfozsef", hashBytes.joinToString("") { "%02x".format(it) } )
        return hashBytes.joinToString("") { "%02x".format(it) } // Convert to hex string
    }






    //bluetooth part _________________________________________________

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanning = false
    private val handler = Handler()

    private fun startBluetoothScan() {
        if (!checkPermissions(1)) return

        handler.postDelayed({
            binding.lottieView.cancelAnimation()
            binding.lottieView.visibility = View.GONE
            binding.swiperbt.isRefreshing = false
            binding.swiperbt.visibility = View.VISIBLE
            stopBluetoothScan()
        }, 5000) // Stop scanning after 5 seconds

        startScan()
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        binding.scantext.visibility = View.GONE
        if (scanning) return
        scanning = true

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(receiver, filter)

        bluetoothAdapter.startDiscovery()
        Log.d("Bluetzqdqzdqdooth", "Scanning for devices...")
    }

    @SuppressLint("MissingPermission")
    private fun stopBluetoothScan() {
        if (!scanning) return
        scanning = false

        bluetoothAdapter.cancelDiscovery()
        requireActivity().unregisterReceiver(receiver)

        Log.d("Bluetzqdqzdqdooth", "Stopped scanning.")
    }

    @SuppressLint("MissingPermission")
    private fun checkAndPairDevice(device: BluetoothDevice) {
        // Check if the device is already paired
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        val isPaired = pairedDevices.any { it.address == device.address }

        if (!isPaired) {
            // Pairing logic here
            Log.d("Bluetzqdqzdqdooth", "Pairing with device: ${device.name} - ${device.address}")
            //pairDevice(device)
        } else {
            Log.d("Bluetzqdqzdqdooth", "Device already paired: ${device.name} - ${device.address}")
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    // Check if device is already in the list
                    if (discoveredDevices.none { d -> d.addres == it.address }) {
                        // Add new device to the list and update adapter
                        val deviceName = it.name ?: "Unknown"
                        val deviceAddress = it.address ?: "N/A"

                        // Log the found device's name and address
                        Log.d("BluetozqqdqqzddothDevice", "Found device: $deviceName - $deviceAddress")

                        // Add the device to the list
                        discoveredDevices.add(btData(deviceName, deviceAddress,device))
                        binding.mainRecyclerviewbt.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun pairDevice(device: BluetoothDevice) {
        try {
            // Initiate pairing (bonding)
            val method = device.javaClass.getMethod("createBond")
            method.invoke(device)
            Log.d("Bluetzqdqzdqdooth", "Pairing initiated with ${device.name}")
        } catch (e: Exception) {
            Log.e("Bluetzqdqzdqdooth", "Pairing failed: ${e.message}")
        }
    }

    private fun checkPermissions(code:Int): Boolean {
        val permissionsNeeded = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        val permissionsDenied = permissionsNeeded.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsDenied.isNotEmpty()) {
            requestPermissions(permissionsDenied.toTypedArray(), code)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted for Bluetooth scan
                    startBluetoothScan()
                } else {
                    Toast.makeText(requireContext(), "Bluetooth scan permissions are required", Toast.LENGTH_SHORT).show()
                    Log.e("Bluetooth", "Scan permissions not granted")
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted for Bluetooth connection
                    AcceptThread().start()
                } else {
                    Toast.makeText(requireContext(), "Bluetooth connection permissions are required", Toast.LENGTH_SHORT).show()
                    Log.e("Bluetooth", "Connection permissions not granted")
                }
            }
            else -> {
                Log.e("Bluetooth", "Unexpected request code: $requestCode")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopBluetoothScan() // Ensure scanning is stopped when fragment is destroyed.
    }

    private fun generateQRCode(bluetoothAddress: String) {
        try {
            val bitMatrix: BitMatrix = QRCodeWriter().encode(bluetoothAddress, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565).apply {
                for (x in 0 until 512) {
                    for (y in 0 until 512) {
                        setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            }

            // Set the generated QR code to an ImageView (replace qrImageView with your actual ImageView)
            binding.qrHolder.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            Log.e("BluetoothFragment", "Error generating QR code: ${e.localizedMessage}")
        }
    }

    private fun startBluetoothAcceptThread() {
        if (!checkPermissions(2)) return

        AcceptThread().start()
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

        mHandler = Handler(requireContext().mainLooper, Handler.Callback {
            try {
                val response = it.obj as Pair<String, ByteArray>
                val from = response.first
                val msg = response.second.decodeToString()
                Toast.makeText(requireContext(), "New Message Received", Toast.LENGTH_SHORT).show()

                return@Callback true
            } catch (e: Exception) {
                return@Callback false
            }
        })

        // Start the communication thread
        val communicationService = ConnectedThread(socket, name)
        communicationService.start()


        mHandler.post {
            val text = "feffesf"
            communicationService.write(text.encodeToByteArray())
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

}