package com.catalent.onehub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    @Serializable
    data object HomeRoute : Route

    @Serializable
    data object ShipmentRoute : Route

    @Serializable
    data object NotificationRoute : Route
}