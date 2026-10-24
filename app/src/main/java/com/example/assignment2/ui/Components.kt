package com.example.assignment2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment2.model.*
import java.time.LocalDateTime

@Composable
fun FavorCard(favor: Favor, personName: String, onToggleRepaid: () -> Unit, onClick: () -> Unit,
              modifier: Modifier = Modifier){
    val status = favor.statusAt(LocalDateTime.now())
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()){
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
                    "$personName - ${favor.category} - weight ${favor.weight} - $status",
                    style = MaterialTheme.typography.bodySmall
                )
                Checkbox(checked = favor.repaidAt != null, onCheckedChange = { onToggleRepaid() })
            }
        }
    }
}

@Composable
private fun balanceColor(value: Double) = when {
    value > 0 -> MaterialTheme.colorScheme.primary
    value < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun KarmaCard(karma: Double, onClick: () -> Unit, modifier: Modifier = Modifier){
    val color = balanceColor(karma)
    val caption = when {
        karma > 0 -> "You are owed overall"
        karma < 0 -> "You owe overall"
        else -> "All settled"
    }
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()){
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Karma balance", style = MaterialTheme.typography.labelLarge)
            Text(
                "%+.1f".format(karma),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(caption, style = MaterialTheme.typography.bodySmall)
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
fun PersonBalanceRow(name: String, balance: Double, modifier: Modifier = Modifier){
    val color = balanceColor(balance)
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), Arrangement.SpaceBetween){
            Text(name)
            Text("%+.1f".format(balance), color = color)
        }
    }
}


@Composable
fun WeightSelector(selected: Importance, onSelect: (Importance) -> Unit,
                   modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Importance.entries.forEach { level ->
            val isSelected = level == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onSelect(level) }) {
                    Icon(
                        imageVector = when (level) {
                            Importance.LOW -> Icons.Default.SentimentDissatisfied
                            Importance.NORMAL -> Icons.Default.SentimentSatisfied
                            Importance.HIGH -> Icons.Default.SentimentVerySatisfied
                        },
                        contentDescription = level.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(if (isSelected) 48.dp else 36.dp)
                    )
                }
                Text(
                    level.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
