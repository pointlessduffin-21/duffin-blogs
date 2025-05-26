package xyz.yeems214.DuffinsBlog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.yeems214.DuffinsBlog.data.model.BlogPost
import xyz.yeems214.DuffinsBlog.data.repository.AuthRepository
import xyz.yeems214.DuffinsBlog.data.repository.BlogRepository

class BlogViewModel(
    private val blogRepository: BlogRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BlogUiState())
    val uiState: StateFlow<BlogUiState> = _uiState.asStateFlow()
    
    private val _selectedPost = MutableStateFlow<BlogPost?>(null)
    val selectedPost: StateFlow<BlogPost?> = _selectedPost.asStateFlow()
    
    init {
        // Don't load posts immediately in init to avoid blocking app startup
        // Posts will be loaded when BlogListScreen is displayed
    }
    
    fun loadPosts() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            blogRepository.getPosts()
                .onSuccess { posts ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        posts = posts,
                        filteredPosts = posts
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load posts"
                    )
                }
        }
    }
    
    fun searchPosts(query: String) {
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                filteredPosts = _uiState.value.posts,
                searchQuery = ""
            )
        } else {
            _uiState.value = _uiState.value.copy(searchQuery = query)
            
            viewModelScope.launch {
                blogRepository.searchPosts(query)
                    .onSuccess { posts ->
                        _uiState.value = _uiState.value.copy(filteredPosts = posts)
                    }
                    .onFailure { 
                        // Fallback to local filtering
                        val filtered = _uiState.value.posts.filter { post ->
                            post.title?.contains(query, ignoreCase = true) == true ||
                            post.content?.contains(query, ignoreCase = true) == true ||
                            post.tags?.any { it.contains(query, ignoreCase = true) } == true
                        }
                        _uiState.value = _uiState.value.copy(filteredPosts = filtered)
                    }
            }
        }
    }
    
    fun filterByTag(tag: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            blogRepository.getPostsByTag(tag)
                .onSuccess { posts ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredPosts = posts,
                        selectedTag = tag
                    )
                }
                .onFailure {
                    // Fallback to local filtering
                    val filtered = _uiState.value.posts.filter { post ->
                        post.tags?.contains(tag) == true
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredPosts = filtered,
                        selectedTag = tag
                    )
                }
        }
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filteredPosts = _uiState.value.posts,
            searchQuery = "",
            selectedTag = null
        )
    }
    
    fun selectPost(post: BlogPost) {
        _selectedPost.value = post
    }
    
    fun createPost(title: String, content: String, tags: List<String>, heroBannerUrl: String? = null) {
        _uiState.value = _uiState.value.copy(isCreating = true, error = null)
        
        viewModelScope.launch {
            val token = authRepository.getAuthToken()
            if (token != null) {
                blogRepository.createPost(token, title, content, tags, heroBannerUrl)
                    .onSuccess { 
                        _uiState.value = _uiState.value.copy(isCreating = false)
                        loadPosts() // Refresh the posts list
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            error = exception.message ?: "Failed to create post"
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = "Authentication required"
                )
            }
        }
    }
    
    fun getAllTags(): List<String> {
        return _uiState.value.posts
            .mapNotNull { it.tags } // Filter out null tags lists
            .flatten() // Flatten the lists of tags
            .distinct()
            .sorted()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BlogUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val posts: List<BlogPost> = emptyList(),
    val filteredPosts: List<BlogPost> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val error: String? = null
)
