package com.crushtech.mycollegecgpa.ui.fragments.weights

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.crushtech.mycollegecgpa.data.local.entities.GradeClass
import com.crushtech.mycollegecgpa.repositories.SemesterRepository
import com.crushtech.mycollegecgpa.utils.Events
import com.crushtech.mycollegecgpa.utils.Resource
import kotlinx.coroutines.launch

class WeightViewModel @ViewModelInject constructor(
    private val semesterRepository: SemesterRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData(false)
    private val _allGradePoints = _forceUpdate.switchMap {
        semesterRepository.getGradePoints()
            .asLiveData(viewModelScope.coroutineContext)
    }.switchMap {
        MutableLiveData(Events(it))
    }

    fun syncGradePoints() = _forceUpdate.postValue(true)

    val allGradePoints: LiveData<Events<Resource<GradeClass>>> = _allGradePoints

    fun insertGradesPoints(gradePoints: GradeClass) = viewModelScope.launch {
        semesterRepository.insertGradesPoints(gradePoints)
    }


    fun resetGradePoints() = viewModelScope.launch {
        semesterRepository.resetGradesPoints()
    }
}