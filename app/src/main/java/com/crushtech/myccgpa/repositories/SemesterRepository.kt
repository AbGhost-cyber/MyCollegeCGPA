package com.crushtech.myccgpa.repositories

import android.app.Application
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.data.local.SemesterDao
import com.crushtech.myccgpa.data.local.converters.LocallyDeletedSemesterRequestId
import com.crushtech.myccgpa.data.local.entities.*
import com.crushtech.myccgpa.data.remote.SemesterApi
import com.crushtech.myccgpa.data.remote.requests.*
import com.crushtech.myccgpa.ui.fragments.settings.SettingsFragmentDirections
import com.crushtech.myccgpa.utils.Constants.IS_LOGGED_IN
import com.crushtech.myccgpa.utils.Constants.IS_THIRD_PARTY
import com.crushtech.myccgpa.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.crushtech.myccgpa.utils.Constants.KEY_PASSWORD
import com.crushtech.myccgpa.utils.Constants.KEY_USERNAME
import com.crushtech.myccgpa.utils.Constants.NO_EMAIL
import com.crushtech.myccgpa.utils.Constants.NO_PASSWORD
import com.crushtech.myccgpa.utils.Constants.NO_USERNAME
import com.crushtech.myccgpa.utils.Constants.STATISTICS_FIRST_TIME_OPEN
import com.crushtech.myccgpa.utils.Constants.TOTAL_NUMBER_OF_COURSES
import com.crushtech.myccgpa.utils.Constants.TOTAL_NUMBER_OF_CREDIT_HOURS
import com.crushtech.myccgpa.utils.NetworkUtils.getNetworkLiveData
import com.crushtech.myccgpa.utils.Resource
import com.crushtech.myccgpa.utils.networkBoundResource
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
    private val authEmail = sharedPreferences.getString(
        KEY_LOGGED_IN_EMAIL,
        NO_EMAIL
    ) ?: NO_EMAIL

    suspend fun register(email: String, password: String, username: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.register(SignInRequest(email, password, username))
                if (response.isSuccessful && response.body()!!.success) {
                    Resource.success(response.body()?.message)
                } else {
                    Resource.error(
                        response.body()?.message
                            ?: response.message(),
                        null
                    )
                }
            } catch (e: Exception) {
                Resource.error(
                    "Couldn't connect to the servers. Check your internet connection",
                    null
                )
            }
        }

    suspend fun registerThirdPartyUser(email: String, username: String) =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    semesterApi.thirdPartyRegister(ThirdPartyAuthRequest(email, username))
                if (response.isSuccessful && response.body()!!.success) {
                    Resource.success(response.body()?.message)
                } else {
                    Resource.error(
                        response.body()?.message
                            ?: response.message(),
                        null
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

    suspend fun loginThirdPartyUser(email: String, username: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.thirdPartyLogin(
                    ThirdPartyAuthRequest(email, username)
                )
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

    suspend fun logOutUser(fragment: Fragment) {
        sharedPreferences.edit().putString(
            KEY_LOGGED_IN_EMAIL,
            NO_EMAIL
        ).apply()
        sharedPreferences.edit().putBoolean(
            IS_THIRD_PARTY,
            false
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
            SettingsFragmentDirections.actionOthersFragmentToChooseLoginOrSignUpFragment(),
            navOptions
        )
        semesterDao.deleteGradePoints()
        semesterDao.deleteAllSemesterRequests()
    }

    suspend fun insertSemester(semester: Semester) {
        val response = try {
            semesterApi.addSemester(semester)
        } catch (e: Exception) {
            null
        }

        if (response != null && response.isSuccessful) {
            // insert to our server
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
        semesterDao.upsertCourse(courses.also { it.semesterId = semesterId })
    }

    suspend fun updateAddedCourse(courses: Courses, semesterId: String, coursePosition: Int) {
        val response = try {
            semesterApi.updateAddedCourse(
                UpdateCourseRequest(
                    semesterId,
                    courses, coursePosition
                )
            )
        } catch (e: Exception) {
            null
        }
        if (response == null) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }
        semesterDao.upsertCourse(courses.also { it.semesterId = semesterId })
    }

    private suspend fun insertSemesters(semester: List<Semester>) {
        semester.forEach { insertSemester(it) }
    }

    suspend fun insertUserPdfDownloads(downloads: UserPdfDownloads) {
        val response = try {
            semesterApi.upsertUserPdfDownloads(downloads)
        } catch (e: Exception) {
            null
        }
        if (response == null) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }
    }

    suspend fun getUserPdfDownloads() = withContext(Dispatchers.IO) {
        try {
            val response = semesterApi.getUserPdfDownloads()
            if (response.isSuccessful) {
                Resource.success(UserPdfDownloads(response.body()!!.noOfPdfDownloads))
            } else {
                Resource.error(
                    "an unknown error occurred, please try again", null
                )
            }
        } catch (e: Exception) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }
    }

    suspend fun updateCourses(courses: List<Courses>, semesterId: String) {
        courses.forEach { updateAddedCourse(it, semesterId, courses.indexOf(it)) }
    }

    suspend fun insertGradesPoints(gradePoints: GradeClass) {
        val response = try {
            semesterApi.upsertUserGradePoints(gradePoints)
        } catch (e: Exception) {
            null
        }

        if (response == null) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }
        semesterDao.insertGrades(gradePoints)
    }

    suspend fun resetGradesPoints() = withContext(Dispatchers.IO) {
        val response = try {
            semesterApi.resetUserGradePoints()
        } catch (e: Exception) {
            null
        }
        if (response == null) {
            Resource.error(
                "Couldn't connect to the servers. Check your internet connection",
                null
            )
        }

        // delete previous grades in dao and reinsert new
        semesterDao.deleteGradePoints()
        semesterDao.insertGrades(GradeClass())
    }

    suspend fun addUserToSemester(semesterRequests: SemesterRequests, receiver: String) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.addOwnerToSemester(
                    AddUserToSemesterRequest(
                        semesterRequests,
                        receiver
                    )
                )
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

    private suspend fun insertSemesterRequest(semesterRequests: SemesterRequests) {
        semesterDao.insertSemesterRequest(semesterRequests)
    }

    private suspend fun insertSemRequestList(semesterRequests: List<SemesterRequests>) {
        semesterRequests.onEach {
            insertSemesterRequest(it)
        }
    }

    suspend fun acceptSharedSemester(semesterRequests: SemesterRequests) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.acceptSharedSemester(
                    AcceptSemesterRequest(
                        semesterRequests,
                        authEmail
                    )
                )
                if (response.isSuccessful && response.body()!!.success) {
                    //  insertSemesterRequest(semesterRequests.also { it.state = STATE.ACCEPTED })
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

    suspend fun rejectSharedSemester(semesterRequests: SemesterRequests) =
        withContext(Dispatchers.IO) {
            try {
                val response = semesterApi.rejectSharedSemester(
                    RejectSemesterRequest(
                        semesterRequests,
                        authEmail
                    )
                )
                if (response.isSuccessful && response.body()!!.success) {
                    // insertSemesterRequest(semesterRequests.also { it.state = STATE.REJECTED })
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

    fun getAllSemRequestList(): Flow<Resource<List<SemesterRequests>>> {
        return networkBoundResource(
            query = {
                semesterDao.getAllSemestersRequestList()
            },
            fetch = {
                syncSemRequests()
                currentSemReqResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let {
                    insertSemRequestList(it)
                }
            },
            shouldFetch = {
                getConnectionByPeeking()
            }
        )
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

    fun getGradePoints(): Flow<Resource<GradeClass>> {
        return networkBoundResource(
            query = {
                semesterDao.getCurrentGradePoints()
            },
            fetch = {
                syncGradePoints()
//                currentGradePointsResponse
                currentGradePointsResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let {
                    insertGradesPoints(it)
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

    private suspend fun deleteLocallyDeletedSemReqIds(deletedSemReqId: String) {
        semesterDao.deleteLocallySemReqId(deletedSemReqId)
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

    suspend fun deleteSemReq(semReqId: String) {
        val response = try {
            semesterApi.deleteSemRequest(DeleteSemRequest(authEmail, semReqId))
        } catch (e: Exception) {
            null
        }
        semesterDao.deleteSemReqById(semReqId)

        if (response == null || !response.isSuccessful) {
            semesterDao.insertLocallyDeletedSemReqId(LocallyDeletedSemesterRequestId(semReqId))
        } else {
            deleteLocallyDeletedSemReqIds(semReqId)
        }
    }

    private suspend fun deleteLocallyDeletedCourseId(deletedCourseId: String) {
        semesterDao.deleteLocallyCourseId(deletedCourseId)
    }

    private var currentSemesterResponse: Response<List<Semester>>? = null
    private var currentSemReqResponse: Response<List<SemesterRequests>>? = null

    private suspend fun syncSemRequests() {
        val locallyDeletedSemesterRequestId = semesterDao.getAllLocallyDeletedSemReqIds()
        locallyDeletedSemesterRequestId.forEach { id ->
            deleteSemReq(id.deletedSemReqId)
            //  deleteSemReq()
        }
        currentSemReqResponse = semesterApi.getSemesterRequestList()
        currentSemReqResponse?.body()?.let { semRequestLists ->
            semesterDao.deleteAllSemesterRequests()
            insertSemRequestList(semRequestLists)
        }
    }

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
            // we delete all local semesters and reinsert again
            semesterDao.deleteAllSemesters()
            // insert semesters gotten from server and set synced state to true
            insertSemesters(semesters.onEach { semester -> semester.isSynced = true })
        }
    }

    private var currentGradePointsResponse: Response<GradeClass>? = null

    private suspend fun syncGradePoints() {
        val unSyncedGradePoints = semesterDao.getGradePointForUser()
        unSyncedGradePoints?.let {
            insertGradesPoints(it)
        }
        currentGradePointsResponse = semesterApi.getUserGradePoints()
        currentGradePointsResponse?.body()?.let {
            // we delete all local gradePoints and reinsert again
            semesterDao.deleteGradePoints()
            insertGradesPoints(it)
        }
    }

    fun getPendingSemList() = semesterDao.getPendingSemReq()
    fun getAcceptedSemList() = semesterDao.getAcceptedSemReq()
    fun getRejectedSemList() = semesterDao.getRejectedSemReq()
}
