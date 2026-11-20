package com.example.assignment2

import android.app.Application
import com.example.assignment2.data.FavorRepository
import com.example.assignment2.data.local.AppDatabase

class FavorApp : Application() {
    val repository by lazy { FavorRepository(AppDatabase.get(this)) }
}
