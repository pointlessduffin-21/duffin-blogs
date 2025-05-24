import os
from dotenv import load_dotenv
from flask import Flask, render_template, request, redirect, url_for, jsonify, flash, session
from flask_login import LoginManager, UserMixin, login_user, logout_user, login_required, current_user
from flask_bcrypt import Bcrypt
from flask_mail import Mail, Message
from pymongo import MongoClient
from datetime import datetime, timezone, timedelta
from bson import ObjectId
from slugify import slugify
import re
import jwt
from werkzeug.utils import secure_filename
from functools import wraps
import secrets

UPLOAD_FOLDER = 'static/uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}

load_dotenv()

app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'dev-secret-key-change-this')
app.config['JWT_SECRET_KEY'] = os.getenv('JWT_SECRET_KEY', 'jwt-secret-key-change-this')
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Mail configuration
app.config['MAIL_SERVER'] = os.getenv('MAIL_SERVER', 'smtp.gmail.com')
app.config['MAIL_PORT'] = int(os.getenv('MAIL_PORT', 587))
app.config['MAIL_USE_TLS'] = os.getenv('MAIL_USE_TLS', 'True').lower() == 'true'
app.config['MAIL_USERNAME'] = os.getenv('MAIL_USERNAME')
app.config['MAIL_PASSWORD'] = os.getenv('MAIL_PASSWORD')

# Initialize extensions
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'
login_manager.login_message = 'Please log in to access this page.'
login_manager.login_message_category = 'info'

bcrypt = Bcrypt(app)
mail = Mail(app)

# MongoDB setup
MONGO_URI = os.getenv("MONGO_URI")
if not MONGO_URI:
    raise RuntimeError("MONGO_URI not set in .env file")

client = MongoClient(MONGO_URI)
db = client['duffins_blog']
posts_collection = db['posts']
users_collection = db['users']
reset_tokens_collection = db['reset_tokens']

# User class for Flask-Login
class User(UserMixin):
    def __init__(self, user_data):
        self.id = str(user_data['_id'])
        self.username = user_data['username']
        self.email = user_data['email']
        self.password_hash = user_data['password_hash']
        self.created_at = user_data.get('created_at', datetime.now(timezone.utc))
        self.is_active_user = user_data.get('is_active', True)

    def is_active(self):
        return self.is_active_user

    @staticmethod
    def get(user_id):
        user_data = users_collection.find_one({"_id": ObjectId(user_id)})
        if user_data:
            return User(user_data)
        return None

    @staticmethod
    def get_by_username(username):
        user_data = users_collection.find_one({"username": username})
        if user_data:
            return User(user_data)
        return None

    @staticmethod
    def get_by_email(email):
        user_data = users_collection.find_one({"email": email})
        if user_data:
            return User(user_data)
        return None

@login_manager.user_loader
def load_user(user_id):
    return User.get(user_id)

# JWT token decorator for API endpoints
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'message': 'Token is missing!'}), 401
        
        try:
            if token.startswith('Bearer '):
                token = token[7:]
            data = jwt.decode(token, app.config['JWT_SECRET_KEY'], algorithms=['HS256'])
            current_user_id = data['user_id']
            current_user_obj = User.get(current_user_id)
            if not current_user_obj:
                return jsonify({'message': 'Token is invalid!'}), 401
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Token has expired!'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Token is invalid!'}), 401
        
        return f(current_user_obj, *args, **kwargs)
    return decorated

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def parse_content_for_display(content):
    # Basic image: ![alt text](image_url)
    content = re.sub(r'(?<!<img src=")\!\[(.*?)\]\((.*?)\)(?!">)', r'<img src="\2" alt="\1" class="img-fluid mb-2">', content)
    # Basic link: [link text](url)
    content = re.sub(r'\[(.*?)\]\((http[s]?://.*?)\)', r'<a href="\2" target="_blank" rel="noopener noreferrer">\1</a>', content)
    # Basic YouTube embed: [youtube](video_id_or_url)
    content = re.sub(r'\[youtube\]\((?:https?://)?(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/)?([a-zA-Z0-9_-]{11})\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="https://www.youtube.com/embed/\1" allowfullscreen></iframe></div>', content)
    # Basic generic iframe embed: [embed](url)
    content = re.sub(r'\[embed\]\((http[s]?://.*?)\)', r'<div class="embed-responsive embed-responsive-16by9 mb-2"><iframe class="embed-responsive-item" src="\1" allowfullscreen></iframe></div>', content)
    # Bold text: **text**
    content = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', content)
    # Italic text: *text*
    content = re.sub(r'\*(.*?)\*', r'<em>\1</em>', content)
    # Strikethrough: ~~text~~
    content = re.sub(r'~~(.*?)~~', r'<del>\1</del>', content)
    # Code: `code`
    content = re.sub(r'`(.*?)`', r'<code>\1</code>', content)
    # Headers
    content = re.sub(r'^### (.*$)', r'<h3>\1</h3>', content, flags=re.MULTILINE)
    content = re.sub(r'^## (.*$)', r'<h2>\1</h2>', content, flags=re.MULTILINE)
    # Quote
    content = re.sub(r'^> (.*$)', r'<blockquote>\1</blockquote>', content, flags=re.MULTILINE)
    # List items
    content = re.sub(r'^- (.*$)', r'<li>\1</li>', content, flags=re.MULTILINE)
    # Newlines to br
    if not any(tag in content for tag in ['<pre', '<div', '<p']):
        content = content.replace('\n', '<br>')
    return content

