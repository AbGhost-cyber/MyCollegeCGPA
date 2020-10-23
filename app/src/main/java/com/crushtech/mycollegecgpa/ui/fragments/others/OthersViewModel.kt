package com.crushtech.mycollegecgpa.ui.fragments.others

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import kotlinx.coroutines.launch

class OthersViewModel @ViewModelInject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    fun logOutCurrentUser(fragment: Fragment) {
        viewModelScope.launch {
            semesterRepository.logOutUser(fragment)
        }
    }
}