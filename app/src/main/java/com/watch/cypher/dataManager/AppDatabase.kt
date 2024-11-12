package com.watch.cypher.dataManager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.ConversationData
import com.watch.cypher.dataModel.Converters
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.dataModel.UserData

@Database(entities = [UserData::class, ContactData::class, ConversationData::class, MessageData::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}