# Template context processor to provide datetime to all templates
@app.context_processor
def inject_datetime():
    return {'datetime': datetime, 'timezone': timezone}

# Authentication Routes
@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')
        confirm_password = request.form.get('confirm_password')

        if not all([username, email, password, confirm_password]):
            flash('All fields are required.', 'error')
            return render_template('auth/register.html')

        if password != confirm_password:
            flash('Passwords do not match.', 'error')
            return render_template('auth/register.html')

        if len(password) < 6:
            flash('Password must be at least 6 characters long.', 'error')
            return render_template('auth/register.html')

        # Check if user already exists
        if User.get_by_username(username):
            flash('Username already exists.', 'error')
            return render_template('auth/register.html')

        if User.get_by_email(email):
            flash('Email already registered.', 'error')
            return render_template('auth/register.html')

        # Create new user
        password_hash = bcrypt.generate_password_hash(password).decode('utf-8')
        user_data = {
            'username': username,
            'email': email,
            'password_hash': password_hash,
            'created_at': datetime.now(timezone.utc),
            'is_active': True
        }
        
        result = users_collection.insert_one(user_data)
        user = User.get(result.inserted_id)
        login_user(user)
        flash('Registration successful!', 'success')
        return redirect(url_for('index'))

    return render_template('auth/register.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        remember = bool(request.form.get('remember'))

        if not username or not password:
            flash('Username and password are required.', 'error')
            return render_template('auth/login.html')

        user = User.get_by_username(username)
        if user and bcrypt.check_password_hash(user.password_hash, password):
            login_user(user, remember=remember)
            next_page = request.args.get('next')
            flash('Login successful!', 'success')
            return redirect(next_page) if next_page else redirect(url_for('index'))
        else:
            flash('Invalid username or password.', 'error')

    return render_template('auth/login.html')

@app.route('/logout')
@login_required
def logout():
    logout_user()
    flash('You have been logged out.', 'info')
    return redirect(url_for('index'))

@app.route('/forgot_password', methods=['GET', 'POST'])
def forgot_password():
    if request.method == 'POST':
        email = request.form.get('email')
        user = User.get_by_email(email)
        
        if user:
            # Generate reset token
            token = secrets.token_urlsafe(32)
            reset_tokens_collection.insert_one({
                'user_id': ObjectId(user.id),
                'token': token,
                'created_at': datetime.now(timezone.utc),
                'expires_at': datetime.now(timezone.utc) + timedelta(hours=1)
            })
            
            # Send reset email
            if app.config['MAIL_USERNAME']:
                try:
                    msg = Message(
                        'Password Reset Request - Duffin\'s Blog',
                        sender=app.config['MAIL_USERNAME'],
                        recipients=[email]
                    )
                    reset_url = url_for('reset_password', token=token, _external=True)
                    msg.body = f'''To reset your password, visit the following link:
{reset_url}

If you did not make this request, simply ignore this email and no changes will be made.

This link will expire in 1 hour.
'''
                    mail.send(msg)
                    flash('A password reset email has been sent.', 'info')
                except Exception as e:
                    flash('Error sending email. Please try again later.', 'error')
            else:
                flash('Email service not configured. Please contact administrator.', 'error')
        else:
            # Don't reveal whether the email exists
            flash('If that email address is in our system, you will receive a password reset email.', 'info')
    
    return render_template('auth/forgot_password.html')

@app.route('/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    reset_token = reset_tokens_collection.find_one({
        'token': token,
        'expires_at': {'$gt': datetime.now(timezone.utc)}
    })
    
    if not reset_token:
        flash('Invalid or expired reset token.', 'error')
        return redirect(url_for('forgot_password'))
    
    if request.method == 'POST':
        password = request.form.get('password')
        confirm_password = request.form.get('confirm_password')
        
        if not password or not confirm_password:
            flash('Both password fields are required.', 'error')
            return render_template('auth/reset_password.html', token=token)
        
        if password != confirm_password:
            flash('Passwords do not match.', 'error')
            return render_template('auth/reset_password.html', token=token)
        
        if len(password) < 6:
            flash('Password must be at least 6 characters long.', 'error')
            return render_template('auth/reset_password.html', token=token)
        
        # Update password
        password_hash = bcrypt.generate_password_hash(password).decode('utf-8')
        users_collection.update_one(
            {'_id': reset_token['user_id']},
            {'$set': {'password_hash': password_hash}}
        )
        
        # Delete the reset token
        reset_tokens_collection.delete_one({'_id': reset_token['_id']})
        
        flash('Your password has been updated!', 'success')
        return redirect(url_for('login'))
    
    return render_template('auth/reset_password.html', token=token)

