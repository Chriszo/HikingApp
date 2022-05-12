package com.example.hikingapp

import com.example.hikingapp.domain.navigation.UserNavigationData
import java.io.ByteArrayOutputStream

interface LocalDBExecutor {

    fun saveToLocalDB(userName: String, image: ByteArrayOutputStream) {}
    fun saveUserNavigationData(userNavigationData: UserNavigationData) {}
    fun loadUserNavigationData(userID: String, routeId: Long): UserNavigationData? {return null}
}