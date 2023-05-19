package com.example.notesapp.viewModels

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.models.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(application: Application) : ViewModel() {
    private val sharedPreferences = application.getSharedPreferences("NoteSharedPreferences", Context.MODE_PRIVATE)
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note>
        get() = _notes
    init {
        _notes.addAll(getNotesFromSharedPreferences())
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteNoteById(id: UUID) {
        viewModelScope.launch {
            _notes.removeIf { it.id == id }
            val notesJsonString = Gson().toJson(_notes)
            sharedPreferences.edit().putString("notes", notesJsonString).apply()
        }
    }
    fun addNote(noteText: String) {
        viewModelScope.launch {
            val newNote = Note(
                text = noteText
            )
            _notes.add(newNote)
            val notesJsonString = Gson().toJson(_notes)
            sharedPreferences.edit().putString("notes", notesJsonString).apply()
        }
    }

    fun updateNoteText(noteId: UUID, noteText: String) {
        viewModelScope.launch {
            val existingNote = _notes.find { it.id == noteId }
            existingNote?.let {
                it.text = noteText
                val notesJsonString = Gson().toJson(_notes)
                sharedPreferences.edit().putString("notes", notesJsonString).apply()
            }
        }
    }

    private fun getNotesFromSharedPreferences(): List<Note> {
        val notesJsonString = sharedPreferences.getString("notes", null)
        val notesType = object : TypeToken<List<Note>>() {}.type
        return Gson().fromJson<List<Note>>(notesJsonString, notesType) ?: emptyList()
    }

}