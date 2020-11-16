package com.crushtech.mycollegecgpa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crushtech.mycollegecgpa.data.local.entities.*

@Database(
    entities = [Semester::class,
        Courses::class,
        LocallyDeletedSemesterId::class,
        LocallyDeletedCourseId::class,
        GradeClass::class],
    version = 12, exportSchema = false
)
@TypeConverters(
    Converters::class,
    OwnersConverters::class,
    GradePointConverters::class
)
abstract class SemesterDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao

}