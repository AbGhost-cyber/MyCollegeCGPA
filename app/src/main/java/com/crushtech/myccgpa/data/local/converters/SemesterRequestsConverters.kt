package com.crushtech.myccgpa.data.local.converters

import androidx.room.TypeConverter
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SemesterRequestsConverters {
    @TypeConverter
    fun fromSemesterRequestList(list: List<SemesterRequests>): String {
        val gson = Gson()
        val type = object : TypeToken<List<SemesterRequests>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toSemesterRequestList(string: String): List<SemesterRequests> {
        return Gson().fromJson(string, object : TypeToken<List<SemesterRequests>>() {
        }.type)
    }
}