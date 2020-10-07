package com.crushtech.mycollegecgpa.data.remote


import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.data.remote.requests.*
import com.crushtech.mycollegecgpa.data.remote.responses.SimpleResponse
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

    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
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

    @POST("/addOwnerToSemester")
    suspend fun addOwnerToSemester(
        @Body addOwnerRequest: AddOwnerRequest
    ): Response<SimpleResponse>

    @POST("/addCourseToSemester")
    suspend fun addCourseToSemester(
        @Body addCourseRequest: AddCourseRequest
    ): Response<ResponseBody>

    @GET("/getSemester")
    suspend fun getSemester(): Response<List<Semester>>
}