package com.crushtech.myccgpa.ui.fragments.settings

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crushtech.myccgpa.repositories.SemesterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {


    fun logOutCurrentUser(fragment: Fragment) {
        viewModelScope.launch {
            semesterRepository.logOutUser(fragment)
        }
    }
}