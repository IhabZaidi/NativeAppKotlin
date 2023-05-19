package com.example.notesapp.models

import java.util.*

data class Note(
    val id: UUID = UUID.randomUUID(),
    var text: String = ""
)