package com.crushtech.mycollegecgpa.ui.fragments.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Events
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)

    private val _allSemesters = _forceUpdate.switchMap {
        semesterRepository.getAllSemesters()
            .asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Events(it))
    }
    val allSemesters: LiveData<Events<Resource<List<Semester>>>> = _allSemesters

    fun syncAllSemesters() = _forceUpdate.postValue(true)

    fun insertSemester(semester: Semester) = viewModelScope.launch {
        semesterRepository.insertSemester(semester)
    }

    fun deleteSemester(semesterId: String) = viewModelScope.launch {
        semesterRepository.deleteSemester(semesterId)
    }

    fun deleteLocallyDeletedSemesterId(deletedSemesterId: String) = viewModelScope.launch {
        semesterRepository.deleteLocallyDeletedSemesterId(deletedSemesterId)
    }
}