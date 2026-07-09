package com.example.thenotesapp

class NoteRepository(private val dao: NoteDao) {

    val allNotes = dao.getAllNotes()

    suspend fun insert(note: NoteEntity) {
        dao.insertNote(note)
    }

    suspend fun delete(note: NoteEntity) {
        dao.deleteNote(note)
    }
    suspend fun updateNote(note: NoteEntity) {
        dao.updateNote(note)
    }
}