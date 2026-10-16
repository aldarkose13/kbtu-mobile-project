package com.example.assignment2.model

sealed class Resource<out T> {
    // 1. Успешный результат (содержит данные)
    data class Success<out T>(val data: T) : Resource<T>()

    // 2. Ошибка (содержит исключение или понятное сообщение)
    data class Error(val exception: Throwable, val message: String? = null) : Resource<Nothing>()

    // 3. Состояние загрузки (полезно для UI в Android)
    object Loading : Resource<Nothing>()
}
