import os
from dotenv import load_dotenv
from flask import Flask, render_template, request, redirect, url_for, jsonify
from pymongo import MongoClient
from datetime import datetime, timezone # Import timezone
from bson import ObjectId # For fetching by ID
from slugify import slugify # For creating URL slugs
import re # For markdown-like link and image parsing
from werkzeug.utils import secure_filename # For secure filenames

UPLOAD_FOLDER = 'static/uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'} # Added webp

load_dotenv() # Load environment variables from .env file

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Get MongoDB URI from environment variable
MONGO_URI = os.getenv("MONGO_URI")
if not MONGO_URI:
    raise RuntimeError("MONGO_URI not set in .env file")

client = MongoClient(MONGO_URI)
db = client['duffins_blog']
posts_collection = db['posts']

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

# Helper function to parse content for display (links, images, embeds)
def parse_content_for_display(content):
    # Basic image: ![alt text](image_url)
    # Ensure it doesn't mess with already existing <img> tags
    content = re.sub(r'(?<!<img src=")\!\[(.*?)\]\((.*?)\)(?!">)', r'<img src="\2" alt="\1" class="img-fluid mb-2">', content)
    # Basic link: [link text](url)
    content = re.sub(r'\[(.*?)\]\((http[s]?://.*?)\)', r'<a href="\2" target="_blank" rel="noopener noreferrer">\1</a>', content)
    # Basic YouTube embed: [youtube](video_id_or_url) - Fixed regex
    content = re.sub(r'\[youtube\]\((?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/)?([a-zA-Z0-9_-]{11})\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="https://www.youtube.com/embed/\1" allowfullscreen></iframe></div>', content)
    # Basic generic iframe embed: [embed](url)
    content = re.sub(r'\[embed\]\((http[s]?://.*?)\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="\1" allowfullscreen></iframe></div>', content)
    # Convert newlines to <br> for HTML display, but not inside <pre> or other block elements
    # This is a simplified approach. A full markdown parser would be more robust.
    if not any(tag in content for tag in ['<pre', '<div', '<p']):
        content = content.replace('\n', '<br>')
    return content

@app.route('/')
def index():
    posts = list(posts_collection.find().sort('timestamp', -1))
    for post in posts:
        post['parsed_content'] = parse_content_for_display(post.get('content', ''))
        if 'title' not in post or not post['title']:
            post['title'] = "Untitled Post"
        if 'slug' not in post or not post['slug']:
            post['slug'] = slugify(post['title']) if post['title'] else f"untitled-post-{post['_id']}" # Ensure slug exists for linking
    return render_template('index.html', posts=posts, now=datetime.now(timezone.utc))

@app.route('/post/<slug>')
def view_post(slug):
    post = posts_collection.find_one({"slug": slug})
    if not post:
        return "Post not found", 404
    post['parsed_content'] = parse_content_for_display(post.get('content', ''))
    post_tags = post.get('tags', [])
    return render_template('view_post.html', post=post, post_tags=post_tags, now=datetime.now(timezone.utc))

@app.route('/create', methods=['GET', 'POST'])
def create_post_page():
    if request.method == 'POST':
        title = request.form.get('title')
        content = request.form.get('content') # This will be raw markdown-like text
        tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
        hero_banner_file = request.files.get('hero_banner')

        if not title: # Content can be empty initially
            return render_template('create_post.html', error="Title is required.", now=datetime.now(timezone.utc), title=title, content=content, tags=",".join(tags))

        post_slug = slugify(title)
        # Check for existing slug and append a unique ID if necessary
        original_slug = post_slug
        counter = 1
        while posts_collection.find_one({"slug": post_slug}):
            post_slug = f"{original_slug}-{counter}"
            counter += 1
        
        hero_banner_url = None
        if hero_banner_file and allowed_file(hero_banner_file.filename):
            filename = secure_filename(f"{post_slug}-hero-{hero_banner_file.filename}")
            hero_banner_file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            hero_banner_url = url_for('static', filename=f'uploads/{filename}')

        post_data = {
            'title': title,
            'slug': post_slug,
            'content': content, # Store raw content
            'tags': tags,
            'hero_banner_url': hero_banner_url,
            'timestamp': datetime.now(timezone.utc),
            'last_updated': datetime.now(timezone.utc)
        }
        posts_collection.insert_one(post_data)
        return redirect(url_for('view_post', slug=post_slug))

    return render_template('create_post.html', is_edit=False, now=datetime.now(timezone.utc))


