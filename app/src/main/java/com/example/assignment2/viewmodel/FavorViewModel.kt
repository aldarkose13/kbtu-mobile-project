package com.example.assignment2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.assignment2.model.Category
import com.example.assignment2.model.Direction
import com.example.assignment2.model.Ledger
import com.example.assignment2.model.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavorViewModel : ViewModel(){
    private val ledger = Ledger()
    private var nextPersonId = 1

    private val _uiState = MutableStateFlow(FavorUiState())
    val uiState: StateFlow<FavorUiState> = _uiState.asStateFlow()

    fun addPerson(username: String){
        if (username.isBlank()) return
        val person = Person(nextPersonId++, username.trim())
        _uiState.update { it.copy(people = it.people + person) }
    }

    fun addFavor(
        personId: Int,
        description: String,
        weight: Double,
        category: Category,
        direction: Direction
    ){
        if (description.isBlank()) return
        ledger.add(personId, description.trim(), weight, category, direction)
        refreshFavors()
    }

    fun repay(id: Int){
        ledger.repay(id)
        refreshFavors()
    }

    fun undo(id: Int) {
        ledger.undo(id)
        refreshFavors()
    }

    fun onSearchChange(query: String){
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterChange(direction: Direction?){
        _uiState.update{ it.copy(directionFilter = direction) }
    }

    private fun refreshFavors(){
        _uiState.update { it.copy(favors = ledger.all()) }
    }


}