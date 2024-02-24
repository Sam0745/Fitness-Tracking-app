package com.example.fitnesstrackingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.fitnesstrackingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject  constructor(
    private val mainRepository: MainRepository
) : ViewModel(){


    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalCaloriesBurned()
    val totalCaloriesBurned = mainRepository.getTotalDistance()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
}