@app.route('/edit/<slug>', methods=['GET', 'POST'])
def edit_post_page(slug):
    post = posts_collection.find_one({"slug": slug})
    if not post:
        return "Post not found", 404

    if request.method == 'POST':
        title = request.form.get('title')
        content = request.form.get('content')
        tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
        hero_banner_file = request.files.get('hero_banner')

        if not title:
            return render_template('create_post.html', error="Title is required.", post=post, is_edit=True, now=datetime.now(timezone.utc), tags=",".join(post.get('tags',[])))

        new_slug = slugify(title)
        if new_slug != slug and posts_collection.find_one({"slug": new_slug}):
            return render_template('create_post.html', error="A post with this new title already exists. Please choose a different title.", post=post, is_edit=True, now=datetime.now(timezone.utc), title=title, content=content, tags=",".join(tags))
        
        update_data = {
            'title': title,
            'content': content,
            'tags': tags,
            'last_updated': datetime.now(timezone.utc)
        }
        if new_slug != slug:
            update_data['slug'] = new_slug
        
        if hero_banner_file and allowed_file(hero_banner_file.filename):
            # Consider deleting old hero banner if replacing
            filename = secure_filename(f"{new_slug}-hero-{hero_banner_file.filename}")
            hero_banner_file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            update_data['hero_banner_url'] = url_for('static', filename=f'uploads/{filename}')
        elif request.form.get('remove_hero_banner'):
            # Logic to remove hero banner (delete file and set URL to None)
            # if post.get('hero_banner_url'):
            #     try:
            #         os.remove(os.path.join(app.config['UPLOAD_FOLDER'], os.path.basename(post['hero_banner_url'])))
            #     except OSError:
            #         pass # File might not exist, or other error
            update_data['hero_banner_url'] = None


        posts_collection.update_one({"_id": post['_id']}, {"$set": update_data})
        return redirect(url_for('view_post', slug=update_data.get('slug', slug)))

    return render_template('create_post.html', post=post, is_edit=True, now=datetime.now(timezone.utc), title=post.get('title'), content=post.get('content'), tags=",".join(post.get('tags',[])))


@app.route('/upload_image', methods=['POST'])
def upload_image_for_editor():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        file_url = url_for('static', filename=f'uploads/{filename}')
        return jsonify({"url": file_url})
    return jsonify({"error": "File type not allowed"}), 400

# Delete route (example, needs protection and confirmation)
@app.route('/post/<slug>/delete', methods=['POST']) 
def delete_post(slug):
    post = posts_collection.find_one_and_delete({"slug": slug})
    if post:
        # Optionally, delete associated images (hero banner, content images) from UPLOAD_FOLDER
        if post.get('hero_banner_url'):
            try:
                hero_banner_path = os.path.join(app.config['UPLOAD_FOLDER'], os.path.basename(post['hero_banner_url']))
                if os.path.exists(hero_banner_path):
                    os.remove(hero_banner_path)
            except Exception as e:
                print(f"Error deleting hero banner for {slug}: {e}")
        
        # Add logic here to find and delete images embedded in content if they are stored locally
        # This is complex if images are just URLs. If they follow a pattern or are listed in DB, it's easier.

        return redirect(url_for('index'))
    else:
        # Handle case where post wasn't found or not deleted
        return "Error deleting post or post not found", 404

if __name__ == '__main__':
    app.run(debug=True)