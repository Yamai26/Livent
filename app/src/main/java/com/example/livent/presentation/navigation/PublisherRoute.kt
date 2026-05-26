package com.example.livent.presentation.navigation

/**
 * Nested graph inside [Route.PublisherMain].
 */
sealed class PublisherRoute(val path: String) {
    data object Dashboard : PublisherRoute("publisher_dashboard")

    data object EventWizardCreate : PublisherRoute("event_wizard")

    data object EventWizardEdit : PublisherRoute("event_wizard/{eventId}") {
        fun create(eventId: String) = "event_wizard/$eventId"
        const val ARG_EVENT_ID = "eventId"
    }
}
