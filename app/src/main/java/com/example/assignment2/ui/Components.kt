package com.example.assignment2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment2.model.*
import java.time.LocalDateTime

@Composable
fun FavorCard(favor: Favor, onToggleRepaid: () -> Unit, modifier: Modifier = Modifier){
    val status = favor.statusAt(LocalDateTime.now())
    Card(modifier = modifier.fillMaxWidth()){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (favor.direction == Direction.IN_FAVOR)
                    Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = favor.direction.name
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)){
                Text(favor.description, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${favor.category} - weight ${favor.weight} - $status",
                    style = MaterialTheme.typography.bodySmall
                )
                Checkbox(checked = favor.repaidAt != null, onCheckedChange = { onToggleRepaid() })
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier : Modifier = Modifier){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search favors") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DirectionFilter(
    selected: Direction?,
    onSelect: (Direction?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected == null, { onSelect(null) }, label = { Text("All") })
        FilterChip(selected == Direction.IN_FAVOR, { onSelect(Direction.IN_FAVOR) }, label = { Text("They owe me") })
        FilterChip(selected == Direction.OUT_FAVOR, { onSelect(Direction.OUT_FAVOR) }, label = { Text("I owe them") })
    }
}

@Composable
fun RotationDemo(vmCount: Int, onVmInc: () -> Unit) {
    var plain by remember { mutableStateOf(0) }
    var saved by rememberSaveable { mutableStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { plain++ }) { Text("remember: $plain") }
        Button(onClick = { saved++ }) { Text("saveable: $saved") }
        Button(onClick = onVmInc) { Text("VM: $vmCount") }
    }
}