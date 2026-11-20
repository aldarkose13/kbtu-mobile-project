package com.example.assignment2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Favor
import com.example.assignment2.model.HistoryEntry
import com.example.assignment2.model.HistoryType
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

/** Two tabs: the activity log, and the trash where deleted favors can be restored. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: FavorViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var purgeId by rememberSaveable { mutableStateOf<Int?>(null) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    val purging = state.deletedFavors.firstOrNull { it.id == purgeId }
    if (purging != null) {
        ConfirmDeleteDialog(
            title = "Delete forever?",
            message = "\"${purging.description}\" cannot be restored after this.",
            onConfirm = { vm.purgeFavor(purging.id); purgeId = null },
            onDismiss = { purgeId = null }
        )
    }
    if (confirmClear) {
        ConfirmDeleteDialog(
            title = "Clear activity log?",
            message = "All ${state.history.size} entries will be removed. Your favors are not affected.",
            onConfirm = { vm.clearHistory(); confirmClear = false },
            onDismiss = { confirmClear = false }
        )
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (tab == 0 && state.history.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear history")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Activity") })
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Recently deleted (${state.deletedFavors.size})") }
                )
            }
            if (tab == 0) {
                ActivityTab(state)
            } else {
                DeletedTab(
                    state = state,
                    onRestore = vm::restoreFavor,
                    onPurge = { purgeId = it }
                )
            }
        }
    }
}

@Composable
private fun ActivityTab(state: FavorUiState) {
    if (state.history.isEmpty()) {
        EmptyMessage("No activity yet.\nCreate, edit or repay a favor and it will show up here.")
        return
    }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "summary") { HistorySummary(state.history) }
        items(state.history, key = { it.id }) { HistoryRow(it) }
    }
}

@Composable
private fun HistorySummary(history: List<HistoryEntry>) {
    fun count(type: HistoryType) = history.count { it.type == type }
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStat("Created", count(HistoryType.CREATED))
            SummaryStat("Repaid", count(HistoryType.REPAID))
            SummaryStat("Deleted", count(HistoryType.DELETED))
            SummaryStat("Restored", count(HistoryType.RESTORED))
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(entry.type.label, style = MaterialTheme.typography.labelLarge)
            Text(entry.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                "With ${entry.personName} · ${entry.timestamp.format(timeFormat)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DeletedTab(
    state: FavorUiState,
    onRestore: (Int) -> Unit,
    onPurge: (Int) -> Unit
) {
    if (state.deletedFavors.isEmpty()) {
        EmptyMessage("Nothing deleted.\nFavors you delete can be restored from here.")
        return
    }
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.deletedFavors, key = { it.id }) { favor ->
            DeletedFavorRow(
                favor = favor,
                personName = state.people.firstOrNull { it.id == favor.personId }?.username ?: "?",
                onRestore = { onRestore(favor.id) },
                onPurge = { onPurge(favor.id) }
            )
        }
    }
}

@Composable
private fun DeletedFavorRow(
    favor: Favor,
    personName: String,
    onRestore: () -> Unit,
    onPurge: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(favor.description, style = MaterialTheme.typography.titleMedium)
                Text(personName, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore")
            }
            IconButton(onClick = onPurge) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete forever")
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
