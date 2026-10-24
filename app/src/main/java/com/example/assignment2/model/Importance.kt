package com.example.assignment2.model

enum class Importance(val label: String, val weight: Double) {
    LOW("Not that important", 0.5),
    NORMAL("Normal", 1.0),
    HIGH("Extra important", 2.0)
}