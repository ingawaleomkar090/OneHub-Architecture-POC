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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catalent.onehub.presentation.components.NetworkBanner
import com.catalent.onehub.presentation.components.SObjectList
import com.catalent.shipment.domain.model.SObjectListState
import com.catalent.shipment.ui.presentation.viewmodel.SObjectListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	isConnected: Boolean,
	onLogout: () -> Unit,
	viewModel: SObjectListViewModel = hiltViewModel(),
) {
	var selectedTabIndex by remember { mutableIntStateOf(0) }
	val tabs = listOf("Contacts", "Accounts")
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

	Scaffold(
		topBar = {
			Column {
				if (!isConnected) NetworkBanner()
				TopAppBar(
					title = { Text("Salesforce POC") },
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
						onClick = { selectedTabIndex = index },
						text = { Text(title) }
					)
				}
			}
			SObjectList(
				state = currentState,
				onSearchQueryChanged = { query ->
					viewModel.onSearchQueryChanged(sObjectType, query, isConnected)
				},
				onRefresh = { viewModel.refresh(sObjectType, isConnected) },
				onLoadMore = { viewModel.loadMore(sObjectType, isConnected) },
			)
		}
	}
}