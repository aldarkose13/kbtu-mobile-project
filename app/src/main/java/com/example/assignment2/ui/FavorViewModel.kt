package com.example.assignment2.ui

import androidx.lifecycle.ViewModel
import com.example.assignment2.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime

class FavorViewModel : ViewModel() {
    private val ledger = Ledger()

    private val _state = MutableStateFlow(FavorUiState())
    val state: StateFlow<FavorUiState> = _state.asStateFlow()

    init {
        ledger.add(1, "Helped him move apartment", 5.0,
            Category.MOVING, Direction.IN_FAVOR)
        ledger.add(2, "Bought me lunch", 2.0,
            Category.FOOD, Direction.OUT_FAVOR, now = LocalDateTime.now().minusDays(8))
        ledger.add(1, "Drove me to the airport", 4.0,
            Category.RIDE, Direction.OUT_FAVOR)
        refresh()
    }

    private fun refresh() = _state.update { it.copy(favors = ledger.all()) }

    fun addFavor(description: String, weight: Double, category: Category, direction: Direction){
        ledger.add(1, description, weight, category, direction)
        refresh()
    }

    fun toggleRepaid(favor: Favor){
        if (favor.repaidAt == null) ledger.repay(favor.id) else ledger.undo(favor.id)
        refresh()
    }

    fun incrementCounter() = _state.update { it.copy(counter = it.counter + 1) }
    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }
    fun onDirectionChange(d: Direction?) = _state.update { it.copy(directionFilter = d) }

}