import xyz.yeems214.DuffinsBlog.data.model.BlogPost

fun main() {
    val post = BlogPost(heroBannerUrl = "/static/uploads/girliesss-hero-mharz.jpg")
    println("Original hero banner URL: ${post.heroBannerUrl}")
    println("Display hero image URL: ${post.displayHeroImage}")
}
