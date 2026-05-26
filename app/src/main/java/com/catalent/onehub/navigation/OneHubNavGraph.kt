package com.catalent.onehub.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.catalent.onehub.presentation.screen.HomeScreen
import com.catalent.shipment.ui.presentation.ui.ShipmentScreen

@Composable
fun OneHubNavGraph(
    isConnected: Boolean,
    onLogout: () -> Unit,
) {
    val backStack = rememberNavBackStack(Route.HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.HomeRoute> {
                HomeScreen(
                    isConnected = isConnected,
                    onLogout = onLogout,
                    onNavigateToShipment = {
                        backStack.add(Route.ShipmentRoute)
                    },
                    onNavigateToNotification = {
//                        backStack.add(Route.NotificationRoute)
                    }
                )
            }
            entry<Route.ShipmentRoute> {
                ShipmentScreen()
            }
            entry<Route.NotificationRoute> {
//                NotificationScreen()
            }
        }
    )
}