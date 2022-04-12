package com.example.hikingapp

import java.io.ByteArrayOutputStream

interface LocalDBExecutor {

    fun saveToLocalDB(userName: String, image: ByteArrayOutputStream)
}