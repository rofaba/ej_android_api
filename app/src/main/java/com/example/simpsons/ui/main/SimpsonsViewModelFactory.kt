package com.example.simpsons.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simpsons.data.repository.SimpsonsRepository
class SimpsonsViewModelFactory(private val repository: SimpsonsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimpsonsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimpsonsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}