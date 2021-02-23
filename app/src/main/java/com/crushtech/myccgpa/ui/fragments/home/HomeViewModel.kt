package com.crushtech.myccgpa.ui.fragments.home

import androidx.lifecycle.*
import com.crushtech.myccgpa.data.local.entities.Semester
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.crushtech.myccgpa.repositories.SemesterRepository
import com.crushtech.myccgpa.utils.Events
import com.crushtech.myccgpa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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

    fun addOwnerToSemester(
        semesterRequests: SemesterRequests,
        receiver: String,
        semesterId: String
    ) {
        _addOwnerStatus.postValue(Events(Resource.loading(null)))

        if (receiver.isEmpty() || semesterId.isEmpty()) {
            _addOwnerStatus.postValue(
                Events(
                    Resource.error(
                        "The receiver can't be empty", null
                    )
                )
            )
            return
        }
        viewModelScope.launch {
            val result = semesterRepository.addUserToSemester(semesterRequests, receiver)

            _addOwnerStatus.postValue(Events(result))
        }
    }

    fun observeSemesterById(semesterId: String) =
        semesterRepository.observeSemesterById(semesterId)
}