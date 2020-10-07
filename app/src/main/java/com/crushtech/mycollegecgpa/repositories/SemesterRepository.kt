package com.crushtech.mycollegecgpa.repositories

import android.app.Application
import com.crushtech.mycollegecgpa.data.local.SemesterDao
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.LocallyDeletedSemesterId
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.data.remote.SemesterApi
import com.crushtech.mycollegecgpa.data.remote.requests.*
import com.crushtech.mycollegecgpa.utils.NetworkUtils.getNetworkLiveData
import com.crushtech.mycollegecgpa.utils.Resource
import com.crushtech.mycollegecgpa.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class SemesterRepository @Inject constructor(
    private val semesterDao: SemesterDao,
    private val semesterApi: SemesterApi,
    private val context: Application
) {

    suspend fun register(email: String, password: String, username: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.register(SignInRequest(email, password, username))
                if (response.isSuccessful && response.body()!!.success) {
                    Resource.success(response.body()?.message)
                } else {
                    Resource.error(
                        response.body()?.message
                            ?: response.message(), null
                    )
                }
            } catch (e: Exception) {
                Resource.error(
                    "Couldn't connect to the servers. Check your internet connection",
                    null
                )
            }

        }

    suspend fun login(email: String, password: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body()!!.success) {
                    Resource.success(response.body()?.message)
                } else {
                    Resource.error(
                        response.body()?.message ?: response.message(), null
                    )
                }
            } catch (e: Exception) {
                Resource.error(
                    "Couldn't connect to the servers. Check your internet connection",
                    null
                )
            }

        }

    suspend fun insertSemester(semester: Semester) {
        val response = try {
            semesterApi.addSemester(semester)
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            semesterDao.insertSemester(semester.apply { isSynced = true })
        } else {
            // if response is null then insert note into
            // local db but don't sync and also notify the user of the error
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
            semesterDao.insertSemester(semester)
        }
    }

    fun observeSemesterById(semesterId: String) = semesterDao.observeSemesterById(semesterId)

    suspend fun insertCourseForSemester(courses: Courses, semesterId: String) {
        val response = try {
            semesterApi.addCourseToSemester(AddCourseRequest(semesterId, courses))
        } catch (e: Exception) {
            null
        }
        if (response == null) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }
        semesterDao.insertCourse(courses)
    }

    private suspend fun insertSemesters(notes: List<Semester>) {
        notes.forEach { insertSemester(it) }
    }

    suspend fun addOwnerToSemester(owner: String, semesterId: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.addOwnerToSemester(AddOwnerRequest(owner, semesterId))
                if (response.isSuccessful && response.body()!!.success) {
                    Resource.success(response.body()?.message)
                } else {
                    Resource.error(
                        response.body()?.message ?: response.message(), null
                    )
                }
            } catch (e: Exception) {
                Resource.error(
                    "Couldn't connect to the servers. Check your internet connection",
                    null
                )
            }

        }

    fun getAllSemesters(): Flow<Resource<List<Semester>>> {
        return networkBoundResource(
            query = {
                semesterDao.getAllSemesters()
            },
            fetch = {
                syncSemesters()
                currentSemesterResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let {
                    insertSemesters(it.onEach { semester -> semester.isSynced = true })
                }
            },
            shouldFetch = {
                getNetworkLiveData(context.applicationContext).value!!
            }
        )
    }

    suspend fun getSemesterById(semesterId: String) = semesterDao.getSemesterById(semesterId)

    suspend fun getCourseList(semesterId: String) = semesterDao.getCourseList(semesterId)

    suspend fun deleteSemester(semesterId: String) {
        val response = try {
            semesterApi.deleteSemester(DeleteSemesterRequest(semesterId))
        } catch (e: Exception) {
            null
        }
        semesterDao.deleteSemesterById(semesterId)

        if (response == null || !response.isSuccessful) {
            semesterDao.insertLocallyDeletedSemesterId(LocallyDeletedSemesterId(semesterId))
        } else {
            deleteLocallyDeletedSemesterId(semesterId)
        }
    }

    suspend fun deleteLocallyDeletedSemesterId(deletedSemesterId: String) {
        semesterDao.deleteLocallySemesterNoteId(deletedSemesterId)
    }

    private var currentSemesterResponse: Response<List<Semester>>? = null

    private suspend fun syncSemesters() {
        val locallyDeletedSemesterIds = semesterDao.getAllLocallyDeletedSemesterIds()
        locallyDeletedSemesterIds.forEach { id ->
            deleteSemester(id.deletedSemesterId)
        }

        val unSyncedSemesters = semesterDao.getAllUnSyncedSemesters()
        unSyncedSemesters.forEach { semester ->
            insertSemester(semester)
        }

        currentSemesterResponse = semesterApi.getSemester()
        currentSemesterResponse?.body()?.let { semesters ->
            //we delete all local semesters and reinsert again
            semesterDao.deleteAllSemesters()
            //insert semesters gotten from server and set synced state to true
            insertSemesters(semesters.onEach { semester -> semester.isSynced = true })
        }
    }
}