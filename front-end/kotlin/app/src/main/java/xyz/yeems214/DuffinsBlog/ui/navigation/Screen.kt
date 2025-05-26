package xyz.yeems214.DuffinsBlog.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object BlogList : Screen("blog_list")
    object BlogDetail : Screen("blog_detail/{postId}") {
        fun createRoute(postId: String) = "blog_detail/$postId"
    }
    object CreatePost : Screen("create_post")
    object EditPost : Screen("edit_post/{postId}") {
        fun createRoute(postId: String) = "edit_post/$postId"
    }
    object Profile : Screen("profile")
}

object NavigationArgs {
    const val POST_ID = "postId"
}
