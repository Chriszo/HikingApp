package com.example.hikingapp.domain.users

class ProfileInfo(
    var savedRoutes: MutableList<Long>,
    var completedRoutes: MutableList<Long>,
    var savedSights: MutableList<Long>,
    var completedSights: MutableList<Long>
) {
}