package com.example.hikingapp.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is navigation Fragment"
    }
    val text: LiveData<String> = _text
}