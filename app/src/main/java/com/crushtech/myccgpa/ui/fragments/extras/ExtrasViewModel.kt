package com.crushtech.myccgpa.ui.fragments.extras

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.myccgpa.repositories.SemesterRepository
import kotlinx.coroutines.launch

class ExtrasViewModel @ViewModelInject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {


    fun logOutCurrentUser(fragment: Fragment) {
        viewModelScope.launch {
            semesterRepository.logOutUser(fragment)
        }
    }
}