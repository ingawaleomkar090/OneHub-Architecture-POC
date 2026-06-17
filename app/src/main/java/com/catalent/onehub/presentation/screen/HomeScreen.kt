package com.catalent.onehub.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catalent.onehub.presentation.components.NetworkBanner
import com.catalent.onehub.presentation.components.SObjectList
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.model.SObjectListState
import com.catalent.shipment.ui.presentation.viewmodel.SObjectListViewModel

@Composable
fun HomeScreen(
    isConnected: Boolean,
    onLogout: () -> Unit,
    viewModel: SObjectListViewModel = hiltViewModel(),
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val sObjectType = if (selectedTabIndex == 0) "Contact" else "Account"

    val contactsState by viewModel.contactsState.collectAsStateWithLifecycle(SObjectListState())
    val accountsState by viewModel.accountsState.collectAsStateWithLifecycle(SObjectListState())
    val currentState = if (selectedTabIndex == 0) contactsState else accountsState

    LaunchedEffect(Unit) {
        viewModel.initialLoad(isConnected)
    }

    LaunchedEffect(isConnected) {
        viewModel.initialLoad(isConnected)
    }

    HomeScreenContent(
        isConnected = isConnected,
        selectedTabIndex = selectedTabIndex,
        currentState = currentState,
        onLogout = onLogout,
        onTabSelected = { selectedTabIndex = it },
        onSearchQueryChanged = { query ->
            viewModel.onSearchQueryChanged(sObjectType, query, isConnected)
        },
        onRefresh = { viewModel.refresh(sObjectType, isConnected) },
        onLoadMore = { viewModel.loadMore(sObjectType, isConnected) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    isConnected: Boolean,
    selectedTabIndex: Int,
    currentState: SObjectListState,
    onLogout: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    val tabs = listOf("Contacts", "Accounts")

    Scaffold(
        topBar = {
            Column {
                if (!isConnected) NetworkBanner()
                TopAppBar(
                    title = { Text("Salesforce Dashboard") },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
            SObjectList(
                state = currentState,
                isConnected = isConnected,
                onSearchQueryChanged = onSearchQueryChanged,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        isConnected = true,
        selectedTabIndex = 0,
        currentState = SObjectListState(
            items = listOf(
                SObjectData("1", "John Doe"),
                SObjectData("2", "Jane Smith")
            )
        ),
        onLogout = {},
        onTabSelected = {},
        onSearchQueryChanged = {},
        onRefresh = {},
        onLoadMore = {}
    )
}
