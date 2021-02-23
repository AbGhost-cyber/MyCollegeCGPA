package com.crushtech.myccgpa.ui.fragments.semesterrequest

import androidx.lifecycle.*
import com.crushtech.myccgpa.data.local.entities.SemesterRequests
import com.crushtech.myccgpa.repositories.SemesterRepository
import com.crushtech.myccgpa.utils.Events
import com.crushtech.myccgpa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SemesterRequestViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)
    private var _allSemestersRequests = _forceUpdate.switchMap {
        semesterRepository.getAllSemRequestList()
            .asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Events(it))
    }

    val allSemestersRequests: LiveData<Events<Resource<List<SemesterRequests>>>> =
        _allSemestersRequests


    fun syncAllSemestersRequest() = _forceUpdate.postValue(true)

    fun acceptSharedSemester(semesterRequests: SemesterRequests) =
        viewModelScope.launch {
            semesterRepository.acceptSharedSemester(semesterRequests)
        }

    fun rejectSharedSemester(semesterRequests: SemesterRequests) =
        viewModelScope.launch {
            semesterRepository.rejectSharedSemester(semesterRequests)
        }

    fun deleteSemRequest(semReqId: String) = viewModelScope.launch {
        semesterRepository.deleteSemReq(semReqId)
    }

    fun getPendingSemList() = semesterRepository.getPendingSemList()

    fun getAcceptedSemList() = semesterRepository.getAcceptedSemList()

    fun getRejectedSemList() = semesterRepository.getRejectedSemList()

}