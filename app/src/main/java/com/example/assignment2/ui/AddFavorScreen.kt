package com.example.assignment2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.Favor
import com.example.assignment2.model.Importance
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Create form (favorId == null) or edit form (favorId != null).
 * The edit form waits for the favor to be loaded from the database, then pre-fills the fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavorScreen(
    vm: FavorViewModel,
    favorId: Int? = null,
    onDone: () -> Unit,
    onManagePeople: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val editing = favorId?.let { id -> state.favors.firstOrNull { it.id == id } }
    val isEdit = favorId != null

    Scaffold(
        snackbarHost = { AppSnackbarHost() },
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit favor" else "New favor") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isEdit && editing == null) {
            Box(Modifier.padding(innerPadding).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(if (state.isLoading) "Loading..." else "Favor not found")
            }
        } else {
            // key() resets the saved field values if a different favor is edited.
            androidx.compose.runtime.key(favorId) {
                FavorForm(
                    state = state,
                    initial = editing,
                    onSave = { description, weight, category, direction, personId, dueDate ->
                        if (editing != null) {
                            vm.updateFavor(
                                editing.id, description, weight, category, direction, personId, dueDate
                            )
                        } else {
                            vm.addFavor(description, weight, category, direction, personId, dueDate)
                        }
                        onDone()
                    },
                    onManagePeople = onManagePeople,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavorForm(
    state: FavorUiState,
    initial: Favor?,
    onSave: (String, Double, Category, Direction, Int, LocalDate) -> Unit,
    onManagePeople: () -> Unit,
    modifier: Modifier = Modifier
) {
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }
    var descriptionTouched by rememberSaveable { mutableStateOf(false) }
    var importance by rememberSaveable {
        mutableStateOf(
            Importance.entries.firstOrNull { it.weight == initial?.weight } ?: Importance.NORMAL
        )
    }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: Category.HELP) }
    var direction by rememberSaveable { mutableStateOf(initial?.direction ?: Direction.IN_FAVOR) }
    var selectedPerson by rememberSaveable { mutableStateOf(initial?.personId) }

    // Dates are saved as epoch days so they survive rotation.
    val createdOn = initial?.createdAt?.toLocalDate() ?: LocalDate.now()
    var dueEpochDay by rememberSaveable {
        mutableLongStateOf((initial?.dueAt?.toLocalDate() ?: createdOn.plusDays(7)).toEpochDay())
    }
    val dueDate = LocalDate.ofEpochDay(dueEpochDay)
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Default to the first person when none was chosen yet.
    val personId = selectedPerson ?: state.people.firstOrNull()?.id

    val descriptionError = FavorValidation.description(description)
    val personError = FavorValidation.person(personId)
    val dueError = FavorValidation.dueDate(dueDate, createdOn)
    val canSave = descriptionError == null && personError == null && dueError == null

    if (showDatePicker) {
        DueDatePickerDialog(
            initial = dueDate,
            minDate = createdOn,
            onPick = { dueEpochDay = it.toEpochDay(); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; descriptionTouched = true },
            label = { Text("What was the favor?") },
            isError = descriptionTouched && descriptionError != null,
            supportingText = {
                if (descriptionTouched && descriptionError != null) {
                    Text(descriptionError)
                } else {
                    Text("${description.trim().length}/$MAX_DESCRIPTION_LENGTH")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Weight", style = MaterialTheme.typography.labelLarge)
        WeightSelector(importance, onSelect = { importance = it })
        Text("With", style = MaterialTheme.typography.labelLarge)
        if (state.people.isEmpty()) {
            Text("You have not added anyone yet.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onManagePeople) { Text("Add a person") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.people.forEach { p ->
                FilterChip(
                    selected = personId == p.id,
                    onClick = { selectedPerson = p.id },
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
        Text("Due date", style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = { showDatePicker = true }) { Text(dueDate.toString()) }

        // Explain why Save is disabled instead of leaving the user guessing.
        val blocker = descriptionError ?: personError ?: dueError
        if (!canSave && blocker != null) {
            Text(
                blocker, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            enabled = canSave,
            onClick = {
                onSave(description, importance.weight, category, direction, personId!!, dueDate)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (initial != null) "Save changes" else "Save") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initial: LocalDate,
    minDate: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val utc = ZoneOffset.UTC
    val minMillis = minDate.atStartOfDay(utc).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(utc).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(utc).toLocalDate())
                    }
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = pickerState) }
}
