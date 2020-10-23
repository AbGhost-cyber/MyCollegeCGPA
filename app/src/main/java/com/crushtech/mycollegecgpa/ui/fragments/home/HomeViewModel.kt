package com.crushtech.mycollegecgpa.ui.fragments.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Events
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)

    private val _addOwnerStatus = MutableLiveData<Events<Resource<String>>>()
    val addOwnerStatus: LiveData<Events<Resource<String>>> = _addOwnerStatus


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

     fun addOwnerToSemester(owner: String, semesterId: String) {
         _addOwnerStatus.postValue(Events(Resource.loading(null)))

         if (owner.isEmpty() || semesterId.isEmpty()) {
             _addOwnerStatus.postValue(
                 Events(
                     Resource.error(
                         "The owner can't be empty", null
                     )
                 )
             )
             return
         }
         viewModelScope.launch {
             delay(3000L)
             val result = semesterRepository.addOwnerToSemester(owner, semesterId)

             _addOwnerStatus.postValue(Events(result))
         }
     }

     fun observeSemesterById(semesterId: String) =
         semesterRepository.observeSemesterById(semesterId)
 }