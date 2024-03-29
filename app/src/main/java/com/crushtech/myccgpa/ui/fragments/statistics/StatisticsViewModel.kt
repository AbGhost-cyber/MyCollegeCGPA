package com.crushtech.myccgpa.ui.fragments.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.myccgpa.data.local.entities.UserPdfDownloads
import com.crushtech.myccgpa.repositories.SemesterRepository
import com.crushtech.myccgpa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {


    private val _pdfDownloads = MutableLiveData<Resource<UserPdfDownloads>>()
    val pdfDownloads: LiveData<Resource<UserPdfDownloads>> = _pdfDownloads

    fun upsertUserPdfDownloads(userPdfDownloads: UserPdfDownloads) = viewModelScope.launch {
        semesterRepository.insertUserPdfDownloads(userPdfDownloads)
    }

    fun getUserPdfDownloads() {
        _pdfDownloads.postValue(Resource.loading(null))
        viewModelScope.launch {
            val downloads = semesterRepository.getUserPdfDownloads()
            _pdfDownloads.postValue(downloads)
        }

    }

}