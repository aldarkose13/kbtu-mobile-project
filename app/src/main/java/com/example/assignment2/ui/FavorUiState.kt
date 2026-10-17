package com.example.assignment2.ui

import com.example.assignment2.model.Favor
import com.example.assignment2.model.Direction

data class FavorUiState(
    val favors: List<Favor> = emptyList(),
    val query: String = "",
    val counter: Int = 0,
    val directionFilter: Direction? = null
) {

}