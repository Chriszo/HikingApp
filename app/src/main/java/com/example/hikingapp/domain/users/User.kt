package com.example.hikingapp.domain.users

import java.io.Serializable

class User(
    var userId: Long,
    var userName: String,
    var mail: String,
    var password: String,
    var profileInfo: ProfileInfo?
) :
    Serializable {}