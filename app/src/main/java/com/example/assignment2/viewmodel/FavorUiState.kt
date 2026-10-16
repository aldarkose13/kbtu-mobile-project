package com.example.assignment2.viewmodel

import com.example.assignment2.model.Direction
import com.example.assignment2.model.Favor
import com.example.assignment2.model.Person

data class FavorUiState(
    val people: List<Person> = emptyList(),
    val favors: List<Favor> = emptyList(),
    val searchQuery: String = "",
    val directionFilter: Direction? = null
) {
    val visibleFavors: List<Favor>
        get() = favors.filter { favor ->
            val matchesDirection = directionFilter == null || favor.direction == directionFilter
            val personName = people.find { it.id == favor.personId }?.username ?:""
            val matchesSearch = searchQuery.isBlank() ||
                    favor.description.contains(searchQuery, ignoreCase = true) ||
                    personName.contains(searchQuery, ignoreCase = true)
            matchesDirection && matchesSearch
        }

}