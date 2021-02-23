package com.crushtech.myccgpa.ui.fragments.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.myccgpa.data.local.entities.Courses
import com.crushtech.myccgpa.data.local.entities.Semester
import com.crushtech.myccgpa.repositories.SemesterRepository
import com.crushtech.myccgpa.utils.Events
import com.crushtech.myccgpa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: SemesterRepository
) : ViewModel() {
    private val _semester = MutableLiveData<Events<Resource<Semester>>>()
    val semester: LiveData<Events<Resource<Semester>>> = _semester

    private val _courses = MutableLiveData<Events<Resource<List<Courses>>>>()
    val courses: LiveData<Events<Resource<List<Courses>>>> = _courses

    fun insertSemester(semester: Semester) = GlobalScope.launch {
        repository.insertSemester(semester)
    }

    fun insertCourse(courses: Courses, semesterId: String) = viewModelScope.launch {
        repository.insertCourseForSemester(courses, semesterId)
    }

    fun updateCourse(courses: Courses, semesterId: String, coursePosition: Int) =
        viewModelScope.launch {
            repository.updateAddedCourse(courses, semesterId, coursePosition)
        }

    fun updateCourses(courses: List<Courses>, semesterId: String) = viewModelScope.launch {
        repository.updateCourses(courses, semesterId)
    }

    fun getSemesterById(semesterId: String) = viewModelScope.launch {
        _semester.postValue(Events(Resource.loading(null)))
        val semester = repository.getSemesterById(semesterId)
        semester?.let {
            _semester.postValue(Events((Resource.success(it))))
        } ?: _semester.postValue(
            Events(
                Resource.error(
                    "semester not found",
                    null
                )
            )
        )
    }

    fun getCourseList(semesterId: String) = viewModelScope.launch {
        _courses.postValue(Events(Resource.loading(null)))
        val courses = repository.getCourseList(semesterId)
        courses?.let {
            _courses.postValue(Events(Resource.success(it)))
        } ?: _courses.postValue(
            Events(
                Resource.error(
                    "this semester has no course",
                    null
                )
            )
        )
    }

    fun deleteCourse(courseId: String, semesterId: String) = viewModelScope.launch {
        repository.deleteCourse(courseId, semesterId)
    }

//    fun deleteLocallyDeletedCourseId(deletedCourseId: String) = viewModelScope.launch {
//        repository.deleteLocallyDeletedCourseId(deletedCourseId)
//    }

}