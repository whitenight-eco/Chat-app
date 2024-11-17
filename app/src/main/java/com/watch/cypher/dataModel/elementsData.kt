package com.watch.cypher.dataModel

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "user")
data class UserData(
    @PrimaryKey val id: String,
    val username: String,
    var BTID: String? = null,
    val pfpurl: String? = null,
    val pfp: ByteArray? = null // Using ByteArray for profile picture
) {
    // Override equals() to compare byte arrays based on content
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserData
        return pfp?.contentEquals(other.pfp) == true
    }

    // Override hashCode() based on content of the byte array
    override fun hashCode(): Int {
        return pfp?.contentHashCode() ?: 0
    }
}
@Parcelize
@Entity(tableName = "contacts")
data class ContactData(
    @PrimaryKey val id: String,
    val username: String,
    var BTID: String? = null,
    val pfpurl: String? = null,
    val pfp: ByteArray? = null,
    val conversationId: String,
    val conversationType: ConvoType
) : Parcelable {
    // Override equals() to compare byte arrays based on content
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserData
        return pfp?.contentEquals(other.pfp) == true
    }

    // Override hashCode() based on content of the byte array
    override fun hashCode(): Int {
        return pfp?.contentHashCode() ?: 0
    }
}

@Entity(tableName = "conversations")
data class ConversationData(
    @PrimaryKey val conversationId: String,
    val conversationType: ConvoType
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
    val imageUrl: ByteArray? = null,
    val voiceChat: ByteArray? = null,
    val poll: PollData? = null,
    val event: EventData? = null,
    val type: MessageType,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageData

        if (messageId != other.messageId) return false
        if (conversationId != other.conversationId) return false
        if (senderID != other.senderID) return false
        if (content != other.content) return false
        if (imageUrl != null) {
            if (other.imageUrl == null) return false
            if (!imageUrl.contentEquals(other.imageUrl)) return false
        } else if (other.imageUrl != null) return false
        if (poll != other.poll) return false
        if (event != other.event) return false
        if (type != other.type) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageId.hashCode()
        result = 31 * result + conversationId.hashCode()
        result = 31 * result + senderID.hashCode()
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.contentHashCode() ?: 0)
        result = 31 * result + (poll?.hashCode() ?: 0)
        result = 31 * result + (event?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

enum class MessageType {
    TEXT, IMAGE, POLL, EVENT, VOICE
}

enum class ConvoType {
    BLUETOOTH, ONLINE
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

