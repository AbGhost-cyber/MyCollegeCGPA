package com.crushtech.mycollegecgpa.repositories

import android.app.Application
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.local.SemesterDao
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.LocallyDeletedCourseId
import com.crushtech.mycollegecgpa.data.local.entities.LocallyDeletedSemesterId
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.data.remote.SemesterApi
import com.crushtech.mycollegecgpa.data.remote.requests.*
import com.crushtech.mycollegecgpa.ui.fragments.others.OthersFragmentDirections
import com.crushtech.mycollegecgpa.utils.Constants.IS_LOGGED_IN
import com.crushtech.mycollegecgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.KEY_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.NO_EMAIL
import com.crushtech.mycollegecgpa.utils.Constants.NO_PASSWORD
import com.crushtech.mycollegecgpa.utils.Constants.NO_USERNAME
import com.crushtech.mycollegecgpa.utils.Constants.STATISTICS_FIRST_TIME_OPEN
import com.crushtech.mycollegecgpa.utils.Constants.TOTAL_NUMBER_OF_COURSES
import com.crushtech.mycollegecgpa.utils.Constants.TOTAL_NUMBER_OF_CREDIT_HOURS
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
    private val context: Application,
    private val sharedPreferences: SharedPreferences
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

    fun logOutUser(fragment: Fragment) {
        sharedPreferences.edit().putString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ).apply()
        sharedPreferences.edit().putString(
            KEY_PASSWORD,
            NO_PASSWORD
        ).apply()
        sharedPreferences.edit().putString(
            KEY_USERNAME,
            NO_USERNAME
        ).apply()
        sharedPreferences.edit().putBoolean(
            IS_LOGGED_IN,
            false
        ).apply()
        sharedPreferences.edit().remove(
            TOTAL_NUMBER_OF_COURSES
        ).apply()
        sharedPreferences.edit().remove(
            TOTAL_NUMBER_OF_CREDIT_HOURS
        ).apply()
        sharedPreferences.edit().remove(
            STATISTICS_FIRST_TIME_OPEN
        ).apply()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        fragment.findNavController().navigate(
            OthersFragmentDirections.actionOthersFragmentToChooseLoginOrSignUpFragment(),
            navOptions
        )
    }

    suspend fun insertSemester(semester: Semester) {
        val response = try {
            semesterApi.addSemester(semester)
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            //insert to our server
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

    suspend fun insertCourses(courses: List<Courses>, semesterId: String) {
        courses.forEach { insertCourseForSemester(it, semesterId) }
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
                getConnectionByPeeking()
            }
        )
    }

    private fun getConnectionByPeeking(): Boolean {
        val events = getNetworkLiveData(context.applicationContext).value
        events?.let {
            return it.peekContent()
        }
        return false
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
        semesterDao.deleteLocallySemesterId(deletedSemesterId)
    }


    suspend fun deleteCourse(courseId: String, semesterId: String) {
        val response = try {
            semesterApi.deleteCourse(DeleteCourseRequest(courseId, semesterId))
        } catch (e: Exception) {
            null
        }
        semesterDao.deleteCourseById(courseId, semesterId)

        if (response == null || !response.isSuccessful) {
            semesterDao.insertLocallyDeletedCourseId(LocallyDeletedCourseId(courseId, semesterId))
        } else {
            deleteLocallyDeletedCourseId(courseId)
        }
    }

    suspend fun deleteLocallyDeletedCourseId(deletedCourseId: String) {
        semesterDao.deleteLocallyCourseId(deletedCourseId)
    }


    private var currentSemesterResponse: Response<List<Semester>>? = null

    private suspend fun syncSemesters() {
        val locallyDeletedSemesterIds = semesterDao.getAllLocallyDeletedSemesterIds()
        locallyDeletedSemesterIds.forEach { id ->
            deleteSemester(id.deletedSemesterId)
        }

        val locallyDeletedCourseIds = semesterDao.getAllLocallyDeletedCourseIds()
        locallyDeletedCourseIds.forEach { id ->
            deleteCourse(id.deletedCourseId, id.deletedCourseSemesterId)
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