# Blog Routes
@app.route('/')
def index():
    posts = list(posts_collection.find().sort('timestamp', -1))
    for post in posts:
        post['parsed_content'] = parse_content_for_display(post.get('content', ''))
        if 'title' not in post or not post['title']:
            post['title'] = "Untitled Post"
        if 'slug' not in post or not post['slug']:
            post['slug'] = slugify(post['title']) if post['title'] else f"untitled-post-{post['_id']}"
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
@login_required
def create_post_page():
    if request.method == 'POST':
        title = request.form.get('title')
        content = request.form.get('content')
        tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
        hero_banner_file = request.files.get('hero_banner')

        if not title:
            flash('Title is required.', 'error')
            return render_template('create_post.html', is_edit=False, title=title, content=content, tags=",".join(tags))

        post_slug = slugify(title)
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
            'content': content,
            'tags': tags,
            'hero_banner_url': hero_banner_url,
            'author_id': ObjectId(current_user.id),
            'author_username': current_user.username,
            'timestamp': datetime.now(timezone.utc),
            'last_updated': datetime.now(timezone.utc)
        }
        posts_collection.insert_one(post_data)
        flash('Post created successfully!', 'success')
        return redirect(url_for('view_post', slug=post_slug))

    return render_template('create_post.html', is_edit=False)

@app.route('/edit/<slug>', methods=['GET', 'POST'])
@login_required
def edit_post_page(slug):
    post = posts_collection.find_one({"slug": slug})
    if not post:
        return "Post not found", 404

    # Check if user owns the post
    if str(post.get('author_id')) != current_user.id:
        flash('You can only edit your own posts.', 'error')
        return redirect(url_for('view_post', slug=slug))

    if request.method == 'POST':
        title = request.form.get('title')
        content = request.form.get('content')
        tags = [tag.strip() for tag in request.form.get('tags', '').split(',') if tag.strip()]
        hero_banner_file = request.files.get('hero_banner')

        if not title:
            flash('Title is required.', 'error')
            return render_template('create_post.html', post=post, is_edit=True, title=title, content=content, tags=",".join(tags))

        new_slug = slugify(title)
        if new_slug != slug and posts_collection.find_one({"slug": new_slug}):
            flash('A post with this title already exists.', 'error')
            return render_template('create_post.html', post=post, is_edit=True, title=title, content=content, tags=",".join(tags))
        
        update_data = {
            'title': title,
            'content': content,
            'tags': tags,
            'last_updated': datetime.now(timezone.utc)
        }
        if new_slug != slug:
            update_data['slug'] = new_slug
        
        if hero_banner_file and allowed_file(hero_banner_file.filename):
            filename = secure_filename(f"{new_slug}-hero-{hero_banner_file.filename}")
            hero_banner_file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            update_data['hero_banner_url'] = url_for('static', filename=f'uploads/{filename}')
        elif request.form.get('remove_hero_banner'):
            update_data['hero_banner_url'] = None

        posts_collection.update_one({"_id": post['_id']}, {"$set": update_data})
        flash('Post updated successfully!', 'success')
        return redirect(url_for('view_post', slug=update_data.get('slug', slug)))

    return render_template('create_post.html', post=post, is_edit=True, title=post.get('title'), content=post.get('content'), tags=",".join(post.get('tags',[])))

