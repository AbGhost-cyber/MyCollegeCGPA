package com.crushtech.myccgpa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crushtech.myccgpa.data.local.converters.*
import com.crushtech.myccgpa.data.local.entities.*

@Database(
    entities = [Semester::class,
        Courses::class,
        LocallyDeletedSemesterId::class,
        LocallyDeletedCourseId::class,
        LocallyDeletedSemesterRequestId::class,
        GradeClass::class,
        SemesterRequests::class],
    version = 19, exportSchema = false
)
@TypeConverters(
    CourseConverters::class,
    OwnersConverters::class,
    GradePointConverters::class,
    SemesterRequestsConverters::class,
    StateConverters::class
)
abstract class SemesterDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao

}