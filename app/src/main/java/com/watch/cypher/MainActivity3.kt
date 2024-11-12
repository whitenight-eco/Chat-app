package com.watch.cypher

import android.os.Bundle
import android.util.Log
import com.watch.cypher.databinding.ActivityMain3Binding
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.UUID


class MainActivity3 : AppCompatActivity() {
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var binding: ActivityMain3Binding
    private val clientId = UUID.randomUUID().toString() // Generate a unique client ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        connectWebSocket()

        binding.joinButton.setOnClickListener {
            val roomName = binding.roomInput.text.toString()
            if (roomName.isNotEmpty()) {
                webSocketClient.send("""{"action": "join", "room": "$roomName"}""")
                binding.messageDisplay.append("Joined room: $roomName\n")
            }
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            val roomName = binding.roomInput.text.toString()
            if (message.isNotEmpty() && roomName.isNotEmpty()) {
                webSocketClient.send("""{"action": "message", "room": "$roomName", "msg": "$message"}""")
                binding.messageInput.text.clear()
                Log.d("WebSocketClient", "Message sent: $message")
            }
        }

        // New quit button functionality for quitting a specific room
        binding.quitButton.setOnClickListener {
            val roomName = binding.roomInput.text.toString() // Use the same input for room name
            if (roomName.isNotEmpty()) {
                webSocketClient.send("""{"action": "quit", "room": "$roomName"}""")
                binding.messageDisplay.append("Left room: $roomName\n")
            }
        }
    }

    private fun connectWebSocket() {
        val uri = URI("wss://cipherflare.tech")

        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("WebSocketClient", "Connected to server")
                // Send client ID to server upon connection
                webSocketClient.send("""{"action": "connect", "id": "$clientId"}""")
            }

            override fun onMessage(message: String?) {
                runOnUiThread {
                    message?.let {
                        val received = JSONObject(it)
                        val msg = received.getString("msg")
                        binding.messageDisplay.append("$msg\n")
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

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close()
    }
}
