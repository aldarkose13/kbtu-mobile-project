package com.example.assignment2.ui

import com.example.assignment2.model.Direction
import com.example.assignment2.model.Favor
import com.example.assignment2.model.Person
import com.example.assignment2.model.Status
import java.time.LocalDateTime

data class FavorUiState(
    val favors: List<Favor> = emptyList(),
    val people: List<Person> = emptyList(),
    val query: String = "",          // what the text field shows (updates instantly)
    val appliedQuery: String = "",   // debounced copy used for filtering
    val directionFilter: Direction? = null,
    val balances: Map<Int, Double> = emptyMap(),
    val isLoading: Boolean = true
) {
    val karma: Double get() = balances.values.sum()
    /** Filtered by direction and search text; overdue first, repaid last, then newest first. */
    val visibleFavors: List<Favor>
        get() {
            val now = LocalDateTime.now()
            return favors
                .filter { directionFilter == null || it.direction == directionFilter }
                .filter { it.description.contains(appliedQuery, ignoreCase = true) }
                .sortedWith(
                    compareBy<Favor> {
                        when (it.statusAt(now)) {
                            Status.OVERDUE -> 0
                            Status.ACTIVE -> 1
                            Status.REPAID -> 2
                        }
                    }.thenByDescending { it.createdAt }
                )
        }
}
