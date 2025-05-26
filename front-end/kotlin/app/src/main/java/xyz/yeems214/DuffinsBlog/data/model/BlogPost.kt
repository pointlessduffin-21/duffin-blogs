package xyz.yeems214.DuffinsBlog.data.model

import com.google.gson.annotations.SerializedName

data class BlogPost(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("parsed_content")
    val parsedContent: String? = null,
    @SerializedName("author_id")
    val authorId: String? = null,
    @SerializedName("author_username")
    val authorUsername: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null,
    @SerializedName("last_updated")
    val lastUpdated: String? = null,
    @SerializedName("tags")
    val tags: List<String>? = null,
    @SerializedName("hero_banner_url")
    val heroBannerUrl: String? = null,
    @SerializedName("ai_summary")
    val aiSummary: String? = null,
    // Legacy fields for backward compatibility
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("hero_image")
    val heroImage: String? = null,
    @SerializedName("summary")
    val summary: String? = null
) {
    // Helper properties for convenient access
    val displayTimestamp: String?
        get() = timestamp ?: createdAt
    
    val displayHeroImage: String?
        get() = heroBannerUrl ?: heroImage
    
    val displaySummary: String?
        get() = aiSummary ?: summary
}

data class CreatePostRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("hero_banner_url")
    val heroBannerUrl: String? = null
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
