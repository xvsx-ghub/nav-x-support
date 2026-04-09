package com.wiswm.nav.support.data.local.dataBase

import androidx.room.TypeConverter
import com.wiswm.nav.support.data.local.dataBase.entity.Weighbridge
import com.wiswm.nav.support.data.local.dataBase.entity.Weighing
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())
    private val listSerializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromHashMap(map: HashMap<String, String>): String {
        return json.encodeToString(mapSerializer, map)
    }

    @TypeConverter
    fun toHashMap(jsonString: String): HashMap<String, String> {
        return json.decodeFromString(mapSerializer, jsonString) as HashMap<String, String>
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return json.encodeToString(listSerializer, list)
    }

    @TypeConverter
    fun toStringList(jsonString: String): List<String> {
        return json.decodeFromString(listSerializer, jsonString)
    }

    @TypeConverter
    fun fromWeighbridgeList(list: List<Weighbridge>): String {
        val serializer = ListSerializer(Weighbridge.serializer())
        return json.encodeToString(serializer, list)
    }

    @TypeConverter
    fun toWeighbridgeList(jsonString: String): List<Weighbridge> {
        val serializer = ListSerializer(Weighbridge.serializer())
        return json.decodeFromString(serializer, jsonString)
    }

    @TypeConverter
    fun fromWeighing(weighing: Weighing?): String {
        if (weighing == null) return ""
        val serializer = Weighing.serializer()
        return json.encodeToString(serializer, weighing)
    }

    @TypeConverter
    fun toWeighing(jsonString: String): Weighing? {
        if (jsonString.isBlank()) return null
        val serializer = Weighing.serializer()
        return json.decodeFromString(serializer, jsonString)
    }
}