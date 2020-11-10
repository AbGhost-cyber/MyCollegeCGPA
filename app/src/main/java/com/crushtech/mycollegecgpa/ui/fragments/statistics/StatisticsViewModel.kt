package com.crushtech.mycollegecgpa.ui.fragments.statistics

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.mycollegecgpa.data.local.entities.UserPdfDownloads
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.launch

class StatisticsViewModel @ViewModelInject constructor(
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