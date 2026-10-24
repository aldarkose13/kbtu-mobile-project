package com.example.assignment2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun FavorScreen(
    vm: FavorViewModel,
    onAddClick: () -> Unit,
    onStatsClick: () -> Unit,
    onFavorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val visible = state.visibleFavors

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Favor")
            }
        }
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KarmaCard(state.karma, onClick = onStatsClick)
            SearchBar(state.query, vm::onQueryChange)
            DirectionFilter(state.directionFilter, vm::onDirectionChange)
            if (state.isLoading){
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible, key = { it.id }) { favor ->
                        val personName =
                            state.people.firstOrNull() { it.id == favor.personId }?.username ?: "?"
                        FavorCard(
                            favor,
                            personName,
                            onToggleRepaid = { vm.toggleRepaid(favor) },
                            onClick = { onFavorClick(favor.id) }
                        )
                    }

                }
            }
        }
    }


}