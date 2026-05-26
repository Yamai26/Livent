package com.example.livent.presentation.navigation

sealed class ExploreRoute(val path: String) {
    data object Home : ExploreRoute("explore_home")

    data object EventDetail : ExploreRoute("explore_event_detail/{eventId}") {
        const val ARG_EVENT_ID = "eventId"
        fun create(eventId: String) = "explore_event_detail/$eventId"
    }
}
