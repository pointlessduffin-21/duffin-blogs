package xyz.yeems214.DuffinsBlog.data.api

import retrofit2.Response
import retrofit2.http.*
import xyz.yeems214.DuffinsBlog.data.model.*

interface BlogApiService {
    
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("posts")
    suspend fun getPosts(): Response<BlogPostsResponse>
    
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: String): Response<BlogPost>
    
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") authorization: String,
        @Body request: CreatePostRequest
    ): Response<BlogPost>
    
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: CreatePostRequest
    ): Response<BlogPost>
    
    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
    
    @GET("posts/search")
    suspend fun searchPosts(@Query("q") query: String): Response<BlogPostsResponse>
    
    @GET("posts/by-tag/{tag}")
    suspend fun getPostsByTag(@Path("tag") tag: String): Response<BlogPostsResponse>
}
