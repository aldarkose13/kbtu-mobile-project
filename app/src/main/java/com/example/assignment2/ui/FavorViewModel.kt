package com.example.assignment2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment2.model.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(FlowPreview::class)
class FavorViewModel : ViewModel() {
    private val ledger = Ledger()

    private val _state = MutableStateFlow(FavorUiState())
    val state: StateFlow<FavorUiState> = _state.asStateFlow()

    // Raw search input; collected with debounce so filtering waits for a typing pause.
    private val queryInput = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryInput
                .debounce(300)
                .distinctUntilChanged()
                .collect { q -> _state.update { it.copy(appliedQuery = q) } }
        }
    }

    init {
        viewModelScope.launch {
            delay(1000)
            ledger.add(1, "Helped him move apartment", 1.0,
                Category.MOVING, Direction.IN_FAVOR)
            ledger.add(2, "Bought me lunch", 0.5,
                Category.FOOD, Direction.OUT_FAVOR, now = LocalDateTime.now().minusDays(8))
            ledger.add(1, "Drove me to the airport", 2.0,
                Category.RIDE, Direction.OUT_FAVOR)
            _state.update { it.copy(people = listOf(Person(1, "Ali"),
                Person(2, "Dariya"))) }
            refresh()
            _state.update { it.copy(isLoading = false) }
        }

    }

    private fun refresh() = _state.update { s ->
            s.copy(
                favors = ledger.all(),
                balances = s.people.associate { it.id to ledger.balanceWith(it.id) }
            )
    }

    fun addFavor(description: String, weight: Double, category: Category, direction: Direction,
                 personId: Int){
        ledger.add(personId, description.trim(), weight, category, direction)
        refresh()
    }

    fun toggleRepaid(favor: Favor){
        if (favor.repaidAt == null) ledger.repay(favor.id) else ledger.undo(favor.id)
        refresh()
    }

    fun deleteFavor(id: Int){
        ledger.remove(id)
        refresh()
    }

    fun interestFor(favor: Favor): Double = ledger.interestFor(favor)

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        queryInput.value = q
    }

    fun onDirectionChange(d: Direction?) = _state.update { it.copy(directionFilter = d) }

}