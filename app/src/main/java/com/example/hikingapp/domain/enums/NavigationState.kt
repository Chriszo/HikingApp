package com.example.hikingapp.domain.enums

import java.io.Serializable

enum class NavigationState:Serializable {
    NOT_STARTED,
    STARTED,
    PAUSED,
    STOPPED,
    SAVED,
    SAVED_STARTED,
    SAVED_PAUSED,
    SAVED_NOT_STARTED,
    FINISHED;

    companion object {
        fun from(stateTxt: String?): NavigationState {

            stateTxt?.let { savedState ->
                return NavigationState.values()
                    .filter { stateValue -> savedState == stateValue.name}[0]
                }
            return NOT_STARTED
        }
    }
}