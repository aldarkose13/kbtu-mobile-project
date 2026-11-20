package com.example.assignment2.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Direction
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: FavorViewModel, favorId: Int, onEdit: (Int) -> Unit, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val favor = state.favors.firstOrNull { it.id == favorId }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog && favor != null) {
        ConfirmDeleteDialog(
            title = "Delete favor?",
            message = "\"${favor.description}\" will move to Recently deleted. You can restore it from History.",
            onConfirm = {
                showDeleteDialog = false
                vm.deleteFavor(favor.id)
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost() },
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
                        IconButton(onClick = { onEdit(favor.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (favor == null) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                EmptyState(
                    title = "Favor not found",
                    message = "It may have been deleted. You can restore it from History.",
                    actionLabel = "Back",
                    onAction = onBack,
                    modifier = Modifier.padding(innerPadding)
                )
            }
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
