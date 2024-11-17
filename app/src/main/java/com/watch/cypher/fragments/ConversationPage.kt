package com.watch.cypher.fragments

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asynctaskcoffee.audiorecorder.uikit.VoiceSenderDialog
import com.asynctaskcoffee.audiorecorder.worker.AudioRecordListener
import com.bumptech.glide.Glide
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.BuildConfig
import com.kroegerama.imgpicker.ButtonType
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.ConversationAdapter
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.MessageType
import com.watch.cypher.databinding.FragmentConversationPageBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.util.UUID

class ConversationPage : Fragment(R.layout.fragment_conversation_page), AudioRecordListener {
    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private lateinit var webSocketClient: WebSocketClient
    private val permissionsRequired = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val permissionCode = 200
    private var permissionToRecordAccepted = false
    private lateinit var binding: FragmentConversationPageBinding
    private var allPosts = mutableListOf<MessageData>()
    private var userID: String? = MainActivity.mainUser.id
    private var convoID: String? = null
    private var contactData : ContactData? = null
    private var convoType : Int? = null
    private var BTaddress : String? = null
    private lateinit var eventSheet : BottomSheetBehavior<CardView>
    val gson = Gson()
    private lateinit var sockets: BluetoothSocket
    private var img : ByteArray? = null

    //bt
    private val TAG: String = "999ZDZDZ9dddd"
    private lateinit var mHandler: Handler
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentConversationPageBinding.inflate(inflater, container, false)
        eventSheet = BottomSheetBehavior.from(binding.addeventsheet)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventSheet.isHideable = true
        eventSheet.state = BottomSheetBehavior.STATE_HIDDEN
        eventSheet.isDraggable = true
        convoType = arguments?.getInt("convoType",0)
        contactData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("contactData", ContactData::class.java)
        } else {
            @Suppress("DEPRECATION") // Suppress the deprecation warning for older API levels
            arguments?.getParcelable("contactData")
        }
        convoID = contactData?.conversationId!!
        Log.i("Bluqzdqzdqetooth", "aa ${contactData?.BTID!!}")

        BTaddress = extractAddressFromUniqueId(contactData?.BTID!!)
        Log.i("Bluqzdqzdqetooth", "ss $BTaddress")
        // Register the media picker result launcher during fragment attachment
        if (convoType == 0){
            connectWebSocket(MainActivity.mainUser.id)


            binding.mainRecyclerview.apply {
                layoutManager = LinearLayoutManager(this.context).apply {
                    stackFromEnd = true // This makes the RecyclerView start at the bottom
                }
                adapter = ConversationAdapter(requireContext(), allPosts).apply {
                    setOnItemClickListener(object : ConversationAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                        }
                    })
                }
            }

            fetchMessages(convoID!!)

            binding.sendButton.setOnClickListener {
                if (binding.commentEditText.text.isNotBlank()){
                    binding.sendButton.isEnabled = false
                    binding.sendButton.speed = 2.5f
                    binding.sendButton.playAnimation()
                    binding.sendButton.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            // Optional: Do something when the animation starts
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            // Reset the animation to the beginning and stop it
                            binding.sendButton.pauseAnimation()  // Stop the animation
                            binding.sendButton.progress = 0f     // Reset to the start
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            // Optional: Handle animation cancellation
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                            // Optional: Handle animation repeat
                        }
                    })
                    val messageJson = JSONObject().apply {
                        put("action", "message")
                        put("msg", binding.commentEditText.text.toString())
                        put("from", userID!!)
                        put("conversationID", convoID)
                    }
                    webSocketClient.send(messageJson.toString())
                    createMessage(convoID!!,userID!!,binding.commentEditText.text.toString(),img,MessageType.TEXT,null)
                }
                else{
                    binding.sendButton.isEnabled = true
                }
            }

            binding.psname.text = convoID
        }
        else{
            fetchMessages(convoID!!)
            binding.psname.text = contactData?.username!!
            binding.boxContainer.visibility = View.GONE
            binding.helpContainer.visibility = View.VISIBLE
            loadImage(contactData?.pfpurl,contactData?.pfp)
            val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter
            val device = bluetoothAdapter!!.getRemoteDevice(BTaddress)
            binding.mainRecyclerview.apply {
                layoutManager = LinearLayoutManager(this.context).apply {
                    stackFromEnd = true // This makes the RecyclerView start at the bottom
                }
                adapter = ConversationAdapter(requireContext(), allPosts).apply {
                    setOnItemClickListener(object : ConversationAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                        }
                    })
                }
            }

            binding.startBtn.setOnClickListener {
                eventSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }

            binding.imgbtn.setOnClickListener {
                checkAndLaunchPhotoPicker()
            }

            binding.connectBtn.setOnClickListener {
                eventSheet.state = BottomSheetBehavior.STATE_HIDDEN
                binding.startBtn.isEnabled = false
                bindProgressButton(binding.startBtn)
                binding.startBtn.attachTextChangeAnimator()
                binding.startBtn.showProgress{
                    progressColor = Color.BLACK
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                binding.helpertext.text = "Attempting to establish connection..."
                ConnectThread(device).start()
            }
            binding.hostBtn.setOnClickListener {
                eventSheet.state = BottomSheetBehavior.STATE_HIDDEN
                binding.startBtn.isEnabled = false
                bindProgressButton(binding.startBtn)
                binding.startBtn.attachTextChangeAnimator()
                binding.startBtn.showProgress{
                    progressColor = Color.BLACK
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                startBluetoothAcceptThread()
                binding.helpertext.text = "Waiting for a device to connect..."
            }

            pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Handle the selected URI
                if (uri != null) {
                    img = compressImage(uri,requireContext())
                    val message = MessageData(
                        conversationId = convoID!!,
                        senderID = userID!!,
                        content = "img",
                        imageUrl = img,
                        type = MessageType.IMAGE,
                        timestamp = System.currentTimeMillis()
                    )
                    createMessage(convoID!!,userID!!,binding.commentEditText.text.toString(),img,MessageType.IMAGE,null)
                    val json = gson.toJson(message)
                    val byteArray = json.toByteArray(Charsets.UTF_8)
                    ConnectedThread(sockets).write(byteArray)
                }
            }

        }

        binding.sendButton.visibility = View.GONE
        binding.recordbtn.visibility = View.VISIBLE

        binding.commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                binding.sendButton.visibility = View.GONE
                binding.recordbtn.visibility = View.VISIBLE
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Called while text is being changed
                if (charSequence.isNullOrEmpty()) {
                    binding.sendButton.visibility = View.GONE
                    binding.recordbtn.visibility = View.VISIBLE
                } else {
                    binding.sendButton.visibility = View.VISIBLE
                    binding.recordbtn.visibility = View.GONE
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                // You can perform any post-text-change logic if needed
            }
        })

        binding.recordbtn.setOnClickListener {
            openDialog()
        }


    }


    fun openDialog() {
        VoiceSenderDialog(this).show(childFragmentManager, "VOICE")
    }

    override fun onAudioReady(audioUri: String?) {
        audioUri?.let {
            try {
                // Step 1: Convert the audio file to a ByteArray
                val byteArrays = File(it).readBytes() // Read the file into a ByteArray
                val message = MessageData(
                    conversationId = convoID!!,
                    senderID = userID!!,
                    content = "img",
                    voiceChat = byteArrays,
                    type = MessageType.VOICE,
                    timestamp = System.currentTimeMillis()
                )
                createMessage(convoID!!,userID!!,binding.commentEditText.text.toString(),img,MessageType.VOICE,byteArrays)
                val json = gson.toJson(message)
                val byteArray = json.toByteArray(Charsets.UTF_8)
                ConnectedThread(sockets).write(byteArray)

            } catch (e: Exception) {
                Log.e(TAG, "Error handling audio file: $audioUri", e)
                Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e(TAG, "Audio URI is null")
            Toast.makeText(context, "No audio file to play", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReadyForRecord() {
    }

    override fun onRecordFailed(errorMessage: String?) {
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

    private fun handleLegacyPhotoPicker() {
        // For older versions, we use the ACTION_OPEN_DOCUMENT intent
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = "image/*" // You can customize this to handle videos or both image/video
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(pickIntent, PERMISSION_REQUEST_CODE)
    }

    // Handle the result for legacy devices that don't support the Photo Picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                Log.d("LegacyPhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("LegacyPhotoPicker", "No media selected")
            }
        }
    }








    fun loadImage(pfpurl: String?, pfp: ByteArray?) {
        if (pfpurl != null) {
            Glide.with(requireContext())
                .load(pfpurl)
                .error(
                    Glide.with(requireContext())
                        .load(pfp?.let { BitmapFactory.decodeByteArray(it, 0, it.size) })
                )
                .into(binding.pfp)
        } else if (pfp != null) {
            // Load from ByteArray as fallback if pfpurl is null
            val bitmap = BitmapFactory.decodeByteArray(pfp, 0, pfp.size)
            binding.pfp.setImageBitmap(bitmap)
        } else {
            // Optionally set a placeholder or default image if both are null
            binding.pfp.setImageResource(R.drawable.pfp)
        }
    }








    private fun connectWebSocket(usrd:String) {
        Log.d("MessagesqdZDZDZDZzqdq", "CALLED")

        val uri = URI("ws://192.168.1.65:3000")
        //val uri = URI("ws://10.0.2.2:3000")

        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                // Send client ID to server upon connection
                webSocketClient.send("""{"action": "connect", "id": "$usrd"}""")
            }

            override fun onMessage(message: String?) {
                activity?.runOnUiThread {
                    message?.let {
                        val received = JSONObject(it)
                        val msg = received.optString("msg")
                        val from = received.optString("from")
                        if (from != userID)
                            createMessage(convoID!!,from,msg,img,MessageType.TEXT,null)
                    }
                }
                Log.d("WebSocketClient", "Message received: $message")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocketClient", "Disconnected: $reason")
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocketClient", "Error: ${ex?.message}")
            }
        }

        webSocketClient.connect()
    }

    private fun fetchMessages(conversationId: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()

        lifecycleScope.launch {
            // Fetch the messages for the specified conversation ID
            val messages: List<MessageData> = appDao.getMessagesForConversation(conversationId)

            allPosts.addAll(messages)
            // Log the messages or update the UI
            messages.forEach { message ->
                Log.d("Messagesqdzqdq", "Message ID: ${message.messageId}, Content: ${message.content}, Timestamp: ${message.timestamp}")
            }
            binding.mainRecyclerview.adapter?.let { adapter ->
                if (adapter is ConversationAdapter) {
                    adapter.updateMessages(messages) // Update the adapter with the new contacts
                }
            }
        }

    }

    private fun updateMessagesList(conversationId: String) {
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()

        lifecycleScope.launch {
            // Fetch the updated messages
            val messages: List<MessageData> = appDao.getMessagesForConversation(conversationId)

            // Update the adapter with the new messages
            binding.mainRecyclerview.adapter?.let { adapter ->
                if (adapter is ConversationAdapter) {
                    adapter.updateMessages(messages)
                }
            }

            // Scroll to the bottom after the list is updated
            binding.mainRecyclerview.post {
                binding.mainRecyclerview.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun createMessage(id:String,senderID:String,message:String,imgSet:ByteArray?,TYPE:MessageType,audio:ByteArray?){
        val message = MessageData(
            conversationId = convoID!!,
            senderID = senderID,
            content = message,
            imageUrl = imgSet,
            voiceChat = audio,
            type = TYPE,
            timestamp = System.currentTimeMillis()
        )

        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()
        lifecycleScope.launch {
            appDao.insertMessage(message)
            updateMessagesList(id)
        }
        binding.commentEditText.text.clear()
        binding.sendButton.isEnabled = true
    }


    //bt

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
                    activity?.runOnUiThread {
                        binding.boxContainer.visibility = View.VISIBLE
                        binding.helpContainer.visibility = View.GONE
                    }
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
                    activity?.runOnUiThread {
                        binding.boxContainer.visibility = View.VISIBLE
                        binding.helpContainer.visibility = View.GONE

                    }
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
        private val mmBuffer: ByteArray = ByteArray(1024) // Buffer for reading data

        override fun run() {
            try {
                while (true) {
                    // Step 1: Read the 4-byte header
                    val header = ByteArray(4)
                    mmInStream.readFully(header, 0, 4) // Read exactly 4 bytes
                    val messageLength = ByteBuffer.wrap(header).int

                    Log.i(TAG, "Received header. Message length: $messageLength bytes")

                    // Step 2: Read the full message based on the length
                    val data = ByteArray(messageLength)
                    mmInStream.readFully(data, 0, messageLength) // Read the entire message

                    Log.i(TAG, "Received complete message of size $messageLength bytes")

                    // Send the obtained bytes to the UI activity
                    val readMsg = mHandler.obtainMessage(
                        MESSAGE_READ, messageLength, -1,
                        "opName" to data
                    )
                    readMsg.sendToTarget()
                }
            } catch (e: IOException) {
                Log.d(TAG, "Input stream was disconnected", e)
                activity?.runOnUiThread {
                    Log.e(TAG, "Connection error or disconnect detected")
                    binding.startBtn.isEnabled = true
                    binding.startBtn.hideProgress("Start Connection")
                    binding.boxContainer.visibility = View.GONE
                    binding.helpContainer.visibility = View.VISIBLE
                    binding.helpertext.text = "One user should host, while the other connects to join."
                    binding.startBtn.isEnabled = true
                    Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                // Add a 4-byte header with the length of the data
                val header = ByteBuffer.allocate(4).putInt(bytes.size).array()

                Log.i(TAG, "Sending data with header. Total size: ${bytes.size} bytes")

                // Write the header followed by the data
                mmOutStream.write(header)
                mmOutStream.write(bytes)
                mmOutStream.flush()

                Log.i(TAG, "Data sent successfully with header.")
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity
                val writeErrorMsg = mHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mHandler.sendMessage(writeErrorMsg)
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                Log.i(TAG, "Closing the connection socket.")
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    fun InputStream.readFully(buffer: ByteArray, offset: Int, length: Int) {
        var bytesRead = 0
        while (bytesRead < length) {
            val read = this.read(buffer, offset + bytesRead, length - bytesRead)
            if (read == -1) throw IOException("End of stream reached before reading fully")
            bytesRead += read
        }
    }



    @SuppressLint("MissingPermission")
    private fun manageServerSocketConnection(socket: BluetoothSocket, name: String) {
        // Generate the shared UUID when a connection is established

        mHandler = Handler(requireContext().mainLooper, Handler.Callback {
            Log.i(TAG, "NEW MESSAGE")
            try {
                val response = it.obj as Pair<String, ByteArray>
                val receivedData = response.second

                // Convert ByteArray to JSON string and then to UserData
                val receivedJson = receivedData.toString(Charsets.UTF_8)
                val receivedUserData = gson.fromJson(receivedJson, MessageData::class.java)
                createMessage(convoID!!, receivedUserData.senderID, receivedUserData.content!!,receivedUserData.imageUrl,receivedUserData.type,receivedUserData.voiceChat)

                return@Callback true
            } catch (e: Exception) {
                Log.e(TAG, "exeeeeeeeeeeeeeeeption $e")
                return@Callback false
            }
        })

        // Start the communication thread
        val communicationService = ConnectedThread(socket)
        sockets = socket
        communicationService.start()


        mHandler.post {
            binding.apply {
                sendButton.setOnClickListener {
                    val text = binding.commentEditText.text.toString()
                    if (binding.commentEditText.text.isNotBlank()){
                        binding.sendButton.isEnabled = false
                        binding.sendButton.speed = 2.5f
                        binding.sendButton.playAnimation()
                        binding.sendButton.addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                // Optional: Do something when the animation starts
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                // Reset the animation to the beginning and stop it
                                binding.sendButton.pauseAnimation()  // Stop the animation
                                binding.sendButton.progress = 0f     // Reset to the start
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                // Optional: Handle animation cancellation
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                // Optional: Handle animation repeat
                            }
                        })
                        val message = MessageData(
                            conversationId = convoID!!,
                            senderID = userID!!,
                            content = text,
                            type = MessageType.TEXT,
                            timestamp = System.currentTimeMillis()
                        )
                        createMessage(convoID!!,userID!!,binding.commentEditText.text.toString(),img,MessageType.TEXT,null)
                        val json = gson.toJson(message)
                        val byteArray = json.toByteArray(Charsets.UTF_8)
                        communicationService.write(byteArray)
                    }
                    else{
                        binding.sendButton.isEnabled = true
                    }
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

    fun extractAddressFromUniqueId(uniqueId: String): String {
        val decodedId = String(Base64.decode(uniqueId, Base64.NO_WRAP)) // Decode Base64
        return decodedId.substringBefore("-") // Get the address part
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAndLaunchPhotoPicker()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
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

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


}
