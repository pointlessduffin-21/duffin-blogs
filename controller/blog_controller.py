from flask import request, render_template, redirect, url_for, flash, jsonify
from flask_login import login_required, current_user
from datetime import datetime, timezone
from model import Post
from utils import parse_content_for_display, generate_ai_summary, save_uploaded_file, delete_file
from middleware import validate_post_data

class BlogController:
    def index(self):
        """Display all blog posts"""
        posts = Post.get_all_posts()
        for post in posts:
            post['parsed_content'] = parse_content_for_display(post.get('content', ''))
            if 'title' not in post or not post['title']:
                post['title'] = "Untitled Post"
            if 'slug' not in post or not post['slug']:
                from slugify import slugify
                post['slug'] = slugify(post['title']) if post['title'] else f"untitled-post-{post['_id']}"
        
        return render_template('index.html', posts=posts, now=datetime.now(timezone.utc))

    def view_post(self, slug):
        """Display a single blog post"""
        post = Post.get_by_slug(slug)
        if not post:
            return "Post not found", 404
        
        post['parsed_content'] = parse_content_for_display(post.get('content', ''))
        post_tags = post.get('tags', [])
        
        # Check if AI service is available and content exists
        from utils.ai_summary import gemini_model
        ai_available = bool(gemini_model and post.get('content') and len(post.get('content', '').strip()) >= 5)
        
        return render_template('view_post.html', post=post, post_tags=post_tags, ai_available=ai_available, now=datetime.now(timezone.utc))

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
                return jsonify({'summary': ai_summary})
            else:
                return jsonify({'error': 'Failed to generate summary'}), 500
                
        except Exception as e:
            print(f"Error in generate_summary_api: {e}")
            return jsonify({'error': 'Internal server error'}), 500

    @login_required
    def create_post_page(self):
        """Handle blog post creation"""
        if request.method == 'POST':
            title = request.form.get('title')
            content = request.form.get('content')
            tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
            hero_banner_file = request.files.get('hero_banner')

            # Validate post data
            errors = validate_post_data(title, content)
            if errors:
                for error in errors:
                    flash(error, 'error')
                return render_template('create_post.html', is_edit=False, title=title, content=content, tags=",".join(tags))

            # Handle hero banner upload
            hero_banner_url = None
            if hero_banner_file and hero_banner_file.filename:
                from slugify import slugify
                post_slug_temp = slugify(title)
                hero_banner_url = save_uploaded_file(hero_banner_file, f"{post_slug_temp}-hero-")

            # Create the post
            post_slug, post_id = Post.create_post(
                title=title,
                content=content,
                tags=tags,
                author_id=current_user.id,
                author_username=current_user.username,
                hero_banner_url=hero_banner_url
            )
            
            flash('Post created successfully!', 'success')
            return redirect(url_for('blog.view_post', slug=post_slug))

        return render_template('create_post.html', is_edit=False)

    @login_required
    def edit_post_page(self, slug):
        """Handle blog post editing"""
        post = Post.get_by_slug(slug)
        if not post:
            return "Post not found", 404

        # Check if user owns the post
        if str(post.get('author_id')) != current_user.id:
            flash('You can only edit your own posts.', 'error')
            return redirect(url_for('blog.view_post', slug=slug))

        if request.method == 'POST':
            title = request.form.get('title')
            content = request.form.get('content')
            tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
            hero_banner_file = request.files.get('hero_banner')
            remove_hero_banner = bool(request.form.get('remove_hero_banner'))

            # Validate post data
            errors = validate_post_data(title, content)
            if errors:
                for error in errors:
                    flash(error, 'error')
                return render_template('create_post.html', post=post, is_edit=True, title=title, content=content, tags=",".join(tags))

            # Handle hero banner
            hero_banner_url = None
            if hero_banner_file and hero_banner_file.filename:
                from slugify import slugify
                new_slug_temp = slugify(title)
                hero_banner_url = save_uploaded_file(hero_banner_file, f"{new_slug_temp}-hero-")

            # Update the post
            new_slug = Post.update_post(
                post_id=str(post['_id']),
                title=title,
                content=content,
                tags=tags,
                hero_banner_url=hero_banner_url,
                remove_hero_banner=remove_hero_banner
            )
            
            flash('Post updated successfully!', 'success')
            return redirect(url_for('blog.view_post', slug=new_slug or slug))

        return render_template('create_post.html', 
                             post=post, 
                             is_edit=True, 
                             title=post.get('title'), 
                             content=post.get('content'), 
                             tags=",".join(post.get('tags', [])))

    @login_required
    def delete_post(self, slug):
        """Handle blog post deletion"""
        post = Post.get_by_slug(slug)
        if not post:
            flash('Post not found.', 'error')
            return redirect(url_for('blog.index'))
        
        # Check if user owns the post
        if str(post.get('author_id')) != current_user.id:
            flash('You can only delete your own posts.', 'error')
            return redirect(url_for('blog.view_post', slug=slug))
        
        # Delete the post
        deleted_post = Post.delete_post(str(post['_id']))
        
        # Clean up associated files
        if deleted_post and deleted_post.get('hero_banner_url'):
            delete_file(deleted_post['hero_banner_url'])
        
        flash('Post deleted successfully!', 'success')
        return redirect(url_for('blog.index'))

    @login_required
    def upload_image_for_editor(self):
        """Handle image upload for the editor"""
        if 'file' not in request.files:
            return jsonify({'error': 'No file provided'}), 400
        
        file = request.files['file']
        if file.filename == '':
            return jsonify({'error': 'No file selected'}), 400
        
        if file:
            file_url = save_uploaded_file(file, "editor-")
            if file_url:
                return jsonify({'url': f"/static/{file_url}"}), 200
            else:
                return jsonify({'error': 'Invalid file type'}), 400
        
        return jsonify({'error': 'Upload failed'}), 500
