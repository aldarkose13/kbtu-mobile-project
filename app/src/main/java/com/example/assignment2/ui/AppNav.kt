package com.example.assignment2.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val DETAIL = "detail/{favorId}"
    const val EDIT = "edit/{favorId}"
    const val STATS = "stats"
    const val PEOPLE = "people"
    const val HISTORY = "history"

    fun detail(id: Int) = "detail/$id"
    fun edit(id: Int) = "edit/$id"
}

@Composable
fun AppNav(vm: FavorViewModel = viewModel(factory = FavorViewModel.Factory)){
    val nav = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            // Show the newest message right away instead of queueing behind older ones.
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = if (event.undoFavorId != null) "Undo" else null,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed && event.undoFavorId != null) {
                    vm.restoreFavor(event.undoFavorId)
                }
            }
        }
    }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHostState) {
    NavHost(nav, startDestination = Routes.LIST){
        composable(Routes.LIST) {
            FavorScreen(
                vm,
                onAddClick = {nav.navigate(Routes.ADD)},
                onStatsClick = {nav.navigate(Routes.STATS)},
                onPeopleClick = {nav.navigate(Routes.PEOPLE)},
                onHistoryClick = {nav.navigate(Routes.HISTORY)},
                onFavorClick = {nav.navigate(Routes.detail(it))}
            )
        }
        composable(Routes.ADD) {
            AddFavorScreen(vm, onDone = { nav.popBackStack() },
                onManagePeople = { nav.navigate(Routes.PEOPLE) })
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("favorId") {type = NavType.IntType})
            ) { entry ->
                DetailScreen(vm, entry.arguments!!.getInt("favorId"),
                    onEdit = { nav.navigate(Routes.edit(it)) },
                    onBack = {nav.popBackStack()})
        }
        composable(
            Routes.EDIT,
            arguments = listOf(navArgument("favorId") {type = NavType.IntType})
        ) { entry ->
            AddFavorScreen(vm, entry.arguments!!.getInt("favorId"),
                onDone = { nav.popBackStack() },
                onManagePeople = { nav.navigate(Routes.PEOPLE) })
        }
        composable(Routes.STATS) {
            StatsScreen(vm, onBack = { nav.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(vm, onBack = { nav.popBackStack() })
        }
        composable(Routes.PEOPLE) {
            PeopleScreen(vm, onBack = { nav.popBackStack() })
        }
    }
    }
}
