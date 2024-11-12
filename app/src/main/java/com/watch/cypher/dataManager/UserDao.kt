package com.watch.cypher.dataManager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.ConversationData
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.UserData

@Dao
interface AppDao {
    // User-related operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserData)

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUserInfo(): UserData?

    // Contact-related operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: ContactData)

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: String): ContactData?

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<ContactData>

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: String)



    // Conversation methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationData)

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationData?

    // Message methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageData)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<MessageData>

    @Delete
    suspend fun deleteConversation(conversation: ConversationData)

    @Delete
    suspend fun deleteMessage(message: MessageData)
}

