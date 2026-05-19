package com.example.livent.presentation.navigation

/**
 * Root graph: welcome → auth | guest_main | user_main | publisher_main.
 * Tab content inside *_main screens (no separate routes in Phase 3).
 */
sealed class Route(val path: String) {
    // Auth graph
    data object Welcome : Route("welcome")
    data object Login : Route("login")
    data object Register : Route("register")

    // Guest exploration
    data object GuestMain : Route("guest_main")

    // Authenticated user (spectator)
    data object UserMain : Route("user_main")

    // Authenticated publisher
    data object PublisherMain : Route("publisher_main")

    // Optional detail (shared)
    data object EventDetail : Route("event_detail/{eventId}") {
        fun create(eventId: String) = "event_detail/$eventId"
    }
}
