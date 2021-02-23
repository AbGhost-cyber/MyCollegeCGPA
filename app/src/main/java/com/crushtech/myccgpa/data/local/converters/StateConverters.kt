package com.crushtech.myccgpa.data.local.converters

import androidx.room.TypeConverter
import com.crushtech.myccgpa.data.local.entities.STATE

class StateConverters {
    @TypeConverter
    fun fromState(state: STATE): String {
        return state.name
    }

    @TypeConverter
    fun toState(state: String): STATE {
        return STATE.valueOf(state)
    }
}