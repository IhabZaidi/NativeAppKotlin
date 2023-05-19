package com.example.notesapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.notesapp.PERMISSIONS_REQUEST_RECORD_AUDIO
import com.example.notesapp.models.Note
import com.example.notesapp.viewModels.NoteViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("RememberReturnType", "UnrememberedMutableState")
@Preview
@Composable
fun NotesApp() {
    val context = LocalContext.current
    val activity = LocalLifecycleOwner.current as Activity
    val viewModel: NoteViewModel = remember { NoteViewModel(context.applicationContext as Application) }
    var noteText by remember { mutableStateOf(TextFieldValue("")) }
    val dialogOpenState = remember { mutableStateOf(false) }
    val editDialogOpenState = remember { mutableStateOf(false) }
    var updatedNoteText = mutableStateOf(TextFieldValue(""))
    val coroutineScope = rememberCoroutineScope()
    val selectedNote = remember { mutableStateOf<Note?>(null) }
    val speechRecognizer by remember { mutableStateOf(SpeechRecognizer.createSpeechRecognizer(context)) }
    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {}

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                noteText = TextFieldValue(matches[0])
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    speechRecognizer.setRecognitionListener(recognitionListener)
    if (editDialogOpenState.value) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF323036),
            onDismissRequest = {
                editDialogOpenState.value = false
                selectedNote.value = null
            },
            title = {
                Text(
                    text = "Edit Note",
                    color = Color.White
                )
            },
            text = {
                Column() {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = updatedNoteText.value,
                        onValueChange = { updatedNoteText.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Edit Note", color = Color.White)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color.Yellow,
                            unfocusedBorderColor = Color.Yellow,
                            placeholderColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }

            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                selectedNote.value?.let { note ->
                                    viewModel.updateNoteText(selectedNote.value!!.id, updatedNoteText.value.text)
                                    editDialogOpenState.value = false
                                    selectedNote.value = null
                                }
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Save", color = Color.White)
                    }
                    Button(
                        onClick = {
                            editDialogOpenState.value = false
                            selectedNote.value = null
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Exit", color = Color.White)
                    }
                }
            }
        )
    }

    Scaffold(
            backgroundColor = Color(0xFF323036),
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Row() {
                        Text(
                            text = "Notes App",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        OutlinedTextField(value = noteText, onValueChange = { noteText = it},
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            placeholder = {
                                Text(text = "type here or push to talk", color = Color.White)
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = Color.White,
                                focusedBorderColor = Color.Yellow,
                                unfocusedBorderColor = Color.Yellow,
                                placeholderColor = Color.White
                            )
                        )

                        IconButton(
                            onClick = {
                                dialogOpenState.value = true
                                noteText.text.trim().takeIf { it.isNotBlank() }?.let {
                                    viewModel.addNote(it)
                                    noteText = TextFieldValue("")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save note", tint = Color.Yellow,)
                        }

                        IconButton(
                            onClick = {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        activity,
                                        arrayOf(Manifest.permission.RECORD_AUDIO),
                                        PERMISSIONS_REQUEST_RECORD_AUDIO
                                    )
                                } else {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                    speechRecognizer.startListening(intent)
                                }

                            }
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Speech to text", tint = Color.Yellow)
                        }
                    }
                    Divider(color = Color.Yellow)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        items(viewModel.notes) { note ->
                            NoteComposable(noteItem = note,
                                selectedNote = selectedNote,
                                updatedNoteText= updatedNoteText,
                                editDialogOpenState= editDialogOpenState) { viewModel.deleteNoteById(note.id) }
                        }
                    }

                }
            }
        )
}

@Composable
fun NoteComposable(
    noteItem: Note,
    selectedNote: MutableState<Note?>,
    updatedNoteText: MutableState<TextFieldValue>,
    editDialogOpenState: MutableState<Boolean>,
    onDeleteClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = noteItem.text, modifier = Modifier.weight(1f), color = Color.White, fontSize = 20.sp)
        IconButton(
            onClick = {
                selectedNote.value = noteItem
                updatedNoteText.value = TextFieldValue(text = noteItem.text)
                editDialogOpenState.value = true
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit note", tint = Color.White)
        }
        IconButton(
            onClick = onDeleteClicked,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = Color.Red)
        }
    }
}
