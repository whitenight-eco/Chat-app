package com.watch.cypher.dataModel

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPollData(poll: PollData?): String? {
        return gson.toJson(poll)
    }

    @TypeConverter
    fun toPollData(data: String?): PollData? {
        return gson.fromJson(data, object : TypeToken<PollData?>() {}.type)
    }

    @TypeConverter
    fun fromList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromEventData(event: EventData?): String? {
        return gson.toJson(event)
    }

    @TypeConverter
    fun toEventData(data: String?): EventData? {
        return gson.fromJson(data, object : TypeToken<EventData?>() {}.type)
    }

}