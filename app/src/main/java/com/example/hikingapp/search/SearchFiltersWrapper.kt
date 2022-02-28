package com.example.hikingapp.search

import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType

class SearchFiltersWrapper private constructor(
    val distance: Double,
    val type: RouteType?,
    val difficultyLevel: DifficultyLevel?,
    val rating: Float
) {


    class Builder {

        private var distance: Double = 0.0
        private var type: RouteType? = null
        private var difficultyLevel: DifficultyLevel? = null
        private var rating: Float = 0.0f


        fun withDistance(distance: Double): Builder = apply { this.distance = distance }

        fun withType(type: RouteType): Builder = apply { this.type = type }

        fun withDifficulty(difficultyLevel: DifficultyLevel): Builder =
            apply { this.difficultyLevel = difficultyLevel }

        fun withRating(rating: Float): Builder = apply { this.rating = rating }

        fun build(): SearchFiltersWrapper {
            return SearchFiltersWrapper(distance, type, difficultyLevel, rating)
        }
    }


}