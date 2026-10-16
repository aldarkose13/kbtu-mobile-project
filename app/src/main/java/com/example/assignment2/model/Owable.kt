package com.example.assignment2.model

import java.time.LocalDateTime

interface Owable<T> {

    fun repay(now: LocalDateTime):T

    fun undo(): T

}