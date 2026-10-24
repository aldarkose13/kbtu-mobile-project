package com.example.assignment2.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Direction
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: FavorViewModel, favorId: Int, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val favor = state.favors.firstOrNull { it.id == favorId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favor details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (favor != null) {
                        IconButton(onClick = {
                            vm.deleteFavor(favor.id)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (favor == null) {
            Text("Favor not found", modifier = Modifier.padding(innerPadding).padding(16.dp))
        } else {
            val now = LocalDateTime.now()
            Column(
                modifier = Modifier.padding(innerPadding).verticalScroll(
                    rememberScrollState()
                ).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(favor.description, style = MaterialTheme.typography.headlineSmall)
                DetailRow("Category", favor.category.name)
                DetailRow("Weight", favor.weight.toString())
                DetailRow("Direction",
                    if (favor.direction == Direction.IN_FAVOR) "They owe me" else "I owe them")
                DetailRow("Status", favor.statusAt(now).name)
                DetailRow("Created", favor.createdAt.toLocalDate().toString())
                DetailRow("Due", favor.dueAt.toLocalDate().toString())
                DetailRow("Interest", "%.2f".format(vm.interestFor(favor)))

                Button(
                    onClick = { vm.toggleRepaid(favor) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (favor.repaidAt == null) "Mark as repaid" else "Undo repayment")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value)
    }
}
