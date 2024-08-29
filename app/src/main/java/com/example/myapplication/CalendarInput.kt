package com.example.myapplication

data class CalendarInput(
    val day:Int,
    val toDos:List<String> = emptyList(),
)