package com.example.assignment2.model

import java.time.Duration
import java.time.LocalDateTime

class InterestPolicy(
    val basePenalty: Int = 1,
    val interestRate: Double = 0.1,
    // in how many days interest is calculated by default one week
    val intervalDays: Int = 7
    ){
    init { require(intervalDays > 0) { "intervalDays must be > 0" } }

    fun completedIntervals(favor: Favor, now: LocalDateTime): Long {
        val end = favor.repaidAt ?: now
        require(!end.isBefore(favor.createdAt)) { "Time precedes creation" }
        return Duration.between(favor.createdAt, end).toDays() / intervalDays
    }

    fun interest(favor: Favor, now: LocalDateTime): Double  =
        basePenalty * interestRate * favor.weight * completedIntervals(favor, now)




}