package com.crushtech.myccga.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crushtech.myccga.data.local.entities.*

@Database(
    entities = [Semester::class,
        Courses::class,
        LocallyDeletedSemesterId::class,
        LocallyDeletedCourseId::class,
        GradeClass::class],
    version = 15, exportSchema = false
)
@TypeConverters(
    Converters::class,
    OwnersConverters::class,
    GradePointConverters::class
)
abstract class SemesterDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao

}