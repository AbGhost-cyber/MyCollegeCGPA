package com.crushtech.myccgpa.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crushtech.myccgpa.data.local.converters.LocallyDeletedSemesterRequestId
import com.crushtech.myccgpa.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: Courses)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(gradePoints: GradeClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesterRequest(semesterRequests: SemesterRequests)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesterRequestList(semesterRequests: List<SemesterRequests>)

    @Query("SELECT * FROM Semester WHERE id = :semesterId ORDER BY id DESC ")
    suspend fun getSemesterById(semesterId: String): Semester?

    @Query("SELECT * FROM COURSES WHERE semesterId = :semesterId ORDER BY id DESC")
    suspend fun getCourseList(semesterId: String): List<Courses>?

    @Query("DELETE FROM semester WHERE id = :semesterId")
    suspend fun deleteSemesterById(semesterId: String)

    @Query("DELETE FROM semester WHERE isSynced = 1")
    suspend fun deleteAllSyncedSemesters()

    @Query("DELETE FROM semester_requests WHERE id = :semReqId")
    suspend fun deleteSemReqById(semReqId: String)

    @Query("DELETE FROM courses WHERE semesterId  = :semesterId")
    suspend fun deleteAllCoursesBySemesterId(semesterId: String)

    @Query("SELECT * FROM semester WHERE id = :semesterId")
    fun observeSemesterById(semesterId: String): LiveData<Semester>

    @Query("SELECT * FROM grades")
    fun getCurrentGradePoints(): Flow<GradeClass>

    @Query("SELECT * FROM semester ORDER BY id DESC")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semester_requests ORDER BY id DESC")
    fun getAllSemestersRequestList(): Flow<List<SemesterRequests>>

    @Query("SELECT * FROM semester WHERE isSynced = 0")
    suspend fun getAllUnSyncedSemesters(): List<Semester>

    @Query("DELETE FROM semester")
    suspend fun deleteAllSemesters()

    @Query("DELETE FROM semester_requests")
    suspend fun deleteAllSemesterRequests()

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

    @Query("SELECT  * FROM locally_deleted_semester_request_ids")
    suspend fun getAllLocallyDeletedSemReqIds(): List<LocallyDeletedSemesterRequestId>

    @Query("DELETE FROM locally_deleted_semester_request_ids WHERE deletedSemReqId  =:deleteSemReqId")
    suspend fun deleteLocallySemReqId(deleteSemReqId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocallyDeletedSemReqId(
        locallyDeletedSemesterRequestId: LocallyDeletedSemesterRequestId
    )

    @Query("SELECT * FROM grades")
    suspend fun getGradePointForUser(): GradeClass?

    @Query("SELECT * FROM semester_requests WHERE state = :state")
    fun getSemRequests(state: String): LiveData<List<SemesterRequests>>

    fun getPendingSemReq() = getSemRequests(STATE.PENDING.name)
    fun getAcceptedSemReq() = getSemRequests(STATE.ACCEPTED.name)
    fun getRejectedSemReq() = getSemRequests(STATE.REJECTED.name)
}
