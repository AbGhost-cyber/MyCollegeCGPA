package com.crushtech.myccgpa.data.remote


import com.crushtech.myccgpa.data.local.entities.GradeClass
import com.crushtech.myccgpa.data.local.entities.Semester
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.crushtech.myccgpa.data.local.entities.UserPdfDownloads
import com.crushtech.myccgpa.data.remote.requests.*
import com.crushtech.myccgpa.data.remote.responses.SimpleResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SemesterApi {

    @POST("/register")
    suspend fun register(
        @Body signInRequest: SignInRequest
    ): Response<SimpleResponse>

    @POST("/third_party_register")
    suspend fun thirdPartyRegister(
        @Body thirdPartyAuthRequest: ThirdPartyAuthRequest
    ): Response<SimpleResponse>


    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<SimpleResponse>

    @POST("/third_party_login")
    suspend fun thirdPartyLogin(
        @Body thirdPartyAuthRequest: ThirdPartyAuthRequest
    ): Response<SimpleResponse>

    @POST("/addSemester")
    suspend fun addSemester(
        @Body semester: Semester
    ): Response<ResponseBody>

    @POST("/deleteSemester")
    suspend fun deleteSemester(
        @Body deleteSemesterRequest: DeleteSemesterRequest
    ): Response<ResponseBody>

    @POST("/deleteCourse")
    suspend fun deleteCourse(
        @Body deleteCourseRequest: DeleteCourseRequest
    ): Response<ResponseBody>

    @POST("/deleteSemRequests")
    suspend fun deleteSemRequest(
        @Body deleteSemRequest: DeleteSemRequest
    ): Response<ResponseBody>

    @POST("/addUserToSemester")
    suspend fun addOwnerToSemester(
        @Body addUserToSemesterRequest: AddUserToSemesterRequest
    ): Response<SimpleResponse>

    @POST("/acceptSharedSemester")
    suspend fun acceptSharedSemester(
        @Body acceptSharedSemester: AcceptSemesterRequest
    ): Response<SimpleResponse>

    @POST("/rejectSharedSemester")
    suspend fun rejectSharedSemester(
        @Body rejectSharedSemester: RejectSemesterRequest
    ): Response<SimpleResponse>

    @POST("/addCourseToSemester")
    suspend fun addCourseToSemester(
        @Body addCourseRequest: AddCourseRequest
    ): Response<ResponseBody>

    @POST("/updateAddedCourse")
    suspend fun updateAddedCourse(
        @Body updateCourseRequest: UpdateCourseRequest
    ): Response<ResponseBody>

    @GET("/getSemester")
    suspend fun getSemester(): Response<List<Semester>>

    @GET("/getPdfDownloads")
    suspend fun getUserPdfDownloads(): Response<UserPdfDownloads>

    @POST("/addUserPdfDownloadsCount")
    suspend fun upsertUserPdfDownloads(
        @Body pdfDownloads: UserPdfDownloads
    ): Response<ResponseBody>

    @POST("/editUserGradePoints")
    suspend fun upsertUserGradePoints(
        @Body gradePoints: GradeClass
    ): Response<ResponseBody>

    @POST("/resetUserGradePoints")
    suspend fun resetUserGradePoints(): Response<GradeClass>

    @GET("/getUserGradePoints")
    suspend fun getUserGradePoints(): Response<GradeClass>

    @GET("/getSemReqLists")
    suspend fun getSemesterRequestList(): Response<List<SemesterRequests>>
}