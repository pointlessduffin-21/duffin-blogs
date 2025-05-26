package xyz.yeems214.DuffinsBlog.data.repository

import kotlinx.coroutines.withTimeoutOrNull
import xyz.yeems214.DuffinsBlog.data.api.BlogApiService
import xyz.yeems214.DuffinsBlog.data.model.*

class BlogRepository(private val apiService: BlogApiService) {
    
    suspend fun getPosts(): Result<List<BlogPost>> {
        return try {
            val response = withTimeoutOrNull(30000L) { // 30 second timeout
                apiService.getPosts()
            }
            if (response?.isSuccessful == true) {
                Result.success(response.body()?.posts ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch posts: ${response?.message() ?: "Timeout"}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPost(id: String): Result<BlogPost> {
        return try {
            val response = apiService.getPost(id)
            if (response.isSuccessful) {
                response.body()?.let { post ->
                    Result.success(post)
                } ?: Result.failure(Exception("Post not found"))
            } else {
                Result.failure(Exception("Failed to fetch post: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPost(
        token: String,
        title: String,
        content: String,
        tags: List<String>,
        heroBannerUrl: String? = null
    ): Result<BlogPost> {
        return try {
            val response = apiService.createPost(
                authorization = "Bearer $token",
                request = CreatePostRequest(title, content, tags, heroBannerUrl)
            )
            if (response.isSuccessful) {
                response.body()?.let { post ->
                    Result.success(post)
                } ?: Result.failure(Exception("Failed to create post"))
            } else {
                Result.failure(Exception("Failed to create post: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePost(
        token: String,
        id: String,
        title: String,
        content: String,
        tags: List<String>,
        heroBannerUrl: String? = null
    ): Result<BlogPost> {
        return try {
            val response = apiService.updatePost(
                authorization = "Bearer $token",
                id = id,
                request = CreatePostRequest(title, content, tags, heroBannerUrl)
            )
            if (response.isSuccessful) {
                response.body()?.let { post ->
                    Result.success(post)
                } ?: Result.failure(Exception("Failed to update post"))
            } else {
                Result.failure(Exception("Failed to update post: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePost(token: String, id: String): Result<Unit> {
        return try {
            val response = apiService.deletePost("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete post: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchPosts(query: String): Result<List<BlogPost>> {
        return try {
            val response = apiService.searchPosts(query)
            if (response.isSuccessful) {
                Result.success(response.body()?.posts ?: emptyList())
            } else {
                Result.failure(Exception("Failed to search posts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPostsByTag(tag: String): Result<List<BlogPost>> {
        return try {
            val response = apiService.getPostsByTag(tag)
            if (response.isSuccessful) {
                Result.success(response.body()?.posts ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch posts by tag: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
