package com.example.assignment2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.assignment2.FavorApp
import com.example.assignment2.data.FavorRepository
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.Favor
import com.example.assignment2.model.HistoryEntry
import com.example.assignment2.model.Person
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A one-off message; [undoFavorId] adds an Undo button that restores that favor. */
data class UiEvent(val message: String, val undoFavorId: Int? = null)

/** Everything read from the database, bundled so it can be combined with the UI inputs. */
private data class Stored(
    val favors: List<Favor>,
    val people: List<Person>,
    val deleted: List<Favor>,
    val history: List<HistoryEntry>
)

@OptIn(FlowPreview::class)
class FavorViewModel(private val repository: FavorRepository) : ViewModel() {

    // Instant search text (what the field shows) and the filter chip selection.
    private val query = MutableStateFlow("")
    private val directionFilter = MutableStateFlow<Direction?>(null)

    // Filtering waits for a typing pause; starts with "" so the first load is not delayed.
    private val appliedQuery = query.debounce(300).distinctUntilChanged().onStart { emit("") }

    // One-off messages for the UI (snackbars). Buffered so none are lost during navigation.
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private fun notify(message: String, undoFavorId: Int? = null) {
        _events.trySend(UiEvent(message, undoFavorId))
    }

    /** Built from Room Flows, so any database change updates every screen automatically. */
    private val data = combine(
        repository.favors,
        repository.people,
        repository.deletedFavors,
        repository.history
    ) { favors, people, deleted, history -> Stored(favors, people, deleted, history) }

    val state: StateFlow<FavorUiState> = combine(
        data,
        query,
        appliedQuery,
        directionFilter
    ) { stored, q, applied, direction ->
        FavorUiState(
            favors = stored.favors,
            people = stored.people,
            deletedFavors = stored.deleted,
            history = stored.history,
            query = q,
            appliedQuery = applied,
            directionFilter = direction,
            balances = stored.people.associate {
                it.id to repository.balanceWith(stored.favors, it.id)
            },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavorUiState())

    fun addFavor(
        description: String, weight: Double, category: Category, direction: Direction,
        personId: Int, dueDate: LocalDate
    ) {
        viewModelScope.launch {
            repository.addFavor(personId, description.trim(), weight, category, direction, dueDate)
            notify("Favor added")
        }
    }

    fun updateFavor(
        id: Int, description: String, weight: Double, category: Category,
        direction: Direction, personId: Int, dueDate: LocalDate
    ) {
        viewModelScope.launch {
            repository.updateFavor(id, personId, description.trim(), weight, category, direction, dueDate)
            notify("Favor updated")
        }
    }

    fun toggleRepaid(favor: Favor) {
        viewModelScope.launch {
            val repay = favor.repaidAt == null
            repository.setRepaid(favor.id, repay)
            notify(if (repay) "Marked as repaid" else "Repayment undone")
        }
    }

    fun deleteFavor(id: Int) {
        viewModelScope.launch {
            repository.deleteFavor(id)
            notify("Favor deleted", undoFavorId = id)
        }
    }

    fun restoreFavor(id: Int) {
        viewModelScope.launch {
            repository.restoreFavor(id)
            notify("Favor restored")
        }
    }

    fun purgeFavor(id: Int) {
        viewModelScope.launch {
            repository.purgeFavor(id)
            notify("Favor deleted permanently")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            notify("History cleared")
        }
    }

    fun addPerson(name: String) {
        viewModelScope.launch {
            repository.addPerson(name.trim())
            notify("${name.trim()} added")
        }
    }

    fun renamePerson(id: Int, name: String) {
        viewModelScope.launch {
            repository.renamePerson(id, name.trim())
            notify("Renamed to ${name.trim()}")
        }
    }

    fun deletePerson(id: Int) {
        viewModelScope.launch {
            repository.deletePerson(id)
            notify("Person deleted")
        }
    }

    /** Returns an error message for an invalid person name, or null if it is acceptable. */
    fun validatePersonName(name: String, excludeId: Int?): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Name cannot be empty"
            trimmed.length > 30 -> "Name is too long (max 30 characters)"
            state.value.people.any {
                it.id != excludeId && it.username.equals(trimmed, ignoreCase = true)
            } -> "Someone with this name already exists"
            else -> null
        }
    }

    fun interestFor(favor: Favor): Double = repository.interestFor(favor)

    fun onQueryChange(q: String) { query.value = q }

    fun onDirectionChange(d: Direction?) { directionFilter.value = d }

    fun clearFilters() {
        query.value = ""
        directionFilter.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FavorApp
                FavorViewModel(app.repository)
            }
        }
    }
}
