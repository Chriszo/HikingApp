package com.example.hikingapp.persistence.entities

data class SightEntity(
    val sightId: Long,
    val name: String,
    val description: String,
    val rating: Float,
    val mainPhoto: Int
)
