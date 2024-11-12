package com.watch.cypher.dataModel

import android.bluetooth.BluetoothDevice
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserData(
    @PrimaryKey val id: String,
)

@Entity(tableName = "contacts")
data class ContactData(
    @PrimaryKey val id: String,
    val conversationId: String,
    )

@Entity(tableName = "conversations")
data class ConversationData(
    @PrimaryKey val conversationId: String
    // Additional fields like "title" or "participants" can be added here
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationData::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE // Optional: cascade delete messages if conversation is deleted
        )
    ],
    indices = [Index("conversationId")] // Index for faster querying by conversationId
)


data class MessageData(
    @PrimaryKey(autoGenerate = true) val messageId: Long = 0,
    val conversationId: String,
    val senderID: String,
    val content: String?,
    val imageUrl: String? = null,
    val poll: PollData? = null,
    val event: EventData? = null,
    val type: MessageType,
    val timestamp: Long
)

enum class MessageType {
    TEXT, IMAGE, POLL, EVENT
}

data class PollData(
    val title: String,
    val options: List<String>,
    val votes: Map<String, Int>
)

data class EventData(
    val title: String,
    val time: String,
    val going: Int,
    val notGoing: Int
)

data class btData(
    val name: String,
    val addres: String,
    val device: BluetoothDevice
)

