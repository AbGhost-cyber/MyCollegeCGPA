package com.crushtech.mycollegecgpa.data.local

import androidx.room.TypeConverter
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromCourseList(list: List<Courses>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Courses>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toCourseList(string: String): List<Courses> {
        return Gson().fromJson(string, object : TypeToken<List<Courses>>() {
        }.type)
    }

}