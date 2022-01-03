package com.safaa.notesapp.Data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val noteList: MutableLiveData<List<Note>> = MutableLiveData()
    val db = Firebase.firestore

    fun readNotes(): MutableLiveData<List<Note>> {

        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val temp = arrayListOf<Note>()
                for (document in result) {
                    document.data.map { (key, value) ->
                        temp.add(Note(document.id, value.toString()))
                    }
                }
                noteList.postValue(temp)
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
            }

        return noteList
    }

    fun addNote(noteText: String) {
        val new_note = hashMapOf(
            "noteText" to noteText,
        )
        db.collection("notes")
            .add(new_note)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "MainViewModelAdd",
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w("MainViewModelAdd", "Error adding document", e)
            }
        readNotes()
    }

    fun editNote(selectedItem: Note) {
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val temp = arrayListOf<Note>()
                for (document in result) {
                    if (document.id == selectedItem.pk)
                        db.collection("notes").document(selectedItem.pk)
                            .update("noteText", selectedItem.text)
                }
                readNotes()
            }
            .addOnFailureListener { exception ->
                Log.w("MainViewModelDelete", "Error getting documents.", exception)
            }
    }

    fun removeNote(deletedItem: Note) {
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val temp = arrayListOf<Note>()
                for (document in result) {
                    if (document.id == deletedItem.pk)
                        db.collection("notes").document(deletedItem.pk).delete()
                }
                readNotes()
            }
            .addOnFailureListener { exception ->
                Log.w("MainViewModelDelete", "Error getting documents.", exception)
            }
    }

}
