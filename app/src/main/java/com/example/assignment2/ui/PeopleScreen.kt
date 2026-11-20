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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Person

/** Lists the people favors can be exchanged with; add, rename and delete live here. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(vm: FavorViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Dialog state survives rotation; people are referenced by id, never by object.
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Int?>(null) }

    val editing = state.people.firstOrNull { it.id == editingId }
    val deleting = state.people.firstOrNull { it.id == deletingId }

    if (showAdd) {
        PersonNameDialog(
            title = "New person",
            initialName = "",
            validate = { vm.validatePersonName(it, excludeId = null) },
            onConfirm = { vm.addPerson(it); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
    if (editing != null) {
        PersonNameDialog(
            title = "Rename person",
            initialName = editing.username,
            validate = { vm.validatePersonName(it, excludeId = editing.id) },
            onConfirm = { vm.renamePerson(editing.id, it); editingId = null },
            onDismiss = { editingId = null }
        )
    }
    if (deleting != null) {
        val count = state.favors.count { it.personId == deleting.id }
        ConfirmDeleteDialog(
            title = "Delete ${deleting.username}?",
            message = if (count == 0) "This person will be removed."
            else "This will also permanently delete $count favor(s) with ${deleting.username}.",
            onConfirm = { vm.deletePerson(deleting.id); deletingId = null },
            onDismiss = { deletingId = null }
        )
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        topBar = {
            TopAppBar(
                title = { Text("People") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add person")
            }
        }
    ) { innerPadding ->
        if (state.people.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding).padding(32.dp), Alignment.Center) {
                Text(
                    if (state.isLoading) "Loading..."
                    else "No people yet.\nTap + to add someone you exchange favors with.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.people, key = { it.id }) { person ->
                    PersonRow(
                        person = person,
                        favorCount = state.favors.count { it.personId == person.id },
                        onEdit = { editingId = person.id },
                        onDelete = { deletingId = person.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonRow(
    person: Person,
    favorCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(person.username, style = MaterialTheme.typography.titleMedium)
                Text("$favorCount favor(s)", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Rename ${person.username}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${person.username}")
            }
        }
    }
}

/** Name input dialog. [validate] returns an error message, or null when the name is fine. */
@Composable
private fun PersonNameDialog(
    title: String,
    initialName: String,
    validate: (String) -> String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var touched by rememberSaveable { mutableStateOf(false) }
    val error = validate(name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; touched = true },
                label = { Text("Name") },
                singleLine = true,
                isError = touched && error != null,
                supportingText = { if (touched && error != null) Text(error) }
            )
        },
        confirmButton = {
            TextButton(enabled = error == null, onClick = { onConfirm(name) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
