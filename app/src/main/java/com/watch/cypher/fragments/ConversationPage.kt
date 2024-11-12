package com.watch.cypher.fragments

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.watch.cypher.MainActivity
import com.watch.cypher.R
import com.watch.cypher.adapters.ConversationAdapter
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.MessageType
import com.watch.cypher.databinding.FragmentConversationPageBinding
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.UUID

class ConversationPage : Fragment(R.layout.fragment_conversation_page) {

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
    private var convoID : String? = null
    private var convoType : Int? = null


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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        convoType = arguments?.getInt("convoType",0)
        convoID = arguments?.getString("convoID").toString()

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
                    createMessage(convoID!!,userID!!,binding.commentEditText.text.toString())
                }
                else{
                    binding.sendButton.isEnabled = true
                }
            }

            binding.psname.text = convoID
        }else{
            val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter
            val device = bluetoothAdapter!!.getRemoteDevice(convoID)
            val connectionSocket = ConnectThread(device)
            connectionSocket.start()
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
                            createMessage(convoID!!,from,msg)
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
            val messages: List<MessageData> = appDao.getMessagesForConversation(conversationId)
            binding.mainRecyclerview.adapter?.let { adapter ->
                if (adapter is ConversationAdapter) {
                    adapter.updateMessages(messages)
                }
            }
        }
    }

    private fun createMessage(id:String,senderID:String,message:String){
        val message = MessageData(
            conversationId = convoID!!,
            senderID = senderID,
            content = message,
            type = MessageType.TEXT,
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
