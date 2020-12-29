package com.crushtech.mycollegecgpa.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.crushtech.mycollegecgpa.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester)

    @Insert
    suspend fun insertCourse(course: Courses)

    @Update
    suspend fun updateCourse(course: Courses)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(gradePoints: GradeClass)

    @Query("SELECT * FROM Semester WHERE id = :semesterId ORDER BY id DESC ")
    suspend fun getSemesterById(semesterId: String): Semester?

    @Query("SELECT * FROM COURSES WHERE semesterId = :semesterId ORDER BY id DESC")
    suspend fun getCourseList(semesterId: String): List<Courses>?

    @Query("DELETE FROM semester WHERE id = :semesterId")
    suspend fun deleteSemesterById(semesterId: String)

    @Query("DELETE FROM semester WHERE isSynced = 1")
    suspend fun deleteAllSyncedSemesters()

    @Query("SELECT * FROM semester WHERE id = :semesterId")
    fun observeSemesterById(semesterId: String): LiveData<Semester>

    @Query("SELECT * FROM grades")
    fun getCurrentGradePoints(): Flow<GradeClass>

    @Query("SELECT * FROM semester ORDER BY id DESC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semester WHERE isSynced = 0")
    suspend fun getAllUnSyncedSemesters(): List<Semester>

    @Query("DELETE FROM semester")
    suspend fun deleteAllSemesters()

    @Query("DELETE FROM grades")
    suspend fun deleteGradePoints()

    @Query("DELETE FROM courses WHERE id = :courseId AND semesterId = :semesterId")
    suspend fun deleteCourseById(courseId: String, semesterId: String)

    @Query("SELECT * FROM locally_deleted_semester_ids")
    suspend fun getAllLocallyDeletedSemesterIds(): List<LocallyDeletedSemesterId>

    @Query("DELETE FROM locally_deleted_semester_ids WHERE deletedSemesterId = :deletedSemesterId")
    suspend fun deleteLocallySemesterId(deletedSemesterId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocallyDeletedSemesterId(locallyDeletedSemesterId: LocallyDeletedSemesterId)

    @Query("SELECT * FROM locally_deleted_course_ids")
    suspend fun getAllLocallyDeletedCourseIds(): List<LocallyDeletedCourseId>

    @Query("DELETE FROM locally_deleted_course_ids WHERE deletedCourseId = :deletedCourseId")
    suspend fun deleteLocallyCourseId(deletedCourseId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocallyDeletedCourseId(locallyDeletedCourseId: LocallyDeletedCourseId)

    @Query("SELECT * FROM grades")
    suspend fun getGradePointForUser(): GradeClass?

}