@app.route('/upload_image', methods=['POST'])
@login_required
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

@app.route('/post/<slug>/delete', methods=['POST'])
@login_required
def delete_post(slug):
    post = posts_collection.find_one({"slug": slug})
    if not post:
        return "Post not found", 404
    
    # Check if user owns the post
    if str(post.get('author_id')) != current_user.id:
        flash('You can only delete your own posts.', 'error')
        return redirect(url_for('view_post', slug=slug))
    
    posts_collection.delete_one({"_id": post['_id']})
    
    # Clean up associated files
    if post.get('hero_banner_url'):
        try:
            hero_banner_path = os.path.join(app.config['UPLOAD_FOLDER'], os.path.basename(post['hero_banner_url']))
            if os.path.exists(hero_banner_path):
                os.remove(hero_banner_path)
        except Exception as e:
            print(f"Error deleting hero banner for {slug}: {e}")
    
    flash('Post deleted successfully!', 'success')
    return redirect(url_for('index'))

# Test email endpoint (remove in production)
@app.route('/test_email')
@login_required
def test_email():
    if not current_user.username == 'admin':  # Only allow admin to test
        flash('Access denied.', 'error')
        return redirect(url_for('index'))
    
    try:
        msg = Message(
            'Test Email - Duffin\'s Blog',
            sender=app.config['MAIL_USERNAME'],
            recipients=[current_user.email]
        )
        msg.body = 'This is a test email to verify your email configuration is working correctly.'
        mail.send(msg)
        flash('Test email sent successfully!', 'success')
    except Exception as e:
        flash(f'Error sending test email: {str(e)}', 'error')
    
    return redirect(url_for('index'))

# API Routes
@app.route('/api/login', methods=['POST'])
def api_login():
    try:
        data = request.get_json()
        username = data.get('username')
        password = data.get('password')

        if not username or not password:
            return jsonify({'message': 'Username and password are required'}), 400

        user = User.get_by_username(username)
        if user and bcrypt.check_password_hash(user.password_hash, password):
            token = jwt.encode({
                'user_id': user.id,
                'username': user.username,
                'exp': datetime.utcnow() + timedelta(days=7)
            }, app.config['JWT_SECRET_KEY'], algorithm='HS256')
            
            return jsonify({
                'token': token,
                'user': {
                    'id': user.id,
                    'username': user.username,
                    'email': user.email
                }
            }), 200
        else:
            return jsonify({'message': 'Invalid credentials'}), 401
    except Exception as e:
        return jsonify({'message': 'Login failed', 'error': str(e)}), 500

@app.route('/api/register', methods=['POST'])
def api_register():
    try:
        data = request.get_json()
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')

        if not all([username, email, password]):
            return jsonify({'message': 'All fields are required'}), 400

        if len(password) < 6:
            return jsonify({'message': 'Password must be at least 6 characters long'}), 400

        if User.get_by_username(username):
            return jsonify({'message': 'Username already exists'}), 400

        if User.get_by_email(email):
            return jsonify({'message': 'Email already registered'}), 400

        password_hash = bcrypt.generate_password_hash(password).decode('utf-8')
        user_data = {
            'username': username,
            'email': email,
            'password_hash': password_hash,
            'created_at': datetime.now(timezone.utc),
            'is_active': True
        }
        
        result = users_collection.insert_one(user_data)
        user = User.get(result.inserted_id)
        
        token = jwt.encode({
            'user_id': user.id,
            'username': user.username,
            'exp': datetime.utcnow() + timedelta(days=7)
        }, app.config['JWT_SECRET_KEY'], algorithm='HS256')
        
        return jsonify({
            'token': token,
            'user': {
                'id': user.id,
                'username': user.username,
                'email': user.email
            }
        }), 201
    except Exception as e:
        return jsonify({'message': 'Registration failed', 'error': str(e)}), 500

@app.route('/api/posts', methods=['GET'])
def api_get_posts():
    try:
        posts = list(posts_collection.find().sort('timestamp', -1))
        for post in posts:
            post['_id'] = str(post['_id'])
            post['author_id'] = str(post.get('author_id', ''))
            post['parsed_content'] = parse_content_for_display(post.get('content', ''))
            if 'timestamp' in post:
                post['timestamp'] = post['timestamp'].isoformat()
            if 'last_updated' in post:
                post['last_updated'] = post['last_updated'].isoformat()
        return jsonify({'posts': posts}), 200
    except Exception as e:
        return jsonify({'message': 'Failed to fetch posts', 'error': str(e)}), 500

