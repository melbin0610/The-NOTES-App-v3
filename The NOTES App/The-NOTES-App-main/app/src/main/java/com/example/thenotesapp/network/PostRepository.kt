package com.example.thenotesapp.network

class PostRepository {
    suspend fun getPosts(): List<Post> = RetrofitInstance.api.getPosts()
    suspend fun addPost(post: Post): Post = RetrofitInstance.api.addPost(post)
    suspend fun updatePost(post: Post): Post = RetrofitInstance.api.updatePost(post.id, post)
    suspend fun deletePost(id: Int) = RetrofitInstance.api.deletePost(id)
}