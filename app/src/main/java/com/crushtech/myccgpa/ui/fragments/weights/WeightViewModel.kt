package com.crushtech.myccgpa.ui.fragments.weights

import androidx.lifecycle.*
import com.crushtech.myccgpa.data.local.entities.GradeClass
import com.crushtech.myccgpa.repositories.SemesterRepository
import com.crushtech.myccgpa.utils.Events
import com.crushtech.myccgpa.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
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