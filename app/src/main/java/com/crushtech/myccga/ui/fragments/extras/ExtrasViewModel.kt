package com.crushtech.myccga.ui.fragments.extras

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.myccga.repositories.SemesterRepository
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