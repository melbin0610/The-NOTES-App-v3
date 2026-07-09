package com.example.thenotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Create
///import androidx.compose.material.icons.filled.Cloud // NEW
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import com.example.thenotesapp.network.Post // NEW


// Define some nice pastel colors for the sticky notes
val noteColors = listOf(
    Color(0xFFFFF9C4), // Light Yellow
    Color(0xFFE1BEE7), // Light Purple
    Color(0xFFC8E6C9), // Light Green
    Color(0xFFBBDEFB)  // Light Blue
)

val onlineNoteColor = Color(0xFFECEFF1) // NEW — neutral gray-blue for online posts

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6750A4), // Nice deep purple theme
                    secondary = Color(0xFF625B71)
                )
            ) {
                val context = applicationContext
                val database = NoteDatabase.getDatabase(context)
                val repository = NoteRepository(database.noteDao())
                val viewModel: NotesViewModel = viewModel(
                    factory = NotesViewModelFactory(repository)
                )
                NotesScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val posts = viewModel.posts.value       // NEW
    val loading = viewModel.isLoading.value // NEW
    val error = viewModel.error.value
    val isOnline = viewModel.isOnlineMode.value
    var editingPost by remember { mutableStateOf<com.example.thenotesapp.network.Post?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }

    // NEW: fetch online posts once when the screen first appears
    LaunchedEffect(Unit) {
        viewModel.fetchPosts()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Notes", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Offline", color = Color.White)
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { viewModel.setOnlineMode(it) }
                    )
                    Text("Online", color = Color.White)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNote = null
                    editingPost = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
            }
        }
    ) { padding ->
        if (notes.isEmpty() && posts.isEmpty() && !loading) {
            // Beautiful Empty State
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Create,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No notes yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap the + button to add your first note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // NEW: status banners for the online fetch (local notes still show regardless)
                if (loading) {
                    item {
                        Text(
                            "Loading online notes...",
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                if (error.isNotEmpty()) {
                    item {
                        Text(
                            error,
                            color = Color(0xFFB00020),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                items(notes, key = { "local_${it.id}" }) { note ->
                    // Pick a color based on the note's ID so it stays consistent
                    val bgColor = noteColors[note.id.toInt() % noteColors.size]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    note.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    note.content,
                                    fontSize = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF424242)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                IconButton(onClick = {
                                    editingNote = note
                                    showDialog = true
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.DarkGray
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteNote(note) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }

                // NEW: online posts rendered in the same list, read-only (no edit/delete)

                if (isOnline) {
                    items(posts, key = { "online_${it.id}" }) { post ->
                        OnlineNoteCard(
                            post = post,
                            onEdit = {
                                editingPost = post
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteOnlinePost(post) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        if (editingPost != null) {
            EditNoteDialog(
                note = NoteEntity(id = 0, title = editingPost!!.title, content = editingPost!!.body),
                onDismiss = { showDialog = false; editingPost = null },
                onConfirm = { title, body ->
                    viewModel.updateOnlinePost(editingPost!!, title, body)
                    showDialog = false
                    editingPost = null
                }
            )
        } else if (editingNote != null) {
            EditNoteDialog(
                note = editingNote!!,
                onDismiss = { showDialog = false },
                onConfirm = { title, content ->
                    viewModel.updateNote(editingNote!!.copy(title = title, content = content))
                    showDialog = false
                    editingNote = null
                }
            )
        } else {
            AddNoteDialog(
                onDismiss = { showDialog = false },
                onConfirm = { title, content ->
                    if (isOnline) viewModel.addOnlinePost(title, content)
                    else viewModel.addNote(title, content)
                    showDialog = false
                }
            )
        }
    }
}

// NEW: card for API-fetched posts, styled to match local notes but visually distinct
@Composable
fun OnlineNoteCard(post: com.example.thenotesapp.network.Post, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = onlineNoteColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Create, contentDescription = "Online note", tint = Color(0xFF546E7A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(post.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(post.body, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, color = Color(0xFF424242))
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.DarkGray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Note", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(title, content)
                }
            }) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun EditNoteDialog(note: NoteEntity, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(title, content)
                }
            }) {
                Text("Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}