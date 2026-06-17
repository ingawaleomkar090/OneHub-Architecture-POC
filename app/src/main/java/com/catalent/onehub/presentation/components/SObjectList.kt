package com.catalent.onehub.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.catalent.onehub.presentation.screen.EmptyErrorState
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.model.SObjectListState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SObjectList(
    state: SObjectListState,
    isConnected: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf(state.searchQuery) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery) {
        if (searchQuery != state.searchQuery) {
            delay(500)
            onSearchQueryChanged(searchQuery)
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            state.items.isNotEmpty() &&
                    last >= state.items.size - 3 &&
                    !state.isLoadingMore &&
                    state.hasMoreRemote &&
                    searchQuery.isEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

        Box(modifier = Modifier.weight(1f).pullRefresh(pullRefreshState)) {
            when {
                state.isInitialLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.isOffline && state.items.isEmpty() -> {
                    EmptyErrorState(
                        message = "You appear to be offline. Connect to load records.",
                        onRetry = onRefresh,
                    )
                }
                state.items.isEmpty() && !state.isRefreshing -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No records found.")
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(state.items.size) { index ->
                            val item = state.items[index]
                            ListItem(
                                headlineContent = { Text(item.name) },
                                supportingContent = { Text(item.id) },
                            )
                            HorizontalDivider()
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SObjectListPreview() {
    SObjectList(
        state = SObjectListState(
            items = listOf(
                SObjectData("1", "Preview Item 1"),
                SObjectData("2", "Preview Item 2"),
                SObjectData("3", "Preview Item 3"),
            ),
            isInitialLoading = false
        ),
        isConnected = true,
        onSearchQueryChanged = {},
        onRefresh = {},
        onLoadMore = {}
    )
}