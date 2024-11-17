package com.watch.cypher.fragments

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
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
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Base64
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.watch.cypher.MainActivity.Companion.mainUser
import com.watch.cypher.PortraitCaptureActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.BtDevicesAdapter
import com.watch.cypher.adapters.ContactsAdapter
import com.watch.cypher.dataManager.AppDao
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.UserData
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.ConversationData
import com.watch.cypher.dataModel.ConvoType
import com.watch.cypher.dataModel.btData
import com.watch.cypher.databinding.FragmentContactsPageBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID



class ContactsPage : Fragment(R.layout.fragment_contacts_page) {
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var eventSheet : BottomSheetBehavior<CardView>
    private val discoveredDevices = mutableListOf<btData>() // Store discovered devices
    private lateinit var binding: FragmentContactsPageBinding
    private var allPosts = mutableListOf<ContactData>()
    private val TAG: String = "999ZDZDZ9W"
    private lateinit var mHandler: Handler
    var screenHeight = 0
    private val mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
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

        screenHeight = Resources.getSystem().displayMetrics.heightPixels
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
                            if (allPosts[position].conversationType.toString() == "ONLINE"){
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
                            else{
                                val bundle = Bundle()
                                bundle.putInt("convoType", 1)
                                bundle.putParcelable("contactData", allPosts[position])
                                Log.d("checkfs", "un: ${allPosts[position].conversationId}")
                                Log.d("checkfs", "uno: ${allPosts[position].BTID!!}")
                                Log.d("checkfs", "unod: ${extractAddressFromUniqueId(allPosts[position].BTID!!)}")

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
            val p1 = "Scan bluetooth devices or Host a Connection"
            val spannableString1 = SpannableString(p1)

            val start1 = p1.indexOf("Host a Connection")
            val end1 = start1 + "Host a Connection".length
            spannableString1.setSpan(UnderlineSpan(), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString1.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    stopBluetoothScan()
                    binding.lottieView.isEnabled = true
                    binding.lottieView.cancelAnimation()
                    binding.lottieView.progress = 0f // Reset the animation to the beginning
                    binding.btscanLayout.visibility = View.GONE
                    binding.bthostLayout.visibility = View.VISIBLE
                    binding.scantext.visibility = View.VISIBLE
                    binding.lottieView.visibility = View.VISIBLE
                    binding.swiperbt.visibility = View.GONE
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(binding.helptext.context, R.color.blue)
                    ds.isUnderlineText = true
                    ds.isFakeBoldText = true
                }
            }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.helptext.text = spannableString1
            binding.helptext.movementMethod = LinkMovementMethod.getInstance()
            val p2 = "Host a Connection or Scan bluetooth devices"
            val spannableString2 = SpannableString(p2)

            val start2 = p2.indexOf("Scan bluetooth devices")
            val end2 = start2 + "Scan bluetooth devices".length
            spannableString2.setSpan(UnderlineSpan(), start1, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString2.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    AcceptThread().cancel()
                    binding.lottieView2.isEnabled = true
                    binding.lottieView2.cancelAnimation()
                    binding.lottieView2.progress = 0f // Reset the animation to the beginning
                    binding.hosttext.visibility = View.VISIBLE
                    binding.btscanLayout.visibility = View.VISIBLE
                    binding.bthostLayout.visibility = View.GONE
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(binding.helptext2.context, R.color.blue)
                    ds.isUnderlineText = true
                    ds.isFakeBoldText = true
                }
            }, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.helptext2.text = spannableString2
            binding.helptext2.movementMethod = LinkMovementMethod.getInstance()
            binding.mainRecyclerviewbt.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = BtDevicesAdapter(requireActivity(), discoveredDevices).apply {
                    setOnItemClickListener(object : BtDevicesAdapter.onItemClickListener {
                        @SuppressLint("MissingPermission")
                        override fun onItemClick(position: Int) {

                            checkAndPairDevice(discoveredDevices[position].device)

                            /*val addressId = discoveredDevices[position].addres
                            val genId = UUID.randomUUID().toString().replace("-", "").take(14)

                            val conversation = ConversationData(addressId, ConvoType.BLUETOOTH)
                            lifecycleScope.launch {
                                //appDao.insertConversation(conversation)
                                addContact(appDao,genId, addressId, ConvoType.BLUETOOTH)
                            }
                            val animator = ValueAnimator.ofInt(screenHeight, 0)
                            animator.addUpdateListener { valueAnimator ->
                                // Update the peek height of the first bottom sheet during the animation.
                                val height = valueAnimator.animatedValue as Int
                                eventSheet.peekHeight = height
                            }
                            animator.duration = 300  // Set the duration of the animation.
                            animator.start()  // Start the animation.
                            */
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
                binding.lottieView.isEnabled = false
                binding.lottieView.playAnimation()
                binding.scantext.visibility = View.INVISIBLE
                binding.swiperbt.setOnRefreshListener {
                    startBluetoothScan()
                }
                startBluetoothScan()

            }

            binding.lottieView2.setOnClickListener {
                binding.lottieView2.isEnabled = false
                binding.lottieView2.playAnimation()
                startBluetoothAcceptThread()
                binding.hosttext.visibility = View.INVISIBLE
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
            appDao.insertUser(UserData(id = userId,"usermamas"))
            Log.d("checkfs", "New user added with ID: $userId")
        } else {
            Log.d("checkfs", "User already exists with ID: ${existingUser.id}")
        }
    }

    private suspend fun addContact(
        appDao: AppDao,
        contactId: String,
        convoID: String,
        convoType: ConvoType
    ) {
        // Check if contact already exists
        val existingContact = if(convoType == ConvoType.ONLINE){appDao.getContactById(contactId)}else{appDao.getContactByConvoID(convoID)}
        Log.d("checkfs", "Contact already exists with ID sssssssssss: ${existingContact}")
        Log.d("checkfs", "Contact already exists with ID sssssssssssaaaaa: ${convoID}")

        if (existingContact == null) {
            // Contact does not exist, insert new contact
            //appDao.insertContact(ContactData(id = contactId, conversationId = convoID, conversationType = convoType))
            updateContactsList()
            Log.d("checkfs", "New contact added with ID: $convoID")
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
        val conversation = ConversationData(conversationId = documentId,ConvoType.ONLINE)

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
                                    addContact(appDao, contactID, document.id, ConvoType.BLUETOOTH)
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
                                addContact(appDao, contactID, document.id, ConvoType.BLUETOOTH)
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
            binding.mainRecyclerviewbt.adapter?.notifyDataSetChanged()
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
            pairDevice(device)
        } else {
            ConnectThread(device).start()
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

    private val bondStateReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Log.d("Bluetooth", "Pairing completed with ${device?.name}")
                        Toast.makeText(context, "Pairing completed", Toast.LENGTH_SHORT).show()
                        if (device != null)
                            ConnectThread(device).start()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Log.d("Bluetooth", "Pairing in progress with ${device?.name}")
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Log.d("Bluetooth", "Pairing failed or removed for ${device?.name}")
                        Toast.makeText(context, "Pairing failed or removed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Call this method to initiate pairing and register the receiver
    @SuppressLint("MissingPermission")
    private fun pairDevice(device: BluetoothDevice) {
        try {
            // Initiate pairing (bonding)
            val method = device.javaClass.getMethod("createBond")
            method.invoke(device)
            Log.d("Bluetooth", "Pairing initiated with ${device.name}")

            // Register the BroadcastReceiver to listen for pairing state changes
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            requireContext().registerReceiver(bondStateReceiver, filter)
        } catch (e: Exception) {
            Log.e("Bluetooth", "Pairing failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(receiver)
        requireActivity().unregisterReceiver(bondStateReceiver)
        stopBluetoothScan() // Ensure scanning is stopped when fragment is destroyed.
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
                    startBluetoothAcceptThread()
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

    @SuppressLint("MissingPermission")
    private fun startBluetoothAcceptThread() {
        if (!checkPermissions(2)) return

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Log.d(TAG, "Making device discoverable...")
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 300 seconds discoverable
            }
            discoverableResultLauncher.launch(discoverableIntent)
            return // Wait for the user response
        }

        // Start the thread directly if already discoverable
        AcceptThread().start()
    }


    private val discoverableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Discoverability request denied")
            // Handle denial case
        } else {
            Log.d(TAG, "Device is now discoverable for ${result.resultCode} seconds")
            // Proceed to start AcceptThread
            AcceptThread().start()
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
                }
                catch (e: IOException) {
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
                val client = socket.remoteDevice
                manageServerSocketConnection(socket)

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

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) :
        Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        @SuppressLint("MissingPermission")
        override fun run() {
            var numBytes: Int // Number of bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {

                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer) // Read bytes into buffer
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    //AcceptThread().start()
                    break
                }

                // Ensure we only send the actual bytes read, not the entire buffer
                val actualData = mmBuffer.copyOf(numBytes)

                // Send the obtained bytes to the UI activity.
                val readMsg = mHandler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmSocket.remoteDevice.name to actualData
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
                    // Manage the connection in a separate thread
                    try {
                        manageServerSocketConnection(bluetoothSocket)
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
    private fun manageServerSocketConnection(socket: BluetoothSocket) {
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()
        val gson = Gson()

        // Generate the shared UUID when a connection is established
        Log.i(TAG, "called msg gfg")

        mHandler = Handler(requireContext().mainLooper, Handler.Callback {
            try {
                val response = it.obj as Pair<String, ByteArray>
                val senderName = response.first
                val receivedData = response.second

                // Convert ByteArray to JSON string and then to UserData
                val receivedJson = receivedData.toString(Charsets.UTF_8)
                val receivedUserData = gson.fromJson(receivedJson, UserData::class.java)
                Log.d(TAG, "New Message Received $receivedUserData")
                Log.d(TAG, "my adrs ${receivedUserData.BTID}, user ards: ${socket.remoteDevice.address}")
                lifecycleScope.launch {
                    val existingContact = appDao.getContactById("BT${receivedUserData.id}")
                    if (existingContact == null) {
                        val convoid = UUID.randomUUID().toString().replace("-", "").take(14)
                        val btid = generateUniqueIdFromAddress(socket.remoteDevice.address)
                        val newContact = ContactData(
                            "BT${receivedUserData.id}",
                            receivedUserData.username,
                            btid,
                            receivedUserData.pfpurl,
                            receivedUserData.pfp,
                            convoid,
                            ConvoType.BLUETOOTH
                        )
                        addBtContact(appDao,receivedUserData.BTID!!,newContact)
                        val conversation = ConversationData(convoid, ConvoType.BLUETOOTH)
                        appDao.insertConversation(conversation)
                        updateContactsList()
                        val animator = ValueAnimator.ofInt(screenHeight, 0)
                        animator.addUpdateListener { valueAnimator ->
                            // Update the peek height of the first bottom sheet during the animation.
                            val height = valueAnimator.animatedValue as Int
                            eventSheet.peekHeight = height
                        }
                        animator.duration = 300  // Set the duration of the animation.
                        animator.start()
                        Toast.makeText(requireContext(), "Contact added successfully", Toast.LENGTH_SHORT).show()

                    }else{
                        Toast.makeText(requireContext(), "Contact already exists", Toast.LENGTH_SHORT).show()
                    }
                }




                return@Callback true
            } catch (e: Exception) {
                return@Callback false
            }
        })



        // Start the communication thread
        val communicationService = ConnectedThread(socket)
        communicationService.start()

        val dt = mainUser
        dt.BTID = socket.remoteDevice.address
        val json = gson.toJson(dt)
        val byteArray = json.toByteArray(Charsets.UTF_8)
        communicationService.write(byteArray)
    }

    companion object {
        const val REQUEST_ENABLE_BT = 100

        // Defines several constants used when transmitting messages between the
        // service and the UI.
        const val MESSAGE_READ: Int = 0
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_TOAST: Int = 2
    }

    private fun addBtContact(appDao: AppDao, address: String, newContact: ContactData){
        if(mainUser.BTID == null){
            lifecycleScope.launch {
                appDao.updateBTID(mainUser.id,address)
                mainUser.BTID = address
                appDao.insertContact(newContact)
            }
        }else{
            lifecycleScope.launch {
                appDao.insertContact(newContact)
            }
        }
    }

    fun generateUniqueIdFromAddress(address: String): String {
        val randomValue = SecureRandom().nextInt(10000) // Append random value to make it unique
        val rawId = "$address-$randomValue"
        return Base64.encodeToString(rawId.toByteArray(), Base64.NO_WRAP) // Encode to Base64
    }

    fun extractAddressFromUniqueId(uniqueId: String): String {
        val decodedId = String(Base64.decode(uniqueId, Base64.NO_WRAP)) // Decode Base64
        return decodedId.substringBefore("-") // Get the address part
    }

}