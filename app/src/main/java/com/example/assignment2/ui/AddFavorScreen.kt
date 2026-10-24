package com.example.assignment2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.Importance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavorScreen(vm: FavorViewModel, onDone: () -> Unit) {

    var description by rememberSaveable { mutableStateOf("") }
    var importance by rememberSaveable { mutableStateOf(Importance.NORMAL) }
    var category by rememberSaveable { mutableStateOf(Category.HELP) }
    var direction by rememberSaveable { mutableStateOf(Direction.IN_FAVOR) }

    val weight = importance.weight
    val canSave = description.isNotBlank()
    val state by vm.state.collectAsStateWithLifecycle()
    var personId by rememberSaveable { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New favor") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).verticalScroll(
                rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("What was the favor?") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Weight", style = MaterialTheme.typography.labelLarge)
            WeightSelector(importance, onSelect = { importance = it })
            Text("With", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.people.forEach { p ->
                    FilterChip(
                        selected = personId == p.id,
                        onClick = { personId = p.id },
                        label = { Text(p.username) }
                    )
                }
            }
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(c.name) }
                    )
                }
            }
            Text("Direction", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = direction == Direction.IN_FAVOR,
                    onClick = { direction = Direction.IN_FAVOR },
                    label = { Text("They owe me") }
                )
                FilterChip(
                    selected = direction == Direction.OUT_FAVOR,
                    onClick = { direction = Direction.OUT_FAVOR },
                    label = { Text("I owe them") }
                )
            }
            Button(
                enabled = canSave,
                onClick = {
                    vm.addFavor(description, weight, category, direction, personId)
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}