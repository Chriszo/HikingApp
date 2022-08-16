package com.example.hikingapp.domain.users.settings

import java.io.Serializable

class UserSettings(
    val distanceUnit: String,
    val heightUnit: String,
//    val timeUnit: String,
    val showTips: Boolean,
    val temperatureUnit: String
) : Serializable {
}