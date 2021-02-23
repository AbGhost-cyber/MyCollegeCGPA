package com.crushtech.myccgpa.data.local.converters

import androidx.room.TypeConverter
import com.crushtech.myccgpa.data.local.entities.GradeClass
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GradePointConverters {
    @TypeConverter
    fun fromGradePointsList(list: List<GradeClass>): String {
        val gson = Gson()
        val type = object : TypeToken<List<GradeClass>>() {}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun toGradePointsList(string: String): List<GradeClass> {
        return Gson().fromJson(string, object : TypeToken<List<GradeClass>>() {
        }.type)
    }

}