@app.route('/api/posts/<slug>', methods=['GET'])
def api_get_post(slug):
    try:
        post = posts_collection.find_one({"slug": slug})
        if not post:
            return jsonify({'message': 'Post not found'}), 404
        
        post['_id'] = str(post['_id'])
        post['author_id'] = str(post.get('author_id', ''))
        post['parsed_content'] = parse_content_for_display(post.get('content', ''))
        if 'timestamp' in post:
            post['timestamp'] = post['timestamp'].isoformat()
        if 'last_updated' in post:
            post['last_updated'] = post['last_updated'].isoformat()
        
        return jsonify({'post': post}), 200
    except Exception as e:
        return jsonify({'message': 'Failed to fetch post', 'error': str(e)}), 500

@app.route('/api/posts', methods=['POST'])
@token_required
def api_create_post(current_user_obj):
    try:
        data = request.get_json()
        title = data.get('title')
        content = data.get('content', '')
        tags = data.get('tags', [])

        if not title:
            return jsonify({'message': 'Title is required'}), 400

        post_slug = slugify(title)
        original_slug = post_slug
        counter = 1
        while posts_collection.find_one({"slug": post_slug}):
            post_slug = f"{original_slug}-{counter}"
            counter += 1

        post_data = {
            'title': title,
            'slug': post_slug,
            'content': content,
            'tags': tags if isinstance(tags, list) else [],
            'author_id': ObjectId(current_user_obj.id),
            'author_username': current_user_obj.username,
            'timestamp': datetime.now(timezone.utc),
            'last_updated': datetime.now(timezone.utc)
        }
        
        result = posts_collection.insert_one(post_data)
        post_data['_id'] = str(result.inserted_id)
        post_data['author_id'] = str(post_data['author_id'])
        post_data['timestamp'] = post_data['timestamp'].isoformat()
        post_data['last_updated'] = post_data['last_updated'].isoformat()
        
        return jsonify({'post': post_data}), 201
    except Exception as e:
        return jsonify({'message': 'Failed to create post', 'error': str(e)}), 500

@app.route('/api/posts/<slug>', methods=['PUT'])
@token_required
def api_update_post(current_user_obj, slug):
    try:
        post = posts_collection.find_one({"slug": slug})
        if not post:
            return jsonify({'message': 'Post not found'}), 404

        if str(post.get('author_id')) != current_user_obj.id:
            return jsonify({'message': 'You can only edit your own posts'}), 403

        data = request.get_json()
        title = data.get('title')
        content = data.get('content')
        tags = data.get('tags', [])

        if not title:
            return jsonify({'message': 'Title is required'}), 400

        new_slug = slugify(title)
        if new_slug != slug and posts_collection.find_one({"slug": new_slug}):
            return jsonify({'message': 'A post with this title already exists'}), 400

        update_data = {
            'title': title,
            'content': content,
            'tags': tags if isinstance(tags, list) else [],
            'last_updated': datetime.now(timezone.utc)
        }
        if new_slug != slug:
            update_data['slug'] = new_slug

        posts_collection.update_one({"_id": post['_id']}, {"$set": update_data})
        
        updated_post = posts_collection.find_one({"_id": post['_id']})
        updated_post['_id'] = str(updated_post['_id'])
        updated_post['author_id'] = str(updated_post.get('author_id', ''))
        updated_post['timestamp'] = updated_post['timestamp'].isoformat()
        updated_post['last_updated'] = updated_post['last_updated'].isoformat()
        
        return jsonify({'post': updated_post}), 200
    except Exception as e:
        return jsonify({'message': 'Failed to update post', 'error': str(e)}), 500

@app.route('/api/posts/<slug>', methods=['DELETE'])
@token_required
def api_delete_post(current_user_obj, slug):
    try:
        post = posts_collection.find_one({"slug": slug})
        if not post:
            return jsonify({'message': 'Post not found'}), 404

        if str(post.get('author_id')) != current_user_obj.id:
            return jsonify({'message': 'You can only delete your own posts'}), 403

        posts_collection.delete_one({"_id": post['_id']})
        return jsonify({'message': 'Post deleted successfully'}), 200
    except Exception as e:
        return jsonify({'message': 'Failed to delete post', 'error': str(e)}), 500

if __name__ == '__main__':
    # Create upload directory if it doesn't exist
    os.makedirs(UPLOAD_FOLDER, exist_ok=True)
    app.run(debug=True, port=5003)
