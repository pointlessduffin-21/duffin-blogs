from datetime import datetime, timezone
from bson import ObjectId
from slugify import slugify
from model.database import posts_collection

class Post:
    @staticmethod
    def get_all_posts():
        """Get all posts sorted by timestamp"""
        return list(posts_collection.find().sort('timestamp', -1))
    
    @staticmethod
    def get_by_slug(slug):
        """Get post by slug"""
        return posts_collection.find_one({"slug": slug})
    
    @staticmethod
    def get_by_id(post_id):
        """Get post by ID"""
        return posts_collection.find_one({"_id": ObjectId(post_id)})
    
    @staticmethod
    def create_post(title, content, tags, author_id, author_username, hero_banner_url=None):
        """Create a new blog post"""
        post_slug = slugify(title)
        original_slug = post_slug
        counter = 1
        
        # Ensure unique slug
        while posts_collection.find_one({"slug": post_slug}):
            post_slug = f"{original_slug}-{counter}"
            counter += 1
        
        post_data = {
            'title': title,
            'slug': post_slug,
            'content': content,
            'tags': tags,
            'hero_banner_url': hero_banner_url,
            'author_id': ObjectId(author_id),
            'author_username': author_username,
            'timestamp': datetime.now(timezone.utc),
            'last_updated': datetime.now(timezone.utc)
        }
        
        result = posts_collection.insert_one(post_data)
        return post_slug, result.inserted_id
    
    @staticmethod
    def update_post(post_id, title, content, tags, hero_banner_url=None, remove_hero_banner=False):
        """Update an existing post"""
        update_data = {
            'title': title,
            'content': content,
            'tags': tags,
            'last_updated': datetime.now(timezone.utc)
        }
        
        # Handle slug update if title changed
        new_slug = slugify(title)
        existing_post = posts_collection.find_one({"_id": ObjectId(post_id)})
        if existing_post and existing_post.get('slug') != new_slug:
            if not posts_collection.find_one({"slug": new_slug, "_id": {"$ne": ObjectId(post_id)}}):
                update_data['slug'] = new_slug
        
        # Handle hero banner
        if hero_banner_url:
            update_data['hero_banner_url'] = hero_banner_url
        elif remove_hero_banner:
            update_data['hero_banner_url'] = None
        
        posts_collection.update_one({"_id": ObjectId(post_id)}, {"$set": update_data})
        return update_data.get('slug', existing_post.get('slug') if existing_post else None)
    
    @staticmethod
    def delete_post(post_id):
        """Delete a post"""
        post = posts_collection.find_one({"_id": ObjectId(post_id)})
        if post:
            posts_collection.delete_one({"_id": ObjectId(post_id)})
            return post
        return None
    
    @staticmethod
    def slug_exists(slug, exclude_id=None):
        """Check if slug already exists"""
        query = {"slug": slug}
        if exclude_id:
            query["_id"] = {"$ne": ObjectId(exclude_id)}
        return bool(posts_collection.find_one(query))
