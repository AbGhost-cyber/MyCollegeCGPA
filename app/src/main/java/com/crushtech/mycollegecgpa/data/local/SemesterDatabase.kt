package com.crushtech.mycollegecgpa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.LocallyDeletedCourseId
import com.crushtech.mycollegecgpa.data.local.entities.LocallyDeletedSemesterId
import com.crushtech.mycollegecgpa.data.local.entities.Semester

@Database(
    entities = [Semester::class,
        Courses::class,
        LocallyDeletedSemesterId::class,
        LocallyDeletedCourseId::class],
    version = 9, exportSchema = false
)
@TypeConverters(Converters::class, OwnersConverters::class)
abstract class SemesterDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao
}