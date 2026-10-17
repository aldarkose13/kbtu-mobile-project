package com.example.assignment2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.assignment2.model.*
import com.example.assignment2.model.Direction

@Composable
fun FavorScreen(vm: FavorViewModel = viewModel(), modifier: Modifier = Modifier) {
    val state by vm.state.collectAsStateWithLifecycle()

    var description by rememberSaveable { mutableStateOf("") }

    val visible = state.favors
        .filter { state.directionFilter == null || it.direction == state.directionFilter }
        .filter { it.description.contains(state.query, ignoreCase = true) }

    Column(modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("New favor") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (description.isNotBlank()){
                        vm.addFavor(description, 1.0, Category.HELP, Direction.IN_FAVOR)
                        description = ""
                    }
                }
            ){Text("Add") }
        }

        RotationDemo(state.counter, vm::incrementCounter)

        SearchBar(state.query, vm::onQueryChange)
        DirectionFilter(state.directionFilter, vm::onDirectionChange)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visible, key = { it.id }) { favor ->
                FavorCard(favor, onToggleRepaid = { vm.toggleRepaid(favor) })
            }
        }
    }
}