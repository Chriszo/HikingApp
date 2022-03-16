package com.example.hikingapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class UserViewModel: ViewModel() {

    val user = MutableLiveData<FirebaseUser>()

}