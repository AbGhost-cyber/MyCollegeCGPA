package com.crushtech.myccgpa.data.local.converters

import androidx.room.TypeConverter
import com.crushtech.myccgpa.data.local.entities.Courses
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CourseConverters {
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