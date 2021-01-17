package com.crushtech.mycollegecgpa.ui.fragments.course

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.mycollegecgpa.data.local.entities.Courses
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Events
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseViewModel @ViewModelInject constructor(
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

//    fun insertCourses(courses: List<Courses>, semesterId: String) = viewModelScope.launch {
//        repository.insertCourses(courses, semesterId)
//    }
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

    fun deleteLocallyDeletedCourseId(deletedCourseId: String) = viewModelScope.launch {
        repository.deleteLocallyDeletedCourseId(deletedCourseId)
    }

}