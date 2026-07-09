package com.example.thenotesapp.data

import com.example.thenotesapp.NoteEntity
import com.example.thenotesapp.network.Post

sealed class NoteListItem {

    data class Local(val note: NoteEntity) : NoteListItem() {
        val title: String get() = note.title
        val subtitle: String get() = note.content
    }

    data class Remote(val post: Post) : NoteListItem() {
        val title: String get() = post.title
        val subtitle: String get() = post.body
    }
}