from flask import jsonify, request
from flask_login import current_user
from model import Post, User
from utils import generate_ai_summary
from middleware import token_required

class APIController:
    @token_required
    def get_posts_api(self, current_user_obj):
        """API endpoint to get all posts"""
        try:
            posts = Post.get_all_posts()
            posts_data = []
            
            for post in posts:
                post_data = {
                    'id': str(post['_id']),
                    'title': post.get('title', 'Untitled Post'),
                    'slug': post.get('slug', ''),
                    'content': post.get('content', ''),
                    'tags': post.get('tags', []),
                    'author_username': post.get('author_username', ''),
                    'timestamp': post.get('timestamp').isoformat() if post.get('timestamp') else None,
                    'last_updated': post.get('last_updated').isoformat() if post.get('last_updated') else None,
                    'hero_banner_url': post.get('hero_banner_url')
                }
                posts_data.append(post_data)
            
            return jsonify({
                'posts': posts_data,
                'total': len(posts_data)
            }), 200
            
        except Exception as e:
            return jsonify({'error': 'Failed to fetch posts'}), 500

    @token_required
    def get_post_api(self, current_user_obj, slug):
        """API endpoint to get a single post"""
        try:
            post = Post.get_by_slug(slug)
            if not post:
                return jsonify({'error': 'Post not found'}), 404
            
            post_data = {
                'id': str(post['_id']),
                'title': post.get('title', 'Untitled Post'),
                'slug': post.get('slug', ''),
                'content': post.get('content', ''),
                'tags': post.get('tags', []),
                'author_username': post.get('author_username', ''),
                'timestamp': post.get('timestamp').isoformat() if post.get('timestamp') else None,
                'last_updated': post.get('last_updated').isoformat() if post.get('last_updated') else None,
                'hero_banner_url': post.get('hero_banner_url')
            }
            
            return jsonify(post_data), 200
            
        except Exception as e:
            return jsonify({'error': 'Failed to fetch post'}), 500

    @token_required
    def create_post_api(self, current_user_obj):
        """API endpoint to create a new post"""
        try:
            data = request.get_json()
            
            if not data:
                return jsonify({'error': 'No data provided'}), 400
            
            title = data.get('title', '').strip()
            content = data.get('content', '').strip()
            tags = data.get('tags', [])
            
            if not title:
                return jsonify({'error': 'Title is required'}), 400
            
            # Create the post
            post_slug, post_id = Post.create_post(
                title=title,
                content=content,
                tags=tags,
                author_id=current_user_obj.id,
                author_username=current_user_obj.username
            )
            
            return jsonify({
                'message': 'Post created successfully',
                'slug': post_slug,
                'id': str(post_id)
            }), 201
            
        except Exception as e:
            return jsonify({'error': 'Failed to create post'}), 500

    @token_required
    def update_post_api(self, current_user_obj, slug):
        """API endpoint to update a post"""
        try:
            post = Post.get_by_slug(slug)
            if not post:
                return jsonify({'error': 'Post not found'}), 404
            
            # Check if user owns the post
            if str(post.get('author_id')) != current_user_obj.id:
                return jsonify({'error': 'You can only edit your own posts'}), 403
            
            data = request.get_json()
            if not data:
                return jsonify({'error': 'No data provided'}), 400
            
            title = data.get('title', post.get('title', '')).strip()
            content = data.get('content', post.get('content', '')).strip()
            tags = data.get('tags', post.get('tags', []))
            
            if not title:
                return jsonify({'error': 'Title is required'}), 400
            
            # Update the post
            new_slug = Post.update_post(
                post_id=str(post['_id']),
                title=title,
                content=content,
                tags=tags
            )
            
            return jsonify({
                'message': 'Post updated successfully',
                'slug': new_slug or slug
            }), 200
            
        except Exception as e:
            return jsonify({'error': 'Failed to update post'}), 500

    @token_required
    def delete_post_api(self, current_user_obj, slug):
        """API endpoint to delete a post"""
        try:
            post = Post.get_by_slug(slug)
            if not post:
                return jsonify({'error': 'Post not found'}), 404
            
            # Check if user owns the post
            if str(post.get('author_id')) != current_user_obj.id:
                return jsonify({'error': 'You can only delete your own posts'}), 403
            
            # Delete the post
            Post.delete_post(str(post['_id']))
            
            return jsonify({'message': 'Post deleted successfully'}), 200
            
        except Exception as e:
            return jsonify({'error': 'Failed to delete post'}), 500

    def generate_summary_api(self, slug):
        """API endpoint to generate AI summary for a post"""
        try:
            post = Post.get_by_slug(slug)
            if not post:
                return jsonify({'error': 'Post not found'}), 404
            
            from utils.ai_summary import gemini_model
            if not gemini_model:
                return jsonify({'error': 'AI service not available'}), 503
            
            content = post.get('content', '')
            if len(content.strip()) < 5:
                return jsonify({'error': 'Content too short for summary'}), 400
                
            ai_summary = generate_ai_summary(content, post.get('title', ''))
            
            if ai_summary:
                return jsonify({'summary': ai_summary}), 200
            else:
                return jsonify({'error': 'Failed to generate summary'}), 500
                
        except Exception as e:
            print(f"Error in generate_summary_api: {e}")
            return jsonify({'error': 'Internal server error'}), 500
