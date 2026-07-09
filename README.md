# The NOTES App

> **📌 Task-3 Updated Version** — this README reflects the app after completing Task-3 (API Integration, Networking & Dynamic Data Display). For the Task-2 state of the project, see the version history / earlier commits.

A modern, production-ready Notes application built for Android using Jetpack Compose and Room Database. This project demonstrates full local data persistence, live REST API integration, and clean architecture principles as part of an Android development internship.

## ✨ Features

### Task 1: Core Functionality
- [x] Clean, modern UI built entirely with Jetpack Compose
- [x] Material 3 design system with custom purple theme
- [x] Create, view, and delete notes seamlessly
- [x] Responsive layout with proper padding and spacing

### Task 2: Data Persistence & Architecture
- [x] Room Database integration for persistent local storage
- [x] Full CRUD operations (Create, Read, Update, Delete)
- [x] MVVM Architecture with ViewModel and Repository pattern
- [x] Automatic UI updates when database changes
- [x] Offline-first functionality (works without internet)

### Task 3: API Integration, Networking & Dynamic Data Display
- [x] Retrofit networking layer integrated alongside Room
- [x] Live data fetched from the JSONPlaceholder REST API
- [x] Online / Offline mode toggle in the top bar
- [x] Local notes (Room) and online notes (API) merged into a single dynamic list
- [x] Full CRUD on online notes too — Add (`POST`), Edit (`PUT`), Delete (`DELETE`)
- [x] Loading state while data is being fetched
- [x] Graceful error handling on network failure — local notes remain fully usable
- [x] All networking run on Kotlin Coroutines, off the main thread

### Visual Enhancements
- [x] Pastel-colored sticky note cards with shadows for local notes
- [x] Distinct gray-blue cards for online notes
- [x] Professional empty state with iconography
- [x] Smooth animations and transitions
- [x] Edit dialog with pre-filled content (works for both local and online notes)

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Local Database:** Room (SQLite abstraction)
- **Networking:** Retrofit + Gson converter
- **Remote API:** [JSONPlaceholder](https://jsonplaceholder.typicode.com/) (`/posts` endpoint)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository pattern
- **Async:** Kotlin Coroutines & Flow
- **Dependency Injection:** Manual (ViewModelFactory)

## 📁 Project Structure

```
app/src/main/java/com/example/thenotesapp/
├── MainActivity.kt          # UI Layer (Composables)
├── NoteEntity.kt            # Local data schema (Room)
├── NoteDao.kt                # Room Data Access Object
├── NoteDatabase.kt          # Room Database instance
├── NoteRepository.kt        # Local repository (Room)
├── NotesViewModel.kt         # ViewModel Layer — combines local + remote data
└── network/
    ├── Post.kt               # Data model for API responses
    ├── ApiService.kt        # Retrofit interface (GET/POST/PUT/DELETE)
    ├── RetrofitInstance.kt  # Retrofit client singleton
    └── PostRepository.kt    # Remote repository (Retrofit)
```

## 🌐 API Note

This app uses **JSONPlaceholder**, a free mock REST API for testing and prototyping. It accepts `POST`/`PUT`/`DELETE` requests and returns valid success responses, but doesn't persist changes server-side. This app makes real network calls for every operation (as required for the task), and reflects results in the UI for the current session — refetching or restarting resets online notes to the original seeded data. This is expected API behavior, not an app bug.

## 🚀 How to Run

1. Clone this repository
2. Open in Android Studio (latest version recommended)
3. Sync Gradle files
4. Run on an emulator or physical device (API 24+, internet connection required for online mode)

## 🎯 Learning Outcomes

This project demonstrates mastery of:
- Android local data persistence with Room
- REST API integration using Retrofit
- Reactive programming with Kotlin Flow and Coroutines
- Clean architecture separation of concerns across local and remote data sources
- Modern Android UI development with Compose
- Handling loading, success, and error states in a production-style UI
- Production-level code organization

Built as part of Android Development Internship — Task 1, 2 & 3
