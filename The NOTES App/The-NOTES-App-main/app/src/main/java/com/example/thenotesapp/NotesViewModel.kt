package com.example.thenotesapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.thenotesapp.network.Post
import com.example.thenotesapp.network.PostRepository
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository,
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    val notes = repository.allNotes

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insert(NoteEntity(title = title, content = content))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    // ===== Online (API) state =====
    val posts = mutableStateOf<List<Post>>(emptyList())
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf("")

    // ===== Offline / Online mode toggle =====
    val isOnlineMode = mutableStateOf(true)
    private var hasFetchedOnce = false

    fun setOnlineMode(online: Boolean) {
        isOnlineMode.value = online
        if (online && !hasFetchedOnce) {
            fetchPosts()
        }
    }

    fun fetchPosts() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = ""
            try {
                posts.value = postRepository.getPosts()
                hasFetchedOnce = true
            } catch (e: Exception) {
                error.value = "Failed to load online notes: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun addOnlinePost(title: String, body: String) {
        viewModelScope.launch {
            try {
                // JSONPlaceholder always echoes back id=101 for new posts and
                // doesn't actually persist it — so we generate our own local id
                // to keep it unique in the UI for this session.
                val localId = (posts.value.maxOfOrNull { it.id } ?: 100) + 1
                val draft = Post(userId = 1, id = localId, title = title, body = body)
                postRepository.addPost(draft) // real network call, response ignored (mock API)
                posts.value = posts.value + draft
            } catch (e: Exception) {
                error.value = "Failed to add online note: ${e.message}"
            }
        }
    }

    fun updateOnlinePost(post: Post, newTitle: String, newBody: String) {
        viewModelScope.launch {
            try {
                val updated = post.copy(title = newTitle, body = newBody)
                postRepository.updatePost(updated) // real PUT call, mock API won't persist
                posts.value = posts.value.map { if (it.id == post.id) updated else it }
            } catch (e: Exception) {
                error.value = "Failed to update online note: ${e.message}"
            }
        }
    }

    fun deleteOnlinePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.id) // real DELETE call
                posts.value = posts.value.filter { it.id != post.id }
            } catch (e: Exception) {
                error.value = "Failed to delete online note: ${e.message}"
            }
        }
    }
}

class NotesViewModelFactory(
    private val repository: NoteRepository,
    private val postRepository: PostRepository = PostRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NotesViewModel(repository, postRepository) as T
    }
}