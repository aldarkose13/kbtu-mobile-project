package com.example.assignment2.ui

import androidx.compose.runtime.Composable
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
    const val STATS = "stats"

    fun detail(id: Int) = "detail/$id"
}

@Composable
fun AppNav(vm: FavorViewModel = viewModel()){
    val nav = rememberNavController()
    NavHost(nav, startDestination = Routes.LIST){
        composable(Routes.LIST) {
            FavorScreen(
                vm,
                onAddClick = {nav.navigate(Routes.ADD)},
                onStatsClick = {nav.navigate(Routes.STATS)},
                onFavorClick = {nav.navigate(Routes.detail(it))}
            )
        }
        composable(Routes.ADD) {
            AddFavorScreen(vm, onDone = { nav.popBackStack() })
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("favorId") {type = NavType.IntType})
            ) { entry ->
                DetailScreen(vm, entry.arguments!!.getInt("favorId"),
                    onBack = {nav.popBackStack()})
        }
        composable(Routes.STATS) {
            StatsScreen(vm, onBack = { nav.popBackStack() })
        }
    }
}