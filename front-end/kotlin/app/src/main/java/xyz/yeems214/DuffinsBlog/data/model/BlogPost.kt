package xyz.yeems214.DuffinsBlog.data.model

import com.google.gson.annotations.SerializedName

data class BlogPost(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("author_username")
    val authorUsername: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("hero_image")
    val heroImage: String? = null,
    @SerializedName("summary")
    val summary: String? = null
)

data class CreatePostRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("hero_image")
    val heroImage: String? = null
)

data class BlogPostsResponse(
    @SerializedName("posts")
    val posts: List<BlogPost>
)

data class ApiResponse<T>(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("data")
    val data: T? = null
